/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.pool;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.annotation.Immutable;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.pool.PoolEntry;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
class LeaseRequest<T, C, E extends PoolEntry<T, C>> {
    private final T route;
    private final Object state;
    private final long connectTimeout;
    private final long deadline;
    private final BasicFuture<E> future;
    private final AtomicBoolean completed;
    private volatile E result;
    private volatile Exception ex;

    public LeaseRequest(T route, Object state, long connectTimeout, long leaseTimeout, BasicFuture<E> future) {
        this.route = route;
        this.state = state;
        this.connectTimeout = connectTimeout;
        this.deadline = leaseTimeout > 0L ? System.currentTimeMillis() + leaseTimeout : Long.MAX_VALUE;
        this.future = future;
        this.completed = new AtomicBoolean(false);
    }

    public T getRoute() {
        return this.route;
    }

    public Object getState() {
        return this.state;
    }

    public long getConnectTimeout() {
        return this.connectTimeout;
    }

    public long getDeadline() {
        return this.deadline;
    }

    public boolean isDone() {
        return this.completed.get();
    }

    public void failed(Exception ex) {
        if (this.completed.compareAndSet(false, true)) {
            this.ex = ex;
        }
    }

    public void completed(E result) {
        if (this.completed.compareAndSet(false, true)) {
            this.result = result;
        }
    }

    public BasicFuture<E> getFuture() {
        return this.future;
    }

    public E getResult() {
        return this.result;
    }

    public Exception getException() {
        return this.ex;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        buffer.append(this.route);
        buffer.append("][");
        buffer.append(this.state);
        buffer.append("]");
        return buffer.toString();
    }
}

