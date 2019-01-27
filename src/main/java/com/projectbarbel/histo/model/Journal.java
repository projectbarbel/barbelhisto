package com.projectbarbel.histo.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Journal<T extends Bitemporal<O>, O> {

    private Map<O, Bitemporal<O>> journal;
    private static final Logger logger = Logger.getLogger(Journal.class.getName());
    private Function<Journal<?,?>,Journal<?,?>> policy;

    private Journal(Map<O, Bitemporal<O>> journal) {
        super();
        this.journal = journal;
    }

    public int size() {
        return journal.size();
    }

    public List<? extends Bitemporal<O>> list() {
        return journal.values().stream().collect(Collectors.toList());
    }

    public Map<O, Bitemporal<O>> map() {
        return journal;
    }

    public static <O> Journal<Bitemporal<O>, O> create(List<? extends Bitemporal<O>> listOfBitemporal) {
        return new Journal<Bitemporal<O>, O>(listOfBitemporal.stream().collect(
                Collectors.toMap(Bitemporal::getVersionId, Function.identity())));
    }

    public Journal<T, O> sortByEffectiveDate() {
        journal = journal.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(
                        (v1, v2) -> v1.getEffectiveFromInstant().compareTo(v2.getEffectiveFromInstant())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return this;
    }

    public void prettyPrint() {
        logger.log(Level.INFO, this.toString());
    }

}
