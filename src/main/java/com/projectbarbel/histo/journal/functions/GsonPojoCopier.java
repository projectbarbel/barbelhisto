package com.projectbarbel.histo.journal.functions;

import java.util.function.Function;

import com.google.gson.Gson;
import com.projectbarbel.histo.BarbelHistoContext;

public class GsonPojoCopier implements Function<Object, Object>{

    private Gson gson = BarbelHistoContext.getDefaultGson();
    
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object apply(Object objectFrom) {
        Object copy = gson.fromJson(gson.toJson(objectFrom), objectFrom.getClass());
        return copy;
    }

}