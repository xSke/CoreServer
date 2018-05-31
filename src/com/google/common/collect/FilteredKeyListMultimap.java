/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Predicate;
import com.google.common.collect.FilteredKeyMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

@GwtCompatible
final class FilteredKeyListMultimap<K, V>
extends FilteredKeyMultimap<K, V>
implements ListMultimap<K, V> {
    FilteredKeyListMultimap(ListMultimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
        super(unfiltered, keyPredicate);
    }

    @Override
    public ListMultimap<K, V> unfiltered() {
        return (ListMultimap)super.unfiltered();
    }

    @Override
    public List<V> get(K key) {
        return (List)super.get(key);
    }

    @Override
    public List<V> removeAll(@Nullable Object key) {
        return (List)super.removeAll(key);
    }

    @Override
    public List<V> replaceValues(K key, Iterable<? extends V> values) {
        return (List)super.replaceValues(key, values);
    }
}

