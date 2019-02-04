package com.projectbarbel.histo;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCoreTest {

    private BarbelHistoCore core;
    
    @Before
    public void setup() {
        core = (BarbelHistoCore) BarbelHistoBuilder.barbel().build();
    }
    
    @Test
    public void testGetIdValue() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        assertEquals(pojo.getDocumentId(), core.getIdValue(pojo).get());
    }

    @Test
    public void testRetrieve() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, core.retrieve(pojo.getDocumentId(), BarbelQueries::all).size());
    }

}
