/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.concurrent;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class PromiseNotifier<V, F extends Future<V>>
implements GenericFutureListener<F> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PromiseNotifier.class);
    private final Promise<? super V>[] promises;

    @SafeVarargs
    public /* varargs */ PromiseNotifier(Promise<? super V> ... promises) {
        ObjectUtil.checkNotNull(promises, "promises");
        for (Promise<? super V> promise : promises) {
            if (promise != null) continue;
            throw new IllegalArgumentException("promises contains null Promise");
        }
        this.promises = (Promise[])promises.clone();
    }

    @Override
    public void operationComplete(F future) throws Exception {
        if (future.isSuccess()) {
            Object result = future.get();
            for (Promise p : this.promises) {
                if (p.trySuccess(result)) continue;
                logger.warn("Failed to mark a promise as success because it is done already: {}", (Object)p);
            }
            return;
        }
        Throwable cause = future.cause();
        for (Promise<V> p : this.promises) {
            if (p.tryFailure(cause)) continue;
            logger.warn("Failed to mark a promise as failure because it's done already: {}", (Object)p, (Object)cause);
        }
    }
}

