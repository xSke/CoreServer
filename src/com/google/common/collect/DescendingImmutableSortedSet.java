/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import javax.annotation.Nullable;

class DescendingImmutableSortedSet<E>
extends ImmutableSortedSet<E> {
    private final ImmutableSortedSet<E> forward;

    DescendingImmutableSortedSet(ImmutableSortedSet<E> forward) {
        super(Ordering.from(forward.comparator()).reverse());
        this.forward = forward;
    }

    @Override
    public int size() {
        return this.forward.size();
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return this.forward.descendingIterator();
    }

    @Override
    ImmutableSortedSet<E> headSetImpl(E toElement, boolean inclusive) {
        return this.forward.tailSet((Object)toElement, inclusive).descendingSet();
    }

    @Override
    ImmutableSortedSet<E> subSetImpl(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return this.forward.subSet((Object)toElement, toInclusive, (Object)fromElement, fromInclusive).descendingSet();
    }

    @Override
    ImmutableSortedSet<E> tailSetImpl(E fromElement, boolean inclusive) {
        return this.forward.headSet((Object)fromElement, inclusive).descendingSet();
    }

    @GwtIncompatible(value="NavigableSet")
    @Override
    public ImmutableSortedSet<E> descendingSet() {
        return this.forward;
    }

    @GwtIncompatible(value="NavigableSet")
    @Override
    public UnmodifiableIterator<E> descendingIterator() {
        return this.forward.iterator();
    }

    @GwtIncompatible(value="NavigableSet")
    @Override
    ImmutableSortedSet<E> createDescendingSet() {
        throw new AssertionError((Object)"should never be called");
    }

    @Override
    public E lower(E element) {
        return this.forward.higher(element);
    }

    @Override
    public E floor(E element) {
        return this.forward.ceiling(element);
    }

    @Override
    public E ceiling(E element) {
        return this.forward.floor(element);
    }

    @Override
    public E higher(E element) {
        return this.forward.lower(element);
    }

    @Override
    int indexOf(@Nullable Object target) {
        int index = this.forward.indexOf(target);
        if (index == -1) {
            return index;
        }
        return this.size() - 1 - index;
    }

    @Override
    boolean isPartialView() {
        return this.forward.isPartialView();
    }
}

