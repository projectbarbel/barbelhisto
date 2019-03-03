package org.projectbarbel.histo.suite.context;

import java.util.function.Function;

import org.projectbarbel.histo.BarbelHistoBuilder;

public class BTContext_Standard implements BTTestContext{

    @Override
    public Function<Class<?>, BarbelHistoBuilder> contextFunction() {
        return (c)->BarbelHistoBuilder.barbel();
    }

    @Override
    public void clearResources() {
        // NOP
    }

}
