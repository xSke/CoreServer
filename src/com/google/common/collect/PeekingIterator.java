/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;

@GwtCompatible
public interface PeekingIterator<E>
extends Iterator<E> {
    public E peek();

    @Override
    public E next();

    @Override
    public void remove();
}

