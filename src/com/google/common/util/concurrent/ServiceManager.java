/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.concurrent.GuardedBy
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ListenerCallQueue;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

@Beta
public final class ServiceManager {
    private static final Logger logger = Logger.getLogger(ServiceManager.class.getName());
    private static final ListenerCallQueue.Callback<Listener> HEALTHY_CALLBACK = new ListenerCallQueue.Callback<Listener>("healthy()"){

        @Override
        void call(Listener listener) {
            listener.healthy();
        }
    };
    private static final ListenerCallQueue.Callback<Listener> STOPPED_CALLBACK = new ListenerCallQueue.Callback<Listener>("stopped()"){

        @Override
        void call(Listener listener) {
            listener.stopped();
        }
    };
    private final ServiceManagerState state;
    private final ImmutableList<Service> services;

    public ServiceManager(Iterable<? extends Service> services) {
        ImmutableList<Service> copy = ImmutableList.copyOf(services);
        if (copy.isEmpty()) {
            logger.log(Level.WARNING, "ServiceManager configured with no services.  Is your application configured properly?", new EmptyServiceManagerWarning());
            copy = ImmutableList.of(new NoOpService());
        }
        this.state = new ServiceManagerState(copy);
        this.services = copy;
        WeakReference<ServiceManagerState> stateReference = new WeakReference<ServiceManagerState>(this.state);
        for (Service service : copy) {
            service.addListener(new ServiceListener(service, stateReference), MoreExecutors.directExecutor());
            Preconditions.checkArgument(service.state() == Service.State.NEW, "Can only manage NEW services, %s", service);
        }
        this.state.markReady();
    }

    public void addListener(Listener listener, Executor executor) {
        this.state.addListener(listener, executor);
    }

    public void addListener(Listener listener) {
        this.state.addListener(listener, MoreExecutors.directExecutor());
    }

    public ServiceManager startAsync() {
        for (Service service : this.services) {
            Service.State state = service.state();
            Preconditions.checkState(state == Service.State.NEW, "Service %s is %s, cannot start it.", new Object[]{service, state});
        }
        for (Service service : this.services) {
            try {
                this.state.tryStartTiming(service);
                service.startAsync();
            }
            catch (IllegalStateException e) {
                String string = String.valueOf(String.valueOf(service));
                logger.log(Level.WARNING, new StringBuilder(24 + string.length()).append("Unable to start Service ").append(string).toString(), e);
            }
        }
        return this;
    }

    public void awaitHealthy() {
        this.state.awaitHealthy();
    }

