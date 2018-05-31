/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Beta
public abstract class AbstractIdleService
implements Service {
    private final Supplier<String> threadNameSupplier = new Supplier<String>(){

        @Override
        public String get() {
            String string = String.valueOf(String.valueOf(AbstractIdleService.this.serviceName()));
            String string2 = String.valueOf(String.valueOf((Object)AbstractIdleService.this.state()));
            return new StringBuilder(1 + string.length() + string2.length()).append(string).append(" ").append(string2).toString();
        }
    };
    private final Service delegate = new AbstractService(){

        @Override
        protected final void doStart() {
            MoreExecutors.renamingDecorator(AbstractIdleService.this.executor(), AbstractIdleService.this.threadNameSupplier).execute(new Runnable(){

                @Override
                public void run() {
                    try {
                        AbstractIdleService.this.startUp();
                        2.this.notifyStarted();
                    }
                    catch (Throwable t) {
                        2.this.notifyFailed(t);
                        throw Throwables.propagate(t);
                    }
                }
            });
        }

        @Override
        protected final void doStop() {
            MoreExecutors.renamingDecorator(AbstractIdleService.this.executor(), AbstractIdleService.this.threadNameSupplier).execute(new Runnable(){

                @Override
                public void run() {
                    try {
                        AbstractIdleService.this.shutDown();
                        2.this.notifyStopped();
                    }
                    catch (Throwable t) {
                        2.this.notifyFailed(t);
                        throw Throwables.propagate(t);
                    }
                }
            });
        }

    };

    protected AbstractIdleService() {
    }

    protected abstract void startUp() throws Exception;

    protected abstract void shutDown() throws Exception;

    protected Executor executor() {
        return new Executor(){

            @Override
            public void execute(Runnable command) {
                MoreExecutors.newThread((String)AbstractIdleService.this.threadNameSupplier.get(), command).start();
            }
        };
    }

    public String toString() {
        String string = String.valueOf(String.valueOf(this.serviceName()));
        String string2 = String.valueOf(String.valueOf((Object)((Object)this.state())));
        return new StringBuilder(3 + string.length() + string2.length()).append(string).append(" [").append(string2).append("]").toString();
    }

    @Override
    public final boolean isRunning() {
        return this.delegate.isRunning();
    }

    @Override
    public final Service.State state() {
        return this.delegate.state();
    }

    @Override
    public final void addListener(Service.Listener listener, Executor executor) {
        this.delegate.addListener(listener, executor);
    }

    @Override
    public final Throwable failureCause() {
        return this.delegate.failureCause();
    }

    @Override
    public final Service startAsync() {
        this.delegate.startAsync();
        return this;
    }

    @Override
    public final Service stopAsync() {
        this.delegate.stopAsync();
        return this;
    }

    @Override
    public final void awaitRunning() {
        this.delegate.awaitRunning();
    }

    @Override
    public final void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
        this.delegate.awaitRunning(timeout, unit);
    }

    @Override
    public final void awaitTerminated() {
        this.delegate.awaitTerminated();
    }

    @Override
    public final void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
        this.delegate.awaitTerminated(timeout, unit);
    }

    protected String serviceName() {
        return this.getClass().getSimpleName();
    }

}

