/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.base;

import com.google.common.base.FinalizableReference;
import com.google.common.base.FinalizableReferenceQueue;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public abstract class FinalizableWeakReference<T>
extends WeakReference<T>
implements FinalizableReference {
    protected FinalizableWeakReference(T referent, FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}

