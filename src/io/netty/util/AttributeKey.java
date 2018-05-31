/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util;

import io.netty.util.UniqueName;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import java.util.concurrent.ConcurrentMap;

public final class AttributeKey<T>
extends UniqueName {
    private static final ConcurrentMap<String, AttributeKey> names = PlatformDependent.newConcurrentHashMap();

    public static <T> AttributeKey<T> valueOf(String name) {
        AttributeKey<T> old;
        ObjectUtil.checkNotNull(name, "name");
        AttributeKey<T> option = names.get(name);
        if (option == null && (old = names.putIfAbsent(name, option = new AttributeKey<T>(name))) != null) {
            option = old;
        }
        return option;
    }

    public static boolean exists(String name) {
        ObjectUtil.checkNotNull(name, "name");
        return names.containsKey(name);
    }

    public static <T> AttributeKey<T> newInstance(String name) {
        ObjectUtil.checkNotNull(name, "name");
        AttributeKey<T> option = new AttributeKey<T>(name);
        AttributeKey<T> old = names.putIfAbsent(name, option);
        if (old != null) {
            throw new IllegalArgumentException(String.format("'%s' is already in use", name));
        }
        return option;
    }

    @Deprecated
    public AttributeKey(String name) {
        super(name);
    }
}

