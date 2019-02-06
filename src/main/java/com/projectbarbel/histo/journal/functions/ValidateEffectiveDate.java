package com.projectbarbel.histo.journal.functions;

import java.time.LocalDate;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.model.Bitemporal;

public class ValidateEffectiveDate implements BiPredicate<Bitemporal, LocalDate> {

    @Override
    public boolean test(Bitemporal currentVersion, LocalDate newEffectiveFrom) {
        Validate.isTrue(newEffectiveFrom.isBefore(currentVersion.getBitemporalStamp().getEffectiveTime().until()),
                "effective date must be before current versions effective until");
        Validate.isTrue(newEffectiveFrom.isBefore(LocalDate.MAX),
                "effective date cannot be infinite");
        Validate.inclusiveBetween(currentVersion.getBitemporalStamp().getEffectiveTime().from().toEpochDay(),
                currentVersion.getBitemporalStamp().getEffectiveTime().until().toEpochDay(),
                newEffectiveFrom.toEpochDay(),
                "effective date of new version must be withing effective period of current version");
        return true;
    }

}
