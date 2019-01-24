package com.projectbarbel.histo.model;

import java.lang.reflect.Field;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DefaultPojoCopierSupplier implements Supplier<BiFunction<? extends Bitemporal<?>, ? extends BitemporalStamp, ? extends Bitemporal<?>>>{

    @SuppressWarnings("unchecked")
    public static <O extends Bitemporal<?>> O flatCopyWithNewStamp(Bitemporal<?> from, BitemporalStamp stamp) {
        Class<?> clazz = from.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Object to = null;
        try {
            to = clazz.newInstance();
            for (Field field : fields) {
                Field fieldFrom = from.getClass().getDeclaredField(field.getName());
                fieldFrom.setAccessible(true);
                Object value;
                if (fieldFrom.getType().isInstance(stamp)) {
                    value = stamp;
                } else {
                    value = fieldFrom.get(from);
                }
                Field fieldTo = to.getClass().getDeclaredField(field.getName());
                fieldTo.setAccessible(true);
                fieldTo.set(to, value);
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Cannot instintiate class " + clazz.getName()
                    + " to create new Version. Make sure it has a public no args contsructor!", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access class " + clazz.getName()
                    + " to create new Version. Make sure it has a public no args contsructor!", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Cannot find field in class " + clazz.getName() + " to create new Version!", e);
        }
        return (O)to;
    }

    @Override
    public BiFunction<? extends Bitemporal<?>, ? extends BitemporalStamp, ? extends Bitemporal<?>> get() {
        return DefaultPojoCopierSupplier::flatCopyWithNewStamp;
    }

}
