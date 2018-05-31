/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.util.AbstractList;
import java.util.List;

final class DelegatingList<E>
extends AbstractList<E> {
    final List<E> delegate;

    DelegatingList(List<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public E get(int index) {
        return this.delegate.get(index);
    }
}

