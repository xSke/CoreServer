/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.base;

import com.google.common.base.FinalizableReference;
import com.google.common.base.FinalizableReferenceQueue;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public abstract class FinalizablePhantomReference<T>
extends PhantomReference<T>
implements FinalizableReference {
    protected FinalizablePhantomReference(T referent, FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}

