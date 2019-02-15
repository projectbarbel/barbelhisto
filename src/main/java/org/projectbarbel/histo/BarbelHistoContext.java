package org.projectbarbel.histo;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.projectbarbel.histo.BarbelHistoCore.UpdateLogRecord;
import org.projectbarbel.histo.functions.DefaultIDGenerator;
import org.projectbarbel.histo.functions.DefaultPojoCopier;
import org.projectbarbel.histo.functions.DefaultPrettyPrinter;
import org.projectbarbel.histo.functions.DefaultProxyingFunction;
import org.projectbarbel.histo.functions.SimpleGsonPojoSerializer;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.Systemclock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

public interface BarbelHistoContext {

    public static final String SYSTEM = "SYSTEM";
    public static final String SYSTEMACTIVITY = "SYSTEMACTIVITY";
    public static final Systemclock CLOCK = new Systemclock();
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    public static final JsonDeserializer<ZonedDateTime> ZDT_DESERIALIZER = new JsonDeserializer<ZonedDateTime>() {
        @Override
        public ZonedDateTime deserialize(final JsonElement json, final Type typeOfT,
                final JsonDeserializationContext context) throws JsonParseException {
            return DATE_FORMATTER.parse(json.getAsString(), ZonedDateTime::from);
        }
    };
    
    public static final JsonSerializer<ZonedDateTime> ZDT_SERIALIZER = new JsonSerializer<ZonedDateTime>() {
        public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DATE_FORMATTER.format(src));
        }
    };

    static <T> Supplier<IndexedCollection<T>> getDefaultBackbone() {
        return () -> new ConcurrentIndexedCollection<T>();
    }

    static Function<BarbelHistoContext, PojoSerializer<Bitemporal>> getDefaultPersistenceSerializerProducer() {
        return (c) -> new SimpleGsonPojoSerializer(BarbelHistoBuilder.barbel());
    }

    static Function<List<Bitemporal>, String> getDefaultPrettyPrinter() {
        return new DefaultPrettyPrinter();
    }

    static IndexedCollection<UpdateLogRecord> getDefaultUpdateLog() {
        return new ConcurrentIndexedCollection<BarbelHistoCore.UpdateLogRecord>();
    }

    static BarbelMode getDefaultBarbelMode() {
        return BarbelMode.POJO;
    }

    static String getDefaultActivity() {
        return SYSTEMACTIVITY;
    }

    static LocalDate getInfiniteDate() {
        return LocalDate.MAX;
    }

    static Systemclock getDefaultClock() {
        return CLOCK;
    }

    static Supplier<Object> getDefaultDocumentIDGenerator() {
        return new DefaultIDGenerator();
    }

    static Supplier<Object> getDefaultVersionIDGenerator() {
        return new DefaultIDGenerator();
    }

    static String getDefaultUser() {
        return SYSTEM;
    }

    static BiFunction<Object, BitemporalStamp, Object> getDefaultProxyingFunction() {
        return new DefaultProxyingFunction();
    }

    static Gson getDefaultGson() {
        return new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, ZDT_DESERIALIZER)
                .registerTypeAdapter(ZonedDateTime.class, ZDT_SERIALIZER).create();
    }

    static Function<Object, Object> getDefaultCopyFunction() {
        return new DefaultPojoCopier();
    }

    Supplier<Object> getDocumentIdGenerator();

    Supplier<Object> getVersionIdGenerator();

    <T> Supplier<IndexedCollection<T>> getBackboneSupplier();

    String getActivity();

    String getUser();

    Map<Object, DocumentJournal> getJournalStore();

    BiFunction<Object, BitemporalStamp, Object> getPojoProxyingFunction();

    Function<Object, Object> getPojoCopyFunction();

    Gson getGson();

    Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> getJournalUpdateStrategyProducer();

    BarbelMode getMode();

    Systemclock getClock();

    IndexedCollection<UpdateLogRecord> getUpdateLog();

    Function<List<Bitemporal>, String> getPrettyPrinter();
    
    Function<BarbelHistoContext, PojoSerializer<Bitemporal>> getPersistenceSerializerProducer();

}