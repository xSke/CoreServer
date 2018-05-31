/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Converter;
import com.google.common.base.Optional;
import com.google.common.base.Platform;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

@GwtCompatible(emulated=true)
@Beta
public final class Enums {
    @GwtIncompatible(value="java.lang.ref.WeakReference")
    private static final Map<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>> enumConstantCache = new WeakHashMap();

    private Enums() {
    }

    @GwtIncompatible(value="reflection")
    public static Field getField(Enum<?> enumValue) {
        Class clazz = enumValue.getDeclaringClass();
        try {
            return clazz.getDeclaredField(enumValue.name());
        }
        catch (NoSuchFieldException impossible) {
            throw new AssertionError(impossible);
        }
    }

    public static <T extends Enum<T>> Optional<T> getIfPresent(Class<T> enumClass, String value) {
        Preconditions.checkNotNull(enumClass);
        Preconditions.checkNotNull(value);
        return Platform.getEnumIfPresent(enumClass, value);
    }

    @GwtIncompatible(value="java.lang.ref.WeakReference")
    private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> populateCache(Class<T> enumClass) {
        HashMap result = new HashMap();
        for (Enum enumInstance : EnumSet.allOf(enumClass)) {
            result.put(enumInstance.name(), new WeakReference<Enum>(enumInstance));
        }
        enumConstantCache.put(enumClass, result);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @GwtIncompatible(value="java.lang.ref.WeakReference")
    static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> getEnumConstants(Class<T> enumClass) {
        Map map = enumConstantCache;
        synchronized (map) {
            Map<String, WeakReference<Enum>> constants = enumConstantCache.get(enumClass);
            if (constants == null) {
                constants = Enums.populateCache(enumClass);
            }
            return constants;
        }
    }

    public static <T extends Enum<T>> Converter<String, T> stringConverter(Class<T> enumClass) {
        return new StringConverter<T>(enumClass);
    }

    private static final class StringConverter<T extends Enum<T>>
    extends Converter<String, T>
    implements Serializable {
        private final Class<T> enumClass;
        private static final long serialVersionUID = 0L;

        StringConverter(Class<T> enumClass) {
            this.enumClass = Preconditions.checkNotNull(enumClass);
        }

        @Override
        protected T doForward(String value) {
            return Enum.valueOf(this.enumClass, value);
        }

        @Override
        protected String doBackward(T enumValue) {
            return enumValue.name();
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (object instanceof StringConverter) {
                StringConverter that = (StringConverter)object;
                return this.enumClass.equals(that.enumClass);
            }
            return false;
        }

        public int hashCode() {
            return this.enumClass.hashCode();
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.enumClass.getName()));
            return new StringBuilder(29 + string.length()).append("Enums.stringConverter(").append(string).append(".class)").toString();
        }
    }

}

