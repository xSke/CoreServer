/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
interface Constraint<E> {
    public E checkElement(E var1);

    public String toString();
}

