/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.cache;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.cache.RemovalCause;
import java.util.Map;
import javax.annotation.Nullable;

@Beta
@GwtCompatible
public final class RemovalNotification<K, V>
implements Map.Entry<K, V> {
    @Nullable
    private final K key;
    @Nullable
    private final V value;
    private final RemovalCause cause;
    private static final long serialVersionUID = 0L;

    RemovalNotification(@Nullable K key, @Nullable V value, RemovalCause cause) {
        this.key = key;
        this.value = value;
        this.cause = Preconditions.checkNotNull(cause);
    }

    public RemovalCause getCause() {
        return this.cause;
    }

    public boolean wasEvicted() {
        return this.cause.wasEvicted();
    }

    @Nullable
    @Override
    public K getKey() {
        return this.key;
    }

    @Nullable
    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public final V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof Map.Entry) {
            Map.Entry that = (Map.Entry)object;
            return Objects.equal(this.getKey(), that.getKey()) && Objects.equal(this.getValue(), that.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        K k = this.getKey();
        V v = this.getValue();
        return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
    }

    public String toString() {
        String string = String.valueOf(String.valueOf(this.getKey()));
        String string2 = String.valueOf(String.valueOf(this.getValue()));
        return new StringBuilder(1 + string.length() + string2.length()).append(string).append("=").append(string2).toString();
    }
}

