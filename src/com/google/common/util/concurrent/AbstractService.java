/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  javax.annotation.concurrent.GuardedBy
 *  javax.annotation.concurrent.Immutable
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenerCallQueue;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;

@Beta
public abstract class AbstractService
implements Service {
    private static final ListenerCallQueue.Callback<Service.Listener> STARTING_CALLBACK = new ListenerCallQueue.Callback<Service.Listener>("starting()"){

        @Override
        void call(Service.Listener listener) {
            listener.starting();
        }
    };
    private static final ListenerCallQueue.Callback<Service.Listener> RUNNING_CALLBACK = new ListenerCallQueue.Callback<Service.Listener>("running()"){

        @Override
        void call(Service.Listener listener) {
            listener.running();
        }
    };
    private static final ListenerCallQueue.Callback<Service.Listener> STOPPING_FROM_STARTING_CALLBACK = AbstractService.stoppingCallback(Service.State.STARTING);
    private static final ListenerCallQueue.Callback<Service.Listener> STOPPING_FROM_RUNNING_CALLBACK = AbstractService.stoppingCallback(Service.State.RUNNING);
    private static final ListenerCallQueue.Callback<Service.Listener> TERMINATED_FROM_NEW_CALLBACK = AbstractService.terminatedCallback(Service.State.NEW);
    private static final ListenerCallQueue.Callback<Service.Listener> TERMINATED_FROM_RUNNING_CALLBACK = AbstractService.terminatedCallback(Service.State.RUNNING);
    private static final ListenerCallQueue.Callback<Service.Listener> TERMINATED_FROM_STOPPING_CALLBACK = AbstractService.terminatedCallback(Service.State.STOPPING);
    private final Monitor monitor = new Monitor();
    private final Monitor.Guard isStartable = new Monitor.Guard(this.monitor){

        @Override
        public boolean isSatisfied() {
            return AbstractService.this.state() == Service.State.NEW;
        }
    };
    private final Monitor.Guard isStoppable = new Monitor.Guard(this.monitor){

        @Override
        public boolean isSatisfied() {
            return AbstractService.this.state().compareTo(Service.State.RUNNING) <= 0;
        }
    };
    private final Monitor.Guard hasReachedRunning = new Monitor.Guard(this.monitor){

        @Override
        public boolean isSatisfied() {
            return AbstractService.this.state().compareTo(Service.State.RUNNING) >= 0;
        }
    };
    private final Monitor.Guard isStopped = new Monitor.Guard(this.monitor){

        @Override
        public boolean isSatisfied() {
            return AbstractService.this.state().isTerminal();
        }
    };
    @GuardedBy(value="monitor")
    private final List<ListenerCallQueue<Service.Listener>> listeners = Collections.synchronizedList(new ArrayList());
    @GuardedBy(value="monitor")
    private volatile StateSnapshot snapshot = new StateSnapshot(Service.State.NEW);

    private static ListenerCallQueue.Callback<Service.Listener> terminatedCallback(final Service.State from) {
        String string = String.valueOf(String.valueOf((Object)from));
        return new ListenerCallQueue.Callback<Service.Listener>(new StringBuilder(21 + string.length()).append("terminated({from = ").append(string).append("})").toString()){

            @Override
            void call(Service.Listener listener) {
                listener.terminated(from);
            }
        };
    }

    private static ListenerCallQueue.Callback<Service.Listener> stoppingCallback(final Service.State from) {
        String string = String.valueOf(String.valueOf((Object)from));
        return new ListenerCallQueue.Callback<Service.Listener>(new StringBuilder(19 + string.length()).append("stopping({from = ").append(string).append("})").toString()){

            @Override
            void call(Service.Listener listener) {
                listener.stopping(from);
            }
        };
    }

    protected AbstractService() {
    }

    protected abstract void doStart();

    protected abstract void doStop();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final Service startAsync() {
        if (this.monitor.enterIf(this.isStartable)) {
            try {
                this.snapshot = new StateSnapshot(Service.State.STARTING);
                this.starting();
                this.doStart();
            }
            catch (Throwable startupFailure) {
                this.notifyFailed(startupFailure);
            }
            finally {
                this.monitor.leave();
                this.executeListeners();
            }
        } else {
            String startupFailure = String.valueOf(String.valueOf(this));
            throw new IllegalStateException(new StringBuilder(33 + startupFailure.length()).append("Service ").append(startupFailure).append(" has already been started").toString());
        }
        return this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public final Service stopAsync() {
        if (!this.monitor.enterIf(this.isStoppable)) return this;
        try {
            Service.State previous = this.state();
            switch (previous) {
                case NEW: {
                    this.snapshot = new StateSnapshot(Service.State.TERMINATED);
                    this.terminated(Service.State.NEW);
                    return this;
                }
                case STARTING: {
                    this.snapshot = new StateSnapshot(Service.State.STARTING, true, null);
                    this.stopping(Service.State.STARTING);
                    return this;
                }
                case RUNNING: {
                    this.snapshot = new StateSnapshot(Service.State.STOPPING);
                    this.stopping(Service.State.RUNNING);
                    this.doStop();
                    return this;
                }
                case STOPPING: 
                case TERMINATED: 
                case FAILED: {
                    String string = String.valueOf(String.valueOf((Object)((Object)previous)));
                    throw new AssertionError((Object)new StringBuilder(45 + string.length()).append("isStoppable is incorrectly implemented, saw: ").append(string).toString());
                }
                default: {
                    String string = String.valueOf(String.valueOf((Object)((Object)previous)));
                    throw new AssertionError((Object)new StringBuilder(18 + string.length()).append("Unexpected state: ").append(string).toString());
                }
            }
        }
        catch (Throwable shutdownFailure) {
            this.notifyFailed(shutdownFailure);
            return this;
        }
        finally {
            this.monitor.leave();
            this.executeListeners();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void awaitRunning() {
        this.monitor.enterWhenUninterruptibly(this.hasReachedRunning);
        try {
            this.checkCurrentState(Service.State.RUNNING);
        }
        finally {
            this.monitor.leave();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
        if (this.monitor.enterWhenUninterruptibly(this.hasReachedRunning, timeout, unit)) {
            try {
                this.checkCurrentState(Service.State.RUNNING);
            }
            finally {
                this.monitor.leave();
            }
        } else {
            String string = String.valueOf(String.valueOf(this));
            String string2 = String.valueOf(String.valueOf((Object)((Object)this.state())));
            throw new TimeoutException(new StringBuilder(66 + string.length() + string2.length()).append("Timed out waiting for ").append(string).append(" to reach the RUNNING state. ").append("Current state: ").append(string2).toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void awaitTerminated() {
        this.monitor.enterWhenUninterruptibly(this.isStopped);
        try {
            this.checkCurrentState(Service.State.TERMINATED);
        }
        finally {
            this.monitor.leave();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
        if (this.monitor.enterWhenUninterruptibly(this.isStopped, timeout, unit)) {
            try {
                this.checkCurrentState(Service.State.TERMINATED);
            }
            finally {
                this.monitor.leave();
            }
        } else {
            String string = String.valueOf(String.valueOf(this));
            String string2 = String.valueOf(String.valueOf((Object)((Object)this.state())));
            throw new TimeoutException(new StringBuilder(65 + string.length() + string2.length()).append("Timed out waiting for ").append(string).append(" to reach a terminal state. ").append("Current state: ").append(string2).toString());
        }
    }

    @GuardedBy(value="monitor")
    private void checkCurrentState(Service.State expected) {
        Service.State actual = this.state();
        if (actual != expected) {
            if (actual == Service.State.FAILED) {
                String string = String.valueOf(String.valueOf((Object)((Object)expected)));
                throw new IllegalStateException(new StringBuilder(55 + string.length()).append("Expected the service to be ").append(string).append(", but the service has FAILED").toString(), this.failureCause());
            }
            String string = String.valueOf(String.valueOf((Object)((Object)expected)));
            String string2 = String.valueOf(String.valueOf((Object)((Object)actual)));
            throw new IllegalStateException(new StringBuilder(37 + string.length() + string2.length()).append("Expected the service to be ").append(string).append(", but was ").append(string2).toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void notifyStarted() {
        this.monitor.enter();
        try {
            if (this.snapshot.state != Service.State.STARTING) {
                String string = String.valueOf(String.valueOf((Object)((Object)this.snapshot.state)));
                IllegalStateException failure = new IllegalStateException(new StringBuilder(43 + string.length()).append("Cannot notifyStarted() when the service is ").append(string).toString());
                this.notifyFailed(failure);
                throw failure;
            }
            if (this.snapshot.shutdownWhenStartupFinishes) {
                this.snapshot = new StateSnapshot(Service.State.STOPPING);
                this.doStop();
            } else {
                this.snapshot = new StateSnapshot(Service.State.RUNNING);
                this.running();
            }
        }
        finally {
            this.monitor.leave();
            this.executeListeners();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void notifyStopped() {
        this.monitor.enter();
        try {
            Service.State previous = this.snapshot.state;
            if (previous != Service.State.STOPPING && previous != Service.State.RUNNING) {
                String string = String.valueOf(String.valueOf((Object)((Object)previous)));
                IllegalStateException failure = new IllegalStateException(new StringBuilder(43 + string.length()).append("Cannot notifyStopped() when the service is ").append(string).toString());
                this.notifyFailed(failure);
                throw failure;
            }
            this.snapshot = new StateSnapshot(Service.State.TERMINATED);
            this.terminated(previous);
        }
        finally {
            this.monitor.leave();
            this.executeListeners();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected final void notifyFailed(Throwable cause) {
        Preconditions.checkNotNull(cause);
        this.monitor.enter();
        try {
            Service.State previous = this.state();
            switch (previous) {
                case NEW: 
                case TERMINATED: {
                    String string = String.valueOf(String.valueOf((Object)((Object)previous)));
                    throw new IllegalStateException(new StringBuilder(22 + string.length()).append("Failed while in state:").append(string).toString(), cause);
                }
                case STARTING: 
                case RUNNING: 
                case STOPPING: {
                    this.snapshot = new StateSnapshot(Service.State.FAILED, false, cause);
                    this.failed(previous, cause);
                    return;
                }
                case FAILED: {
                    return;
                }
                default: {
                    String string = String.valueOf(String.valueOf((Object)((Object)previous)));
                    throw new AssertionError((Object)new StringBuilder(18 + string.length()).append("Unexpected state: ").append(string).toString());
                }
            }
        }
        finally {
            this.monitor.leave();
            this.executeListeners();
        }
    }

    @Override
    public final boolean isRunning() {
        return this.state() == Service.State.RUNNING;
    }

    @Override
    public final Service.State state() {
        return this.snapshot.externalState();
    }

    @Override
    public final Throwable failureCause() {
        return this.snapshot.failureCause();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void addListener(Service.Listener listener, Executor executor) {
        Preconditions.checkNotNull(listener, "listener");
        Preconditions.checkNotNull(executor, "executor");
        this.monitor.enter();
        try {
            if (!this.state().isTerminal()) {
                this.listeners.add(new ListenerCallQueue<Service.Listener>(listener, executor));
            }
        }
        finally {
            this.monitor.leave();
        }
    }

    public String toString() {
        String string = String.valueOf(String.valueOf(this.getClass().getSimpleName()));
        String string2 = String.valueOf(String.valueOf((Object)((Object)this.state())));
        return new StringBuilder(3 + string.length() + string2.length()).append(string).append(" [").append(string2).append("]").toString();
    }

    private void executeListeners() {
        if (!this.monitor.isOccupiedByCurrentThread()) {
            for (int i = 0; i < this.listeners.size(); ++i) {
                this.listeners.get(i).execute();
            }
        }
    }

    @GuardedBy(value="monitor")
    private void starting() {
        STARTING_CALLBACK.enqueueOn(this.listeners);
    }

    @GuardedBy(value="monitor")
    private void running() {
        RUNNING_CALLBACK.enqueueOn(this.listeners);
    }

    @GuardedBy(value="monitor")
    private void stopping(Service.State from) {
        if (from == Service.State.STARTING) {
            STOPPING_FROM_STARTING_CALLBACK.enqueueOn(this.listeners);
        } else if (from == Service.State.RUNNING) {
            STOPPING_FROM_RUNNING_CALLBACK.enqueueOn(this.listeners);
        } else {
            throw new AssertionError();
        }
    }

    @GuardedBy(value="monitor")
    private void terminated(Service.State from) {
        switch (from) {
            case NEW: {
                TERMINATED_FROM_NEW_CALLBACK.enqueueOn(this.listeners);
                break;
            }
            case RUNNING: {
                TERMINATED_FROM_RUNNING_CALLBACK.enqueueOn(this.listeners);
                break;
            }
            case STOPPING: {
                TERMINATED_FROM_STOPPING_CALLBACK.enqueueOn(this.listeners);
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
    }

    @GuardedBy(value="monitor")
    private void failed(final Service.State from, final Throwable cause) {
        String string = String.valueOf(String.valueOf((Object)((Object)from)));
        String string2 = String.valueOf(String.valueOf(cause));
        new ListenerCallQueue.Callback<Service.Listener>(new StringBuilder(27 + string.length() + string2.length()).append("failed({from = ").append(string).append(", cause = ").append(string2).append("})").toString()){

            @Override
            void call(Service.Listener listener) {
                listener.failed(from, cause);
            }
        }.enqueueOn(this.listeners);
    }

    @Immutable
    private static final class StateSnapshot {
        final Service.State state;
        final boolean shutdownWhenStartupFinishes;
        @Nullable
        final Throwable failure;

        StateSnapshot(Service.State internalState) {
            this(internalState, false, null);
        }

        StateSnapshot(Service.State internalState, boolean shutdownWhenStartupFinishes, @Nullable Throwable failure) {
            Preconditions.checkArgument(!shutdownWhenStartupFinishes || internalState == Service.State.STARTING, "shudownWhenStartupFinishes can only be set if state is STARTING. Got %s instead.", new Object[]{internalState});
            Preconditions.checkArgument(!(failure != null ^ internalState == Service.State.FAILED), "A failure cause should be set if and only if the state is failed.  Got %s and %s instead.", new Object[]{internalState, failure});
            this.state = internalState;
            this.shutdownWhenStartupFinishes = shutdownWhenStartupFinishes;
            this.failure = failure;
        }

        Service.State externalState() {
            if (this.shutdownWhenStartupFinishes && this.state == Service.State.STARTING) {
                return Service.State.STOPPING;
            }
            return this.state;
        }

        Throwable failureCause() {
            Preconditions.checkState(this.state == Service.State.FAILED, "failureCause() is only valid if the service has failed, service is %s", new Object[]{this.state});
            return this.failure;
        }
    }

}

