/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal.chmv8;

import io.netty.util.internal.chmv8.ForkJoinTask;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.misc.Unsafe;

public abstract class CountedCompleter<T>
extends ForkJoinTask<T> {
    private static final long serialVersionUID = 5232453752276485070L;
    final CountedCompleter<?> completer;
    volatile int pending;
    private static final Unsafe U;
    private static final long PENDING;

    protected CountedCompleter(CountedCompleter<?> completer, int initialPendingCount) {
        this.completer = completer;
        this.pending = initialPendingCount;
    }

    protected CountedCompleter(CountedCompleter<?> completer) {
        this.completer = completer;
    }

    protected CountedCompleter() {
        this.completer = null;
    }

    public abstract void compute();

    public void onCompletion(CountedCompleter<?> caller) {
    }

    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        return true;
    }

    public final CountedCompleter<?> getCompleter() {
        return this.completer;
    }

    public final int getPendingCount() {
        return this.pending;
    }

    public final void setPendingCount(int count) {
        this.pending = count;
    }

    public final void addToPendingCount(int delta) {
        int c;
        while (!U.compareAndSwapInt(this, PENDING, c = this.pending, c + delta)) {
        }
    }

    public final boolean compareAndSetPendingCount(int expected, int count) {
        return U.compareAndSwapInt(this, PENDING, expected, count);
    }

    public final int decrementPendingCountUnlessZero() {
        int c;
        while ((c = this.pending) != 0 && !U.compareAndSwapInt(this, PENDING, c, c - 1)) {
        }
        return c;
    }

    public final CountedCompleter<?> getRoot() {
        CountedCompleter<?> p;
        CountedCompleter<?> a = this;
        while ((p = a.completer) != null) {
            a = p;
        }
        return a;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public final void tryComplete() {
        s = a = this;
        do lbl-1000: // 3 sources:
        {
            if ((c = a.pending) != 0) continue;
            a.onCompletion(s);
            s = a;
            a = s.completer;
            if (a != null) ** GOTO lbl-1000
            s.quietlyComplete();
            return;
        } while (!CountedCompleter.U.compareAndSwapInt(a, CountedCompleter.PENDING, c, c - 1));
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public final void propagateCompletion() {
        s = a = this;
        do lbl-1000: // 3 sources:
        {
            if ((c = a.pending) != 0) continue;
            s = a;
            a = s.completer;
            if (a != null) ** GOTO lbl-1000
            s.quietlyComplete();
            return;
        } while (!CountedCompleter.U.compareAndSwapInt(a, CountedCompleter.PENDING, c, c - 1));
    }

    @Override
    public void complete(T rawResult) {
        this.setRawResult(rawResult);
        this.onCompletion(this);
        this.quietlyComplete();
        CountedCompleter<?> p = this.completer;
        if (p != null) {
            p.tryComplete();
        }
    }

    public final CountedCompleter<?> firstComplete() {
        int c;
        do {
            if ((c = this.pending) != 0) continue;
            return this;
        } while (!U.compareAndSwapInt(this, PENDING, c, c - 1));
        return null;
    }

    public final CountedCompleter<?> nextComplete() {
        CountedCompleter<?> p = this.completer;
        if (p != null) {
            return p.firstComplete();
        }
        this.quietlyComplete();
        return null;
    }

    public final void quietlyCompleteRoot() {
        CountedCompleter<?> a = this;
        do {
            CountedCompleter<?> p;
            if ((p = a.completer) == null) {
                a.quietlyComplete();
                return;
            }
            a = p;
        } while (true);
    }

    @Override
    void internalPropagateException(Throwable ex) {
        CountedCompleter<?> a;
        CountedCompleter<?> s = a = this;
        while (a.onExceptionalCompletion(ex, s)) {
            s = a;
            a = s.completer;
            if (a != null && a.status >= 0 && a.recordExceptionalCompletion(ex) == Integer.MIN_VALUE) continue;
        }
    }

    @Override
    protected final boolean exec() {
        this.compute();
        return false;
    }

    @Override
    public T getRawResult() {
        return null;
    }

    @Override
    protected void setRawResult(T t) {
    }

    private static Unsafe getUnsafe() {
        try {
            return Unsafe.getUnsafe();
        }
        catch (SecurityException securityException) {
            try {
                return (Unsafe)AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>(){

                    @Override
                    public Unsafe run() throws Exception {
                        Class<Unsafe> k = Unsafe.class;
                        for (Field f : k.getDeclaredFields()) {
                            f.setAccessible(true);
                            Object x = f.get(null);
                            if (!k.isInstance(x)) continue;
                            return k.cast(x);
                        }
                        throw new NoSuchFieldError("the Unsafe");
                    }
                });
            }
            catch (PrivilegedActionException e) {
                throw new RuntimeException("Could not initialize intrinsics", e.getCause());
            }
        }
    }

    static {
        try {
            U = CountedCompleter.getUnsafe();
            PENDING = U.objectFieldOffset(CountedCompleter.class.getDeclaredField("pending"));
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

}

