/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.concurrent.GuardedBy
 */
package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class SerializingExecutor
implements Executor {
    private static final Logger log = Logger.getLogger(SerializingExecutor.class.getName());
    private final Executor executor;
    @GuardedBy(value="internalLock")
    private final Queue<Runnable> waitQueue = new ArrayDeque<Runnable>();
    @GuardedBy(value="internalLock")
    private boolean isThreadScheduled = false;
    private final TaskRunner taskRunner = new TaskRunner();
    private final Object internalLock = new Object(){

        public String toString() {
            String string = String.valueOf(super.toString());
            return string.length() != 0 ? "SerializingExecutor lock: ".concat(string) : new String("SerializingExecutor lock: ");
        }
    };

    public SerializingExecutor(Executor executor) {
        Preconditions.checkNotNull(executor, "'executor' must not be null.");
        this.executor = executor;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute(Runnable r) {
        Preconditions.checkNotNull(r, "'r' must not be null.");
        boolean scheduleTaskRunner = false;
        Object object = this.internalLock;
        synchronized (object) {
            this.waitQueue.add(r);
            if (!this.isThreadScheduled) {
                this.isThreadScheduled = true;
                scheduleTaskRunner = true;
            }
        }
        if (scheduleTaskRunner) {
            boolean threw = true;
            try {
                this.executor.execute(this.taskRunner);
                threw = false;
            }
            finally {
                if (threw) {
                    Object object2 = this.internalLock;
                    synchronized (object2) {
                        this.isThreadScheduled = false;
                    }
                }
            }
        }
    }

    private class TaskRunner
    implements Runnable {
        private TaskRunner() {
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
                        Preconditions.checkState(SerializingExecutor.this.isThreadScheduled);
                        Object object = SerializingExecutor.this.internalLock;
                        synchronized (object) {
                            nextToRun = (Runnable)SerializingExecutor.this.waitQueue.poll();
                            if (nextToRun == null) {
                                SerializingExecutor.this.isThreadScheduled = false;
                                stillRunning = false;
                                break block18;
                            }
                        }
                        try {
                            nextToRun.run();
                            continue block14;
                        }
                        catch (RuntimeException e) {
                            String string = String.valueOf(String.valueOf(nextToRun));
                            log.log(Level.SEVERE, new StringBuilder(35 + string.length()).append("Exception while executing runnable ").append(string).toString(), e);
                            continue;
                        }
                        break;
                    } while (true);
                } while (true);
                finally {
                    if (stillRunning) {
                        nextToRun = SerializingExecutor.this.internalLock;
                        synchronized (nextToRun) {
                            SerializingExecutor.this.isThreadScheduled = false;
                        }
                    }
                }
            }
        }
    }

}

