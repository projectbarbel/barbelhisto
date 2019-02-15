package org.projectbarbel.histo.model;

import java.time.LocalDate;
import java.util.Objects;

import org.projectbarbel.histo.BarbelHistoContext;

public final class EffectivePeriod {
    private final LocalDate until;
    private final LocalDate from;

    private EffectivePeriod(LocalDate from, LocalDate until) {
        this.from = Objects.requireNonNull(from);
        this.until = Objects.requireNonNull(until);
    }

    public static EffectivePeriod of(LocalDate from, LocalDate until) {
        return new EffectivePeriod(from, until);
    }
    public boolean isInfinite() {
       return until.equals(BarbelHistoContext.getInfiniteDate());
    }
    
    public LocalDate from() {
        return from;
    }

    public LocalDate until() {
        return until;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EffectivePeriod)) {
            return false;
        }
        EffectivePeriod abstractValueObject = (EffectivePeriod) o;
        return Objects.equals(from, abstractValueObject.from)
                && Objects.equals(until, abstractValueObject.until);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, until);
    }

    @Override
    public String toString() {
        return "EffectivePeriod [from=" + from + ", until=" + until + "]";
    }

}