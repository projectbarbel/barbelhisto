package com.projectbarbel.histo.journal.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;

public class JournalUpdateStrategyEmbedding implements BiFunction<DocumentJournal, Bitemporal, List<Object>> {

    private final BarbelHistoContext context;
    private List<Object> newVersions = new ArrayList<>();
    private JournalUpdateCase actualCase;

    public JournalUpdateCase getActualCase() {
        return actualCase;
    }

    public JournalUpdateStrategyEmbedding(BarbelHistoContext context) {
        this.context = context;
    }

    @Override
    public List<Object> apply(DocumentJournal journal, final Bitemporal update) {
        Validate.isTrue(journal.getId().equals(update.getBitemporalStamp().getDocumentId()),
                "update and journal must have same document id");
        Validate.isTrue(update.getBitemporalStamp().isActive(), "only active bitemporals are allowed here");
        Optional<Bitemporal> interruptedFromVersion = journal.read().effectiveTime()
                .effectiveAt(update.getBitemporalStamp().getEffectiveTime().from());
        Optional<Bitemporal> interruptedUntilVersion = journal.read().effectiveTime()
                .effectiveAt(update.getBitemporalStamp().getEffectiveTime().until());
        IndexedCollection<Bitemporal> betweenVersions = journal.read().effectiveTime()
                .effectiveBetween(update.getBitemporalStamp().getEffectiveTime());
        actualCase = JournalUpdateCase.validate(interruptedFromVersion.isPresent(), interruptedUntilVersion.isPresent(),
                interruptedFromVersion.equals(interruptedUntilVersion), !betweenVersions.isEmpty());
        newVersions.add(update);
        interruptedFromVersion.ifPresent(d -> processInterruptedFrom(update, d));
        interruptedUntilVersion.ifPresent(d -> processInterruptedUntil(update, d));
        interruptedFromVersion
                .ifPresent(d -> d.setBitemporalStamp(d.getBitemporalStamp().inactivatedCopy(context)));
        interruptedUntilVersion
                .ifPresent(d -> d.setBitemporalStamp(d.getBitemporalStamp().inactivatedCopy(context)));
        betweenVersions.stream()
                .forEach(d -> d.setBitemporalStamp(d.getBitemporalStamp().inactivatedCopy(context)));
        return newVersions;
    }

    private void processInterruptedFrom(final Bitemporal update, Bitemporal interruptedFrom) {
        Bitemporal newPrecedingVersion = context.getMode().snapshotManagedBitemporal(context, interruptedFrom,
                BitemporalStamp.createActiveWithContext(context, update.getBitemporalStamp().getDocumentId(),
                        EffectivePeriod.of(interruptedFrom.getBitemporalStamp().getEffectiveTime().from(),
                                update.getBitemporalStamp().getEffectiveTime().from())));
        newVersions.add(newPrecedingVersion);
    }

    private void processInterruptedUntil(final Bitemporal update, Bitemporal interruptedUntil) {
        Bitemporal newSubsequentVersion = context.getMode().snapshotManagedBitemporal(context, interruptedUntil,
                BitemporalStamp.createActiveWithContext(context, update.getBitemporalStamp().getDocumentId(),
                        EffectivePeriod.of(update.getBitemporalStamp().getEffectiveTime().until(),
                                interruptedUntil.getBitemporalStamp().getEffectiveTime().until())));
        newVersions.add(newSubsequentVersion);
    }

    public enum JournalUpdateCase {

        //// @formatter:off

        PREOVERLAPPING(asByte(new boolean[] {false, true, false, false})),   
        // A:      |---------|
        // U: |-------|
                                   
        POSTOVERLAPPING(asByte(new boolean[] {true, false, false, false})), 
        // A: |-------|
        // U:      |---------|

        EMBEDDEDINTERVAL(asByte(new boolean[] {true, true, true, false})),  
        // A: |--------------|
        // U:     |------|
        
        EMBEDDEDOVERLAP(asByte(new boolean[] {true, true, false, false})),   
        // A: |-------|------|------|
        // U:     |-------|
                                   
        OVERLAY(asByte(new boolean[] {false, false, true, true})),    
        // A:         |------|
        // U:     |--------------|
        
        EMBEDDEDOVERLAY(asByte(new boolean[] {true, true, false, true})),    
        // A: |-------|------|------|
        // U:     |--------------|

        EMBEDDEDOVERLAY_PREOVERLAPPING(asByte(new boolean[] {false, true, false, true})),    
        // A:     |-------|------|
        // U: |--------------|
        
        EMBEDDEDOVERLAY_POSTOVERLAPPING(asByte(new boolean[] {true, false, false, true}));    
        // A: |------|------|
        // U:    |--------------|

        // @formatter:on

        private byte pattern;

        private JournalUpdateCase(byte pattern) {
            this.pattern = pattern;
        }

        public static JournalUpdateCase validate(boolean interruptedFrom, boolean interruptedUntil, boolean interruptedEqual,
                boolean betweenVersions) {
            byte pattern = asByte(
                    new boolean[] { interruptedFrom, interruptedUntil, interruptedEqual, betweenVersions });
            JournalUpdateCase validCase = Arrays.asList(JournalUpdateCase.values()).stream().filter(c -> pattern == c.getPattern()).findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "unknown case for journal update: " + Byte.toString(pattern)));
            return validCase;
        }

        private byte getPattern() {
            return pattern;
        }

    }

    private static byte asByte(boolean[] source) {
        byte result = 0;

        int index = 8 - source.length;

        for (int i = 0; i < source.length; i++) {
            if (source[i])
                result |= (byte) (1 << (7 - index));
            index++;
        }
        return result;
    }

}
