/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.concurrent.ThreadSafe
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.SmoothRateLimiter;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Beta
public abstract class RateLimiter {
    private final SleepingStopwatch stopwatch;
    private volatile Object mutexDoNotUseDirectly;

    public static RateLimiter create(double permitsPerSecond) {
        return RateLimiter.create(SleepingStopwatch.createFromSystemTimer(), permitsPerSecond);
    }

    @VisibleForTesting
    static RateLimiter create(SleepingStopwatch stopwatch, double permitsPerSecond) {
        SmoothRateLimiter.SmoothBursty rateLimiter = new SmoothRateLimiter.SmoothBursty(stopwatch, 1.0);
        rateLimiter.setRate(permitsPerSecond);
        return rateLimiter;
    }

    public static RateLimiter create(double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
        Preconditions.checkArgument(warmupPeriod >= 0L, "warmupPeriod must not be negative: %s", warmupPeriod);
        return RateLimiter.create(SleepingStopwatch.createFromSystemTimer(), permitsPerSecond, warmupPeriod, unit);
    }

    @VisibleForTesting
    static RateLimiter create(SleepingStopwatch stopwatch, double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
        SmoothRateLimiter.SmoothWarmingUp rateLimiter = new SmoothRateLimiter.SmoothWarmingUp(stopwatch, warmupPeriod, unit);
        rateLimiter.setRate(permitsPerSecond);
        return rateLimiter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Object mutex() {
        Object mutex = this.mutexDoNotUseDirectly;
        if (mutex == null) {
            RateLimiter rateLimiter = this;
            synchronized (rateLimiter) {
                mutex = this.mutexDoNotUseDirectly;
                if (mutex == null) {
                    this.mutexDoNotUseDirectly = mutex = new Object();
                }
            }
        }
        return mutex;
    }

    RateLimiter(SleepingStopwatch stopwatch) {
        this.stopwatch = Preconditions.checkNotNull(stopwatch);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setRate(double permitsPerSecond) {
        Preconditions.checkArgument(permitsPerSecond > 0.0 && !Double.isNaN(permitsPerSecond), "rate must be positive");
        Object object = this.mutex();
        synchronized (object) {
            this.doSetRate(permitsPerSecond, this.stopwatch.readMicros());
        }
    }

    abstract void doSetRate(double var1, long var3);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final double getRate() {
        Object object = this.mutex();
        synchronized (object) {
            return this.doGetRate();
        }
    }

    abstract double doGetRate();

    public double acquire() {
        return this.acquire(1);
    }

    public double acquire(int permits) {
        long microsToWait = this.reserve(permits);
        this.stopwatch.sleepMicrosUninterruptibly(microsToWait);
        return 1.0 * (double)microsToWait / (double)TimeUnit.SECONDS.toMicros(1L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final long reserve(int permits) {
        RateLimiter.checkPermits(permits);
        Object object = this.mutex();
        synchronized (object) {
            return this.reserveAndGetWaitLength(permits, this.stopwatch.readMicros());
        }
    }

    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return this.tryAcquire(1, timeout, unit);
    }

    public boolean tryAcquire(int permits) {
        return this.tryAcquire(permits, 0L, TimeUnit.MICROSECONDS);
    }

    public boolean tryAcquire() {
        return this.tryAcquire(1, 0L, TimeUnit.MICROSECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        long microsToWait;
        long timeoutMicros = Math.max(unit.toMicros(timeout), 0L);
        RateLimiter.checkPermits(permits);
        Object object = this.mutex();
        synchronized (object) {
            long nowMicros = this.stopwatch.readMicros();
            if (!this.canAcquire(nowMicros, timeoutMicros)) {
                return false;
            }
            microsToWait = this.reserveAndGetWaitLength(permits, nowMicros);
        }
        this.stopwatch.sleepMicrosUninterruptibly(microsToWait);
        return true;
    }

    private boolean canAcquire(long nowMicros, long timeoutMicros) {
        return this.queryEarliestAvailable(nowMicros) - timeoutMicros <= nowMicros;
    }

    final long reserveAndGetWaitLength(int permits, long nowMicros) {
        long momentAvailable = this.reserveEarliestAvailable(permits, nowMicros);
        return Math.max(momentAvailable - nowMicros, 0L);
    }

    abstract long queryEarliestAvailable(long var1);

    abstract long reserveEarliestAvailable(int var1, long var2);

    public String toString() {
        return String.format("RateLimiter[stableRate=%3.1fqps]", this.getRate());
    }

    private static int checkPermits(int permits) {
        Preconditions.checkArgument(permits > 0, "Requested permits (%s) must be positive", permits);
        return permits;
    }

    @VisibleForTesting
    static abstract class SleepingStopwatch {
        SleepingStopwatch() {
        }

        abstract long readMicros();

        abstract void sleepMicrosUninterruptibly(long var1);

        static final SleepingStopwatch createFromSystemTimer() {
            return new SleepingStopwatch(){
                final Stopwatch stopwatch = Stopwatch.createStarted();

                @Override
                long readMicros() {
                    return this.stopwatch.elapsed(TimeUnit.MICROSECONDS);
                }

                @Override
                void sleepMicrosUninterruptibly(long micros) {
                    if (micros > 0L) {
                        Uninterruptibles.sleepUninterruptibly(micros, TimeUnit.MICROSECONDS);
                    }
                }
            };
        }

    }

}

