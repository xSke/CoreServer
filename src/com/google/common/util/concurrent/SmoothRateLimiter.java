/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.util.concurrent;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.TimeUnit;

abstract class SmoothRateLimiter
extends RateLimiter {
    double storedPermits;
    double maxPermits;
    double stableIntervalMicros;
    private long nextFreeTicketMicros = 0L;

    private SmoothRateLimiter(RateLimiter.SleepingStopwatch stopwatch) {
        super(stopwatch);
    }

    @Override
    final void doSetRate(double permitsPerSecond, long nowMicros) {
        double stableIntervalMicros;
        this.resync(nowMicros);
        this.stableIntervalMicros = stableIntervalMicros = (double)TimeUnit.SECONDS.toMicros(1L) / permitsPerSecond;
        this.doSetRate(permitsPerSecond, stableIntervalMicros);
    }

    abstract void doSetRate(double var1, double var3);

    @Override
    final double doGetRate() {
        return (double)TimeUnit.SECONDS.toMicros(1L) / this.stableIntervalMicros;
    }

    @Override
    final long queryEarliestAvailable(long nowMicros) {
        return this.nextFreeTicketMicros;
    }

    @Override
    final long reserveEarliestAvailable(int requiredPermits, long nowMicros) {
        this.resync(nowMicros);
        long returnValue = this.nextFreeTicketMicros;
        double storedPermitsToSpend = Math.min((double)requiredPermits, this.storedPermits);
        double freshPermits = (double)requiredPermits - storedPermitsToSpend;
        long waitMicros = this.storedPermitsToWaitTime(this.storedPermits, storedPermitsToSpend) + (long)(freshPermits * this.stableIntervalMicros);
        this.nextFreeTicketMicros += waitMicros;
        this.storedPermits -= storedPermitsToSpend;
        return returnValue;
    }

    abstract long storedPermitsToWaitTime(double var1, double var3);

    private void resync(long nowMicros) {
        if (nowMicros > this.nextFreeTicketMicros) {
            this.storedPermits = Math.min(this.maxPermits, this.storedPermits + (double)(nowMicros - this.nextFreeTicketMicros) / this.stableIntervalMicros);
            this.nextFreeTicketMicros = nowMicros;
        }
    }

    static final class SmoothBursty
    extends SmoothRateLimiter {
        final double maxBurstSeconds;

        SmoothBursty(RateLimiter.SleepingStopwatch stopwatch, double maxBurstSeconds) {
            super(stopwatch);
            this.maxBurstSeconds = maxBurstSeconds;
        }

        @Override
        void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
            double oldMaxPermits = this.maxPermits;
            this.maxPermits = this.maxBurstSeconds * permitsPerSecond;
            this.storedPermits = oldMaxPermits == Double.POSITIVE_INFINITY ? this.maxPermits : (oldMaxPermits == 0.0 ? 0.0 : this.storedPermits * this.maxPermits / oldMaxPermits);
        }

        @Override
        long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
            return 0L;
        }
    }

    static final class SmoothWarmingUp
    extends SmoothRateLimiter {
        private final long warmupPeriodMicros;
        private double slope;
        private double halfPermits;

        SmoothWarmingUp(RateLimiter.SleepingStopwatch stopwatch, long warmupPeriod, TimeUnit timeUnit) {
            super(stopwatch);
            this.warmupPeriodMicros = timeUnit.toMicros(warmupPeriod);
        }

        @Override
        void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
            double oldMaxPermits = this.maxPermits;
            this.maxPermits = (double)this.warmupPeriodMicros / stableIntervalMicros;
            this.halfPermits = this.maxPermits / 2.0;
            double coldIntervalMicros = stableIntervalMicros * 3.0;
            this.slope = (coldIntervalMicros - stableIntervalMicros) / this.halfPermits;
            this.storedPermits = oldMaxPermits == Double.POSITIVE_INFINITY ? 0.0 : (oldMaxPermits == 0.0 ? this.maxPermits : this.storedPermits * this.maxPermits / oldMaxPermits);
        }

        @Override
        long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
            double availablePermitsAboveHalf = storedPermits - this.halfPermits;
            long micros = 0L;
            if (availablePermitsAboveHalf > 0.0) {
                double permitsAboveHalfToTake = Math.min(availablePermitsAboveHalf, permitsToTake);
                micros = (long)(permitsAboveHalfToTake * (this.permitsToTime(availablePermitsAboveHalf) + this.permitsToTime(availablePermitsAboveHalf - permitsAboveHalfToTake)) / 2.0);
                permitsToTake -= permitsAboveHalfToTake;
            }
            micros = (long)((double)micros + this.stableIntervalMicros * permitsToTake);
            return micros;
        }

        private double permitsToTime(double permits) {
            return this.stableIntervalMicros + permits * this.slope;
        }
    }

}

