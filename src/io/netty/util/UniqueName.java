/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util;

import io.netty.util.internal.ObjectUtil;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class UniqueName
implements Comparable<UniqueName> {
    private static final AtomicInteger nextId = new AtomicInteger();
    private final int id;
    private final String name;

    public /* varargs */ UniqueName(ConcurrentMap<String, Boolean> map, String name, Object ... args) {
        ObjectUtil.checkNotNull(map, "map");
        if (args != null && args.length > 0) {
            this.validateArgs(args);
        }
        if (map.putIfAbsent(name, Boolean.TRUE) != null) {
            throw new IllegalArgumentException(String.format("'%s' is already in use", name));
        }
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.id = nextId.incrementAndGet();
    }

    protected UniqueName(String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.id = nextId.incrementAndGet();
    }

    protected /* varargs */ void validateArgs(Object ... args) {
    }

    public final String name() {
        return this.name;
    }

    public final int id() {
        return this.id;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    public final boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int compareTo(UniqueName other) {
        if (this == other) {
            return 0;
        }
        int returnCode = this.name.compareTo(other.name);
        if (returnCode != 0) {
            return returnCode;
        }
        return Integer.valueOf(this.id).compareTo(other.id);
    }

    public String toString() {
        return this.name();
    }
}

