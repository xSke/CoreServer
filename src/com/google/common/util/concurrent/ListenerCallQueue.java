/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.concurrent.GuardedBy
 */
package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class ListenerCallQueue<L>
implements Runnable {
    private static final Logger logger = Logger.getLogger(ListenerCallQueue.class.getName());
    private final L listener;
    private final Executor executor;
    @GuardedBy(value="this")
    private final Queue<Callback<L>> waitQueue = Queues.newArrayDeque();
    @GuardedBy(value="this")
    private boolean isThreadScheduled;

    ListenerCallQueue(L listener, Executor executor) {
        this.listener = Preconditions.checkNotNull(listener);
        this.executor = Preconditions.checkNotNull(executor);
    }

    synchronized void add(Callback<L> callback) {
        this.waitQueue.add(callback);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void execute() {
        boolean scheduleTaskRunner = false;
        ListenerCallQueue listenerCallQueue = this;
        synchronized (listenerCallQueue) {
            if (!this.isThreadScheduled) {
                this.isThreadScheduled = true;
                scheduleTaskRunner = true;
            }
        }
        if (scheduleTaskRunner) {
            try {
                this.executor.execute(this);
            }
            catch (RuntimeException e) {
                Object object = this;
                synchronized (object) {
                    this.isThreadScheduled = false;
                }
                object = String.valueOf(String.valueOf(this.listener));
                String string = String.valueOf(String.valueOf(this.executor));
                logger.log(Level.SEVERE, new StringBuilder(42 + object.length() + string.length()).append("Exception while running callbacks for ").append((String)object).append(" on ").append(string).toString(), e);
                throw e;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        block18 : {
            Object nextToRun;
            boolean stillRunning = true;
            block14 : do {
                do {
                    ListenerCallQueue listenerCallQueue = this;
                    synchronized (listenerCallQueue) {
                        Preconditions.checkState(this.isThreadScheduled);
                        nextToRun = this.waitQueue.poll();
                        if (nextToRun == null) {
                            this.isThreadScheduled = false;
                            stillRunning = false;
                            break block18;
                        }
                    }
                    try {
                        nextToRun.call(this.listener);
                        continue block14;
                    }
                    catch (RuntimeException e) {
                        String string = String.valueOf(String.valueOf(this.listener));
                        String string2 = String.valueOf(String.valueOf(((Callback)nextToRun).methodCall));
                        logger.log(Level.SEVERE, new StringBuilder(37 + string.length() + string2.length()).append("Exception while executing callback: ").append(string).append(".").append(string2).toString(), e);
                        continue;
                    }
                    break;
                } while (true);
            } while (true);
            finally {
                if (stillRunning) {
                    nextToRun = this;
                    synchronized (nextToRun) {
                        this.isThreadScheduled = false;
                    }
                }
            }
        }
    }

    static abstract class Callback<L> {
        private final String methodCall;

        Callback(String methodCall) {
            this.methodCall = methodCall;
        }

        abstract void call(L var1);

        void enqueueOn(Iterable<ListenerCallQueue<L>> queues) {
            for (ListenerCallQueue<L> queue : queues) {
                queue.add(this);
            }
        }
    }

}