    public void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
        this.state.awaitHealthy(timeout, unit);
    }

    public ServiceManager stopAsync() {
        for (Service service : this.services) {
            service.stopAsync();
        }
        return this;
    }

    public void awaitStopped() {
        this.state.awaitStopped();
    }

    public void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
        this.state.awaitStopped(timeout, unit);
    }

    public boolean isHealthy() {
        for (Service service : this.services) {
            if (service.isRunning()) continue;
            return false;
        }
        return true;
    }

    public ImmutableMultimap<Service.State, Service> servicesByState() {
        return this.state.servicesByState();
    }

    public ImmutableMap<Service, Long> startupTimes() {
        return this.state.startupTimes();
    }

    public String toString() {
        return MoreObjects.toStringHelper(ServiceManager.class).add("services", Collections2.filter(this.services, Predicates.not(Predicates.instanceOf(NoOpService.class)))).toString();
    }

    private static final class EmptyServiceManagerWarning
    extends Throwable {
        private EmptyServiceManagerWarning() {
        }
    }

    private static final class NoOpService
    extends AbstractService {
        private NoOpService() {
        }

        @Override
        protected void doStart() {
            this.notifyStarted();
        }

        @Override
        protected void doStop() {
            this.notifyStopped();
        }
    }

    private static final class ServiceListener
    extends Service.Listener {
        final Service service;
        final WeakReference<ServiceManagerState> state;

        ServiceListener(Service service, WeakReference<ServiceManagerState> state) {
            this.service = service;
            this.state = state;
        }

        @Override
        public void starting() {
            ServiceManagerState state = this.state.get();
            if (state != null) {
                state.transitionService(this.service, Service.State.NEW, Service.State.STARTING);
                if (!(this.service instanceof NoOpService)) {
                    logger.log(Level.FINE, "Starting {0}.", this.service);
                }
            }
        }

        @Override
        public void running() {
            ServiceManagerState state = this.state.get();
            if (state != null) {
                state.transitionService(this.service, Service.State.STARTING, Service.State.RUNNING);
            }
        }

        @Override
        public void stopping(Service.State from) {
            ServiceManagerState state = this.state.get();
            if (state != null) {
                state.transitionService(this.service, from, Service.State.STOPPING);
            }
        }

        @Override
        public void terminated(Service.State from) {
            ServiceManagerState state = this.state.get();
            if (state != null) {
                if (!(this.service instanceof NoOpService)) {
                    logger.log(Level.FINE, "Service {0} has terminated. Previous state was: {1}", new Object[]{this.service, from});
                }
                state.transitionService(this.service, from, Service.State.TERMINATED);
            }
        }

        @Override
        public void failed(Service.State from, Throwable failure) {
            ServiceManagerState state = this.state.get();
            if (state != null) {
                if (!(this.service instanceof NoOpService)) {
                    String string = String.valueOf(String.valueOf(this.service));
                    String string2 = String.valueOf(String.valueOf((Object)from));
                    logger.log(Level.SEVERE, new StringBuilder(34 + string.length() + string2.length()).append("Service ").append(string).append(" has failed in the ").append(string2).append(" state.").toString(), failure);
                }
                state.transitionService(this.service, from, Service.State.FAILED);
            }
        }
    }

    private static final class ServiceManagerState {
        final Monitor monitor = new Monitor();
        @GuardedBy(value="monitor")
        final SetMultimap<Service.State, Service> servicesByState = Multimaps.newSetMultimap(new EnumMap(Service.State.class), new Supplier<Set<Service>>(){

            @Override
            public Set<Service> get() {
                return Sets.newLinkedHashSet();
            }
        });
        @GuardedBy(value="monitor")
        final Multiset<Service.State> states = this.servicesByState.keys();
        @GuardedBy(value="monitor")
        final Map<Service, Stopwatch> startupTimers = Maps.newIdentityHashMap();
        @GuardedBy(value="monitor")
        boolean ready;
        @GuardedBy(value="monitor")
        boolean transitioned;
        final int numberOfServices;
        final Monitor.Guard awaitHealthGuard = new Monitor.Guard(this.monitor){

            @Override
            public boolean isSatisfied() {
                return ServiceManagerState.this.states.count((Object)Service.State.RUNNING) == ServiceManagerState.this.numberOfServices || ServiceManagerState.this.states.contains((Object)Service.State.STOPPING) || ServiceManagerState.this.states.contains((Object)Service.State.TERMINATED) || ServiceManagerState.this.states.contains((Object)Service.State.FAILED);
            }
        };
        final Monitor.Guard stoppedGuard = new Monitor.Guard(this.monitor){

            @Override
            public boolean isSatisfied() {
                return ServiceManagerState.this.states.count((Object)Service.State.TERMINATED) + ServiceManagerState.this.states.count((Object)Service.State.FAILED) == ServiceManagerState.this.numberOfServices;
            }
        };
        @GuardedBy(value="monitor")
        final List<ListenerCallQueue<Listener>> listeners = Collections.synchronizedList(new ArrayList());

        ServiceManagerState(ImmutableCollection<Service> services) {
            this.numberOfServices = services.size();
            this.servicesByState.putAll(Service.State.NEW, services);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void tryStartTiming(Service service) {
            this.monitor.enter();
            try {
                Stopwatch stopwatch = this.startupTimers.get(service);
                if (stopwatch == null) {
                    this.startupTimers.put(service, Stopwatch.createStarted());
                }
            }
            finally {
                this.monitor.leave();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void markReady() {
            block5 : {
                this.monitor.enter();
                try {
                    Object service2;
                    if (!this.transitioned) {
                        this.ready = true;
                        break block5;
                    }
                    ArrayList<Service> servicesInBadStates = Lists.newArrayList();
                    for (Object service2 : this.servicesByState().values()) {
                        if (service2.state() == Service.State.NEW) continue;
                        servicesInBadStates.add((Service)service2);
                    }
                    String i$ = String.valueOf(String.valueOf("Services started transitioning asynchronously before the ServiceManager was constructed: "));
                    service2 = String.valueOf(String.valueOf(servicesInBadStates));
                    throw new IllegalArgumentException(new StringBuilder(0 + i$.length() + service2.length()).append(i$).append((String)service2).toString());
                }
                finally {
                    this.monitor.leave();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void addListener(Listener listener, Executor executor) {
            Preconditions.checkNotNull(listener, "listener");
            Preconditions.checkNotNull(executor, "executor");
            this.monitor.enter();
            try {
                if (!this.stoppedGuard.isSatisfied()) {
                    this.listeners.add(new ListenerCallQueue<Listener>(listener, executor));
                }
            }
            finally {
                this.monitor.leave();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void awaitHealthy() {
            this.monitor.enterWhenUninterruptibly(this.awaitHealthGuard);
            try {
                this.checkHealthy();
            }
            finally {
                this.monitor.leave();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
            this.monitor.enter();
            try {
                if (!this.monitor.waitForUninterruptibly(this.awaitHealthGuard, timeout, unit)) {
                    String string = String.valueOf(String.valueOf("Timeout waiting for the services to become healthy. The following services have not started: "));
                    String string2 = String.valueOf(String.valueOf(Multimaps.filterKeys(this.servicesByState, Predicates.in(ImmutableSet.of(Service.State.NEW, Service.State.STARTING)))));
                    throw new TimeoutException(new StringBuilder(0 + string.length() + string2.length()).append(string).append(string2).toString());
                }
                this.checkHealthy();
            }
            finally {
                this.monitor.leave();
            }
        }

        void awaitStopped() {
            this.monitor.enterWhenUninterruptibly(this.stoppedGuard);
            this.monitor.leave();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
            this.monitor.enter();
            try {
                if (!this.monitor.waitForUninterruptibly(this.stoppedGuard, timeout, unit)) {
                    String string = String.valueOf(String.valueOf("Timeout waiting for the services to stop. The following services have not stopped: "));
                    String string2 = String.valueOf(String.valueOf(Multimaps.filterKeys(this.servicesByState, Predicates.not(Predicates.in(ImmutableSet.of(Service.State.TERMINATED, Service.State.FAILED))))));
                    throw new TimeoutException(new StringBuilder(0 + string.length() + string2.length()).append(string).append(string2).toString());
                }
            }
            finally {
                this.monitor.leave();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        ImmutableMultimap<Service.State, Service> servicesByState() {
            ImmutableSetMultimap.Builder builder;
            builder = ImmutableSetMultimap.builder();
            this.monitor.enter();
            try {
                for (Map.Entry<Service.State, Service> entry : this.servicesByState.entries()) {
                    if (entry.getValue() instanceof NoOpService) continue;
                    builder.put((Object)entry.getKey(), entry.getValue());
                }
            }
            finally {
                this.monitor.leave();
            }
            return builder.build();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        ImmutableMap<Service, Long> startupTimes() {
            ArrayList<Map.Entry<Service, Long>> loadTimes;
            this.monitor.enter();
            try {
                loadTimes = Lists.newArrayListWithCapacity(this.startupTimers.size());
                for (Map.Entry<Service, Stopwatch> entry : this.startupTimers.entrySet()) {
                    Service service = entry.getKey();
                    Stopwatch stopWatch = entry.getValue();
                    if (stopWatch.isRunning() || service instanceof NoOpService) continue;
                    loadTimes.add(Maps.immutableEntry(service, stopWatch.elapsed(TimeUnit.MILLISECONDS)));
                }
            }
            finally {
                this.monitor.leave();
            }
            Collections.sort(loadTimes, Ordering.natural().onResultOf(new Function<Map.Entry<Service, Long>, Long>(){

                @Override
                public Long apply(Map.Entry<Service, Long> input) {
                    return input.getValue();
                }
            }));
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Map.Entry entry : loadTimes) {
                builder.put(entry);
            }
            return builder.build();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void transitionService(Service service, Service.State from, Service.State to) {
            Preconditions.checkNotNull(service);
            Preconditions.checkArgument(from != to);
            this.monitor.enter();
            try {
                this.transitioned = true;
                if (!this.ready) {
                    return;
                }
                Preconditions.checkState(this.servicesByState.remove((Object)from, service), "Service %s not at the expected location in the state map %s", new Object[]{service, from});
                Preconditions.checkState(this.servicesByState.put(to, service), "Service %s in the state map unexpectedly at %s", new Object[]{service, to});
                Stopwatch stopwatch = this.startupTimers.get(service);
                if (stopwatch == null) {
                    stopwatch = Stopwatch.createStarted();
                    this.startupTimers.put(service, stopwatch);
                }
                if (to.compareTo(Service.State.RUNNING) >= 0 && stopwatch.isRunning()) {
                    stopwatch.stop();
                    if (!(service instanceof NoOpService)) {
                        logger.log(Level.FINE, "Started {0} in {1}.", new Object[]{service, stopwatch});
                    }
                }
                if (to == Service.State.FAILED) {
                    this.fireFailedListeners(service);
                }
                if (this.states.count((Object)Service.State.RUNNING) == this.numberOfServices) {
                    this.fireHealthyListeners();
                } else if (this.states.count((Object)Service.State.TERMINATED) + this.states.count((Object)Service.State.FAILED) == this.numberOfServices) {
                    this.fireStoppedListeners();
                }
            }
            finally {
                this.monitor.leave();
                this.executeListeners();
            }
        }

        @GuardedBy(value="monitor")
        void fireStoppedListeners() {
            STOPPED_CALLBACK.enqueueOn(this.listeners);
        }

        @GuardedBy(value="monitor")
        void fireHealthyListeners() {
            HEALTHY_CALLBACK.enqueueOn(this.listeners);
        }

        @GuardedBy(value="monitor")
        void fireFailedListeners(final Service service) {
            String string = String.valueOf(String.valueOf(service));
            new ListenerCallQueue.Callback<Listener>(new StringBuilder(18 + string.length()).append("failed({service=").append(string).append("})").toString()){

                @Override
                void call(Listener listener) {
                    listener.failure(service);
                }
            }.enqueueOn(this.listeners);
        }

        void executeListeners() {
            Preconditions.checkState(!this.monitor.isOccupiedByCurrentThread(), "It is incorrect to execute listeners with the monitor held.");
            for (int i = 0; i < this.listeners.size(); ++i) {
                this.listeners.get(i).execute();
            }
        }

        @GuardedBy(value="monitor")
        void checkHealthy() {
            if (this.states.count((Object)Service.State.RUNNING) != this.numberOfServices) {
                String string = String.valueOf(String.valueOf(Multimaps.filterKeys(this.servicesByState, Predicates.not(Predicates.equalTo(Service.State.RUNNING)))));
                IllegalStateException exception = new IllegalStateException(new StringBuilder(79 + string.length()).append("Expected to be healthy after starting. The following services are not running: ").append(string).toString());
                throw exception;
            }
        }

    }

    @Beta
    public static abstract class Listener {
        public void healthy() {
        }

        public void stopped() {
        }

        public void failure(Service service) {
        }
    }

}

