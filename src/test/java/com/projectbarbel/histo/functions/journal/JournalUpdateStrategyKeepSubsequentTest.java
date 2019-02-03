package com.projectbarbel.histo.functions.journal;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.api.VersionUpdate;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.Systemclock;

public class JournalUpdateStrategyKeepSubsequentTest {

    private DocumentJournal<DefaultDocument> journal;
    private Systemclock clock = new Systemclock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));

    @Before
    public void setUp() {
        journal = DocumentJournal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("someId", Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))));
    }
    
    @Test
    public void testApply_KeepSubsequent() throws Exception {
        VersionUpdate<DefaultDocument> update = VersionUpdate.of(journal.list().get(3)).prepare().from(clock.now().toLocalDate()).setProperty("data", "some new data").get();
        assertTrue(journal.read().activeVersions().size()==4);
        VersionUpdateResult<DefaultDocument> result = update.execute();
        JournalUpdateStrategyKeepSubsequent<DefaultDocument> updateFunction = new JournalUpdateStrategyKeepSubsequent<DefaultDocument>();
        List<DefaultDocument> updateVersion = updateFunction.apply(journal, update.result());
        assertTrue(result.newSubsequentVersion().getData().equals("some new data"));
        assertTrue(result.newPrecedingVersion().getData().equals(journal.list().get(3).getData()));
        assertTrue(updateVersion.size()==2);
        assertTrue(journal.read().inactiveVersions().size()==1);
    }

    @Test
    public void testApply_KeepSubsequent_subsequentPeriodsExist_andShouldNotBeInactivated() throws Exception {
        VersionUpdate<DefaultDocument> update = VersionUpdate.of(journal.list().get(2)).prepare().from(LocalDate.of(2018, 7, 1)).setProperty("data", "some new data").get();
        assertTrue(journal.read().activeVersions().size()==4);
        VersionUpdateResult<DefaultDocument> result = update.execute();
        JournalUpdateStrategyKeepSubsequent<DefaultDocument> updateFunction = new JournalUpdateStrategyKeepSubsequent<DefaultDocument>();
        List<DefaultDocument> updateVersion = updateFunction.apply(journal, update.result());
        assertTrue(result.newSubsequentVersion().getData().equals("some new data"));
        assertTrue(result.newPrecedingVersion().getData().equals(journal.list().get(2).getData()));
        assertTrue(updateVersion.size()==2);
        assertTrue(journal.read().inactiveVersions().size()==1);
    }
    
    @Test
    public void testApply_KeepSubsequent_multiplieSubsequentPeriodsExist_andShouldNotBeInactivated() throws Exception {
        VersionUpdate<DefaultDocument> update = VersionUpdate.of(journal.list().get(0)).prepare().from(LocalDate.of(2016, 7, 1)).setProperty("data", "some new data").get();
        assertTrue(journal.read().activeVersions().size()==4);
        VersionUpdateResult<DefaultDocument> result = update.execute();
        JournalUpdateStrategyKeepSubsequent<DefaultDocument> updateFunction = new JournalUpdateStrategyKeepSubsequent<DefaultDocument>();
        List<DefaultDocument> updateVersion = updateFunction.apply(journal, update.result());
        assertTrue(result.newSubsequentVersion().getData().equals("some new data"));
        assertTrue(result.newPrecedingVersion().getData().equals(journal.list().get(0).getData()));
        assertTrue(updateVersion.size()==2);
        assertTrue(journal.read().inactiveVersions().size()==1);
    }
        
}
