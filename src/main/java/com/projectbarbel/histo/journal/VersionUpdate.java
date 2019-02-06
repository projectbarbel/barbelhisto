package com.projectbarbel.histo.journal;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.journal.functions.ValidateEffectiveDate;
import com.projectbarbel.histo.model.Bitemporal;

public final class VersionUpdate<T> {

    public enum UpdateState {
        PREPARATION(() -> {
        }, () -> Validate.validState(false, "this method is not allowed in state PTREPARATION")),
        EXECUTED(() -> Validate.validState(false, "this method is not allowed in state EXECUTED"), () -> {
        });
        private Runnable inputSetter;
        private Runnable outputGetter;

        private UpdateState(Runnable inputSetter, Runnable outputGetter) {
            this.inputSetter = inputSetter;
            this.outputGetter = outputGetter;
        }

        public <T> T set(T value) {
            inputSetter.run();
            return value;
        }

        public <T> T get(T value) {
            outputGetter.run();
            return value;
        }
    }

    private final T oldVersion;
    private T newPrecedingVersion;
    private T newSubsequentVersion;
    private LocalDate newEffectiveDate;
    private LocalDate newEffectiveUntil;
    private String activity = "SYSTEM_ACTIVITY";
    private String createdBy = "SYSTEM";
    private UpdateState state = UpdateState.PREPARATION;
    private Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> updateExecutionFunction;
    private Function<T, T> copyFunction;
    private final Map<String, Object> propertyUpdates = new HashMap<>();
    private VersionUpdateResult<T> result;

    private VersionUpdate(Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> updateExecutionFunction,
            Function<T, T> copyFunction, T bitemporal) {
        oldVersion = Objects.requireNonNull(bitemporal, "bitemporal object must not be null");
        newEffectiveDate = Objects.requireNonNull(
                ((Bitemporal) bitemporal).getBitemporalStamp().getEffectiveTime().from(),
                "the bitemporal passed must not contain null value on effective from");
        newEffectiveUntil = Objects.requireNonNull(
                ((Bitemporal) bitemporal).getBitemporalStamp().getEffectiveTime().until(),
                "the bitemporal passed must not contain null value on effective until");
        this.updateExecutionFunction = updateExecutionFunction;
        this.copyFunction = copyFunction;
    }

    public static <T> VersionUpdate<T> of(
            Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> updateExecutionFunction,
            Function<T, T> copyFunction, T document) {
        return new VersionUpdate<T>(updateExecutionFunction, copyFunction, document);
    }

    public VersionUpdateExecutionBuilder<T> prepare() {
        return new VersionUpdateExecutionBuilder<T>(this);
    }

    @SuppressWarnings("unchecked")
    public <O> VersionUpdateResult<O> execute() {
        result = state.set(updateExecutionFunction.apply(new UpdateExecutionContext<T>(this, propertyUpdates)));
        state = UpdateState.EXECUTED;
        return (VersionUpdateResult<O>) result;
    }

    public VersionUpdateResult<T> result() {
        return state.get(result);
    }

    public boolean isDone() {
        return state.equals(UpdateState.EXECUTED);
    }

    public static class VersionUpdateResult<T> {

        private VersionUpdate<T> update;

        private VersionUpdateResult(VersionUpdate<T> update, T newPrecedingVersion, T subSequentVersion) {
            update.newPrecedingVersion = newPrecedingVersion;
            update.newSubsequentVersion = subSequentVersion;
            this.update = update;
        }

        public T oldVersion() {
            return update.oldVersion;
        }

        public T newPrecedingVersion() {
            return update.newPrecedingVersion;
        }

        public T newSubsequentVersion() {
            return update.newSubsequentVersion;
        }

        public LocalDate effectiveFrom() {
            return update.newEffectiveDate;
        }

        public LocalDate effectiveUntil() {
            return update.newEffectiveUntil;
        }

    }

    public static class VersionUpdateExecutionBuilder<T> {
        private final VersionUpdate<T> update;
        private BiPredicate<Bitemporal, LocalDate> effectiveDateValidationFuction = new ValidateEffectiveDate();

        private VersionUpdateExecutionBuilder(VersionUpdate<T> update) {
            this.update = update;
        }

        public VersionUpdateExecutionBuilder<T> effectiveFrom(LocalDate newEffectiveFrom) {
            if (!effectiveDateValidationFuction.test((Bitemporal) update.oldVersion, newEffectiveFrom))
                throw new IllegalArgumentException("new effective date is not valid: " + newEffectiveFrom
                        + " - old version: " + update.oldVersion.toString());
            update.newEffectiveDate = update.state.set(newEffectiveFrom);
            return this;
        }

        public VersionUpdateExecutionBuilder<T> untilInfinite() {
            update.newEffectiveUntil = update.state.set(BarbelHistoContext.getInfiniteDate());
            return this;
        }

        public VersionUpdateExecutionBuilder<T> until(LocalDate newEffectiveUntil) {
            update.newEffectiveUntil = update.state.set(newEffectiveUntil);
            return this;
        }

        public VersionUpdateExecutionBuilder<T> activity(String activity) {
            update.activity = update.state.set(activity);
            return this;
        }

        public VersionUpdateExecutionBuilder<T> createdBy(String createdBy) {
            update.createdBy = update.state.set(createdBy);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <O> VersionUpdateResult<O> execute() {
            return (VersionUpdateResult<O>) update.execute();
        }

        public VersionUpdate<T> get() {
            return update;
        }

    }

    public static class UpdateExecutionContext<T> {
        private VersionUpdate<T> update;
        private Map<String, Object> propertyUpdates;

        public Map<String, Object> propertyUpdates() {
            return propertyUpdates;
        }

        private UpdateExecutionContext(VersionUpdate<T> update, Map<String, Object> propertyUpdates) {
            super();
            this.update = update;
            this.propertyUpdates = propertyUpdates;
        }

        public T oldVersion() {
            return update.oldVersion;
        }

        public LocalDate newEffectiveFrom() {
            return update.newEffectiveDate;
        }

        public String activity() {
            return update.activity;
        }

        public String createdBy() {
            return update.createdBy;
        }

        public Function<T, T> copyFunction() {
            return update.copyFunction;
        }

        public VersionUpdateResult<T> createExecutionResult(T newPrecedingVersion, T newSubsequentVersion) {
            return new VersionUpdateResult<T>(update, newPrecedingVersion, newSubsequentVersion);
        }

    }

}
