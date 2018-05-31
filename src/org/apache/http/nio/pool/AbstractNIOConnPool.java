/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.pool;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.pool.LeaseRequest;
import org.apache.http.nio.pool.NIOConnFactory;
import org.apache.http.nio.pool.RouteSpecificPool;
import org.apache.http.nio.pool.SocketAddressResolver;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.pool.ConnPool;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.pool.PoolEntry;
import org.apache.http.pool.PoolEntryCallback;
import org.apache.http.pool.PoolStats;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public abstract class AbstractNIOConnPool<T, C, E extends PoolEntry<T, C>>
implements ConnPool<T, E>,
ConnPoolControl<T> {
    private final ConnectingIOReactor ioreactor;
    private final NIOConnFactory<T, C> connFactory;
    private final SocketAddressResolver<T> addressResolver;
    private final SessionRequestCallback sessionRequestCallback;
    private final Map<T, RouteSpecificPool<T, C, E>> routeToPool;
    private final LinkedList<LeaseRequest<T, C, E>> leasingRequests;
    private final Set<SessionRequest> pending;
    private final Set<E> leased;
    private final LinkedList<E> available;
    private final ConcurrentLinkedQueue<LeaseRequest<T, C, E>> completedRequests;
    private final Map<T, Integer> maxPerRoute;
    private final Lock lock;
    private final AtomicBoolean isShutDown;
    private volatile int defaultMaxPerRoute;
    private volatile int maxTotal;

    @Deprecated
    public AbstractNIOConnPool(ConnectingIOReactor ioreactor, NIOConnFactory<T, C> connFactory, int defaultMaxPerRoute, int maxTotal) {
        Args.notNull(ioreactor, "I/O reactor");
        Args.notNull(connFactory, "Connection factory");
        Args.positive(defaultMaxPerRoute, "Max per route value");
        Args.positive(maxTotal, "Max total value");
        this.ioreactor = ioreactor;
        this.connFactory = connFactory;
        this.addressResolver = new SocketAddressResolver<T>(){

            @Override
            public SocketAddress resolveLocalAddress(T route) throws IOException {
                return AbstractNIOConnPool.this.resolveLocalAddress(route);
            }

            @Override
            public SocketAddress resolveRemoteAddress(T route) throws IOException {
                return AbstractNIOConnPool.this.resolveRemoteAddress(route);
            }
        };
        this.sessionRequestCallback = new InternalSessionRequestCallback();
        this.routeToPool = new HashMap<T, RouteSpecificPool<T, C, E>>();
        this.leasingRequests = new LinkedList();
        this.pending = new HashSet<SessionRequest>();
        this.leased = new HashSet();
        this.available = new LinkedList();
        this.maxPerRoute = new HashMap<T, Integer>();
        this.completedRequests = new ConcurrentLinkedQueue();
        this.lock = new ReentrantLock();
        this.isShutDown = new AtomicBoolean(false);
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        this.maxTotal = maxTotal;
    }

    public AbstractNIOConnPool(ConnectingIOReactor ioreactor, NIOConnFactory<T, C> connFactory, SocketAddressResolver<T> addressResolver, int defaultMaxPerRoute, int maxTotal) {
        Args.notNull(ioreactor, "I/O reactor");
        Args.notNull(connFactory, "Connection factory");
        Args.notNull(addressResolver, "Address resolver");
        Args.positive(defaultMaxPerRoute, "Max per route value");
        Args.positive(maxTotal, "Max total value");
        this.ioreactor = ioreactor;
        this.connFactory = connFactory;
        this.addressResolver = addressResolver;
        this.sessionRequestCallback = new InternalSessionRequestCallback();
        this.routeToPool = new HashMap<T, RouteSpecificPool<T, C, E>>();
        this.leasingRequests = new LinkedList();
        this.pending = new HashSet<SessionRequest>();
        this.leased = new HashSet();
        this.available = new LinkedList();
        this.completedRequests = new ConcurrentLinkedQueue();
        this.maxPerRoute = new HashMap<T, Integer>();
        this.lock = new ReentrantLock();
        this.isShutDown = new AtomicBoolean(false);
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        this.maxTotal = maxTotal;
    }

    @Deprecated
    protected SocketAddress resolveRemoteAddress(T route) {
        return null;
    }

    @Deprecated
    protected SocketAddress resolveLocalAddress(T route) {
        return null;
    }

    protected abstract E createEntry(T var1, C var2);

    protected void onLease(E entry) {
    }

    protected void onRelease(E entry) {
    }

    public boolean isShutdown() {
        return this.isShutDown.get();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void shutdown(long waitMs) throws IOException {
        if (this.isShutDown.compareAndSet(false, true)) {
            this.fireCallbacks();
            this.lock.lock();
            try {
                for (SessionRequest sessionRequest : this.pending) {
                    sessionRequest.cancel();
                }
                for (PoolEntry entry : this.available) {
                    entry.close();
                }
                for (PoolEntry entry : this.leased) {
                    entry.close();
                }
                for (RouteSpecificPool pool : this.routeToPool.values()) {
                    pool.shutdown();
                }
                this.routeToPool.clear();
                this.leased.clear();
                this.pending.clear();
                this.available.clear();
                this.leasingRequests.clear();
                this.ioreactor.shutdown(waitMs);
            }
            finally {
                this.lock.unlock();
            }
        }
    }

    private RouteSpecificPool<T, C, E> getPool(T route) {
        RouteSpecificPool<T, C, E> pool = this.routeToPool.get(route);
        if (pool == null) {
            pool = new RouteSpecificPool<T, C, E>(route){

                @Override
                protected E createEntry(T route, C conn) {
                    return AbstractNIOConnPool.this.createEntry(route, conn);
                }
            };
            this.routeToPool.put(route, pool);
        }
        return pool;
    }

    public Future<E> lease(T route, Object state, long connectTimeout, TimeUnit tunit, FutureCallback<E> callback) {
        return this.lease(route, state, connectTimeout, connectTimeout, tunit, callback);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Future<E> lease(T route, Object state, long connectTimeout, long leaseTimeout, TimeUnit tunit, FutureCallback<E> callback) {
        BasicFuture<E> future;
        Args.notNull(route, "Route");
        Args.notNull(tunit, "Time unit");
        Asserts.check(!this.isShutDown.get(), "Connection pool shut down");
        future = new BasicFuture<E>(callback);
        this.lock.lock();
        try {
            long timeout = connectTimeout > 0L ? tunit.toMillis(connectTimeout) : 0L;
            LeaseRequest<T, C, E> request = new LeaseRequest<T, C, E>(route, state, timeout, leaseTimeout, future);
            boolean completed = this.processPendingRequest(request);
            if (!request.isDone() && !completed) {
                this.leasingRequests.add(request);
            }
            if (request.isDone()) {
                this.completedRequests.add(request);
            }
        }
        finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
        return future;
    }

    @Override
    public Future<E> lease(T route, Object state, FutureCallback<E> callback) {
        return this.lease(route, state, -1L, TimeUnit.MICROSECONDS, callback);
    }

    public Future<E> lease(T route, Object state) {
        return this.lease(route, state, -1L, TimeUnit.MICROSECONDS, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void release(E entry, boolean reusable) {
        if (entry == null) {
            return;
        }
        if (this.isShutDown.get()) {
            return;
        }
        this.lock.lock();
        try {
            if (this.leased.remove(entry)) {
                RouteSpecificPool<T, C, E> pool = this.getPool(entry.getRoute());
                pool.free(entry, reusable);
                if (reusable) {
                    this.available.addFirst(entry);
                    this.onRelease(entry);
                } else {
                    entry.close();
                }
                this.processNextPendingRequest();
            }
        }
        finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
    }

    private void processPendingRequests() {
        ListIterator<LeaseRequest<T, C, E>> it = this.leasingRequests.listIterator();
        while (it.hasNext()) {
            LeaseRequest<T, C, E> request = it.next();
            boolean completed = this.processPendingRequest(request);
            if (request.isDone() || completed) {
                it.remove();
            }
            if (!request.isDone()) continue;
            this.completedRequests.add(request);
        }
    }

    private void processNextPendingRequest() {
        ListIterator<LeaseRequest<T, C, E>> it = this.leasingRequests.listIterator();
        while (it.hasNext()) {
            LeaseRequest<T, C, E> request = it.next();
            boolean completed = this.processPendingRequest(request);
            if (request.isDone() || completed) {
                it.remove();
            }
            if (request.isDone()) {
                this.completedRequests.add(request);
            }
            if (!completed) continue;
            return;
        }
    }

    private boolean processPendingRequest(LeaseRequest<T, C, E> request) {
        E entry;
        T route = request.getRoute();
        Object state = request.getState();
        long deadline = request.getDeadline();
        long now = System.currentTimeMillis();
        if (now > deadline) {
            request.failed(new TimeoutException());
            return false;
        }
        RouteSpecificPool<T, C, E> pool = this.getPool(route);
        while ((entry = pool.getFree(state)) != null && (entry.isClosed() || entry.isExpired(System.currentTimeMillis()))) {
            entry.close();
            this.available.remove(entry);
            pool.free(entry, false);
        }
        if (entry != null) {
            this.available.remove(entry);
            this.leased.add(entry);
            request.completed(entry);
            this.onLease(entry);
            return true;
        }
        int maxPerRoute = this.getMax(route);
        int excess = Math.max(0, pool.getAllocatedCount() + 1 - maxPerRoute);
        if (excess > 0) {
            E lastUsed;
            for (int i = 0; i < excess && (lastUsed = pool.getLastUsed()) != null; ++i) {
                lastUsed.close();
                this.available.remove(lastUsed);
                pool.remove(lastUsed);
            }
        }
        if (pool.getAllocatedCount() < maxPerRoute) {
            SocketAddress remoteAddress;
            SocketAddress localAddress;
            int totalUsed = this.pending.size() + this.leased.size();
            int freeCapacity = Math.max(this.maxTotal - totalUsed, 0);
            if (freeCapacity == 0) {
                return false;
            }
            int totalAvailable = this.available.size();
            if (totalAvailable > freeCapacity - 1 && !this.available.isEmpty()) {
                PoolEntry lastUsed = (PoolEntry)this.available.removeLast();
                lastUsed.close();
                RouteSpecificPool<T, C, PoolEntry> otherpool = this.getPool(lastUsed.getRoute());
                otherpool.remove(lastUsed);
            }
            try {
                remoteAddress = this.addressResolver.resolveRemoteAddress(route);
                localAddress = this.addressResolver.resolveLocalAddress(route);
            }
            catch (IOException ex) {
                request.failed(ex);
                return false;
            }
            SessionRequest sessionRequest = this.ioreactor.connect(remoteAddress, localAddress, route, this.sessionRequestCallback);
            int timout = request.getConnectTimeout() < Integer.MAX_VALUE ? (int)request.getConnectTimeout() : Integer.MAX_VALUE;
            sessionRequest.setConnectTimeout(timout);
            this.pending.add(sessionRequest);
            pool.addPending(sessionRequest, request.getFuture());
            return true;
        }
        return false;
    }

    private void fireCallbacks() {
        LeaseRequest<T, C, E> request;
        while ((request = this.completedRequests.poll()) != null) {
            BasicFuture<E> future = request.getFuture();
            Exception ex = request.getException();
            E result = request.getResult();
            if (ex != null) {
                future.failed(ex);
                continue;
            }
            if (result != null) {
                future.completed(result);
                continue;
            }
            future.cancel();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void validatePendingRequests() {
        this.lock.lock();
        try {
            long now = System.currentTimeMillis();
            ListIterator<LeaseRequest<T, C, E>> it = this.leasingRequests.listIterator();
            while (it.hasNext()) {
                LeaseRequest<T, C, E> request = it.next();
                long deadline = request.getDeadline();
                if (now <= deadline) continue;
                it.remove();
                request.failed(new TimeoutException());
                this.completedRequests.add(request);
            }
        }
        finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void requestCompleted(SessionRequest request) {
        if (this.isShutDown.get()) {
            return;
        }
        Object route = request.getAttachment();
        this.lock.lock();
        try {
            this.pending.remove(request);
            RouteSpecificPool<Object, C, E> pool = this.getPool(route);
            IOSession session = request.getSession();
            try {
                C conn = this.connFactory.create(route, session);
                E entry = pool.createEntry(request, conn);
                this.leased.add(entry);
                pool.completed(request, entry);
                this.onLease(entry);
            }
            catch (IOException ex) {
                pool.failed(request, ex);
            }
        }
        finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void requestCancelled(SessionRequest request) {
        if (this.isShutDown.get()) {
            return;
        }
        Object route = request.getAttachment();
        this.lock.lock();
        try {
            this.pending.remove(request);
            RouteSpecificPool<Object, C, E> pool = this.getPool(route);
            pool.cancelled(request);
            this.processNextPendingRequest();
        }
        finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void requestFailed(SessionRequest request) {
        if (this.isShutDown.get()) {
            return;
        }
        Object route = request.getAttachment();
        this.lock.lock();
        try {
            this.pending.remove(request);
            RouteSpecificPool<Object, C, E> pool = this.getPool(route);
            pool.failed(request, request.getException());
            this.processNextPendingRequest();
        }
        finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void requestTimeout(SessionRequest request) {
        if (this.isShutDown.get()) {
            return;
        }
        Object route = request.getAttachment();
        this.lock.lock();
        try {
            this.pending.remove(request);
            RouteSpecificPool<Object, C, E> pool = this.getPool(route);
            pool.timeout(request);
            this.processNextPendingRequest();
        }
        finally {
            this.lock.unlock();
        }
        this.fireCallbacks();
    }

    private int getMax(T route) {
        Integer v = this.maxPerRoute.get(route);
        if (v != null) {
            return v;
        }
        return this.defaultMaxPerRoute;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMaxTotal(int max) {
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.maxTotal = max;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getMaxTotal() {
        this.lock.lock();
        try {
            int n = this.maxTotal;
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setDefaultMaxPerRoute(int max) {
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.defaultMaxPerRoute = max;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getDefaultMaxPerRoute() {
        this.lock.lock();
        try {
            int n = this.defaultMaxPerRoute;
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMaxPerRoute(T route, int max) {
        Args.notNull(route, "Route");
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.maxPerRoute.put(route, max);
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getMaxPerRoute(T route) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            int n = this.getMax(route);
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public PoolStats getTotalStats() {
        this.lock.lock();
        try {
            PoolStats poolStats = new PoolStats(this.leased.size(), this.pending.size(), this.available.size(), this.maxTotal);
            return poolStats;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public PoolStats getStats(T route) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            RouteSpecificPool<T, C, E> pool = this.getPool(route);
            PoolStats poolStats = new PoolStats(pool.getLeasedCount(), pool.getPendingCount(), pool.getAvailableCount(), this.getMax(route));
            return poolStats;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void enumAvailable(PoolEntryCallback<T, C> callback) {
        this.lock.lock();
        try {
            Iterator<E> it = this.available.iterator();
            while (it.hasNext()) {
                PoolEntry entry = (PoolEntry)it.next();
                callback.process(entry);
                if (!entry.isClosed()) continue;
                RouteSpecificPool<T, C, PoolEntry> pool = this.getPool(entry.getRoute());
                pool.remove(entry);
                it.remove();
            }
            this.processPendingRequests();
            this.purgePoolMap();
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void enumLeased(PoolEntryCallback<T, C> callback) {
        this.lock.lock();
        try {
            for (PoolEntry entry : this.leased) {
                callback.process(entry);
            }
            this.processPendingRequests();
        }
        finally {
            this.lock.unlock();
        }
    }

    @Deprecated
    protected void enumEntries(Iterator<E> it, PoolEntryCallback<T, C> callback) {
        while (it.hasNext()) {
            PoolEntry entry = (PoolEntry)it.next();
            callback.process(entry);
        }
        this.processPendingRequests();
    }

    private void purgePoolMap() {
        Iterator<Map.Entry<T, RouteSpecificPool<T, C, E>>> it = this.routeToPool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<T, RouteSpecificPool<T, C, E>> entry = it.next();
            RouteSpecificPool<T, C, E> pool = entry.getValue();
            if (pool.getAllocatedCount() != 0) continue;
            it.remove();
        }
    }

    public void closeIdle(long idletime, TimeUnit tunit) {
        Args.notNull(tunit, "Time unit");
        long time = tunit.toMillis(idletime);
        if (time < 0L) {
            time = 0L;
        }
        final long deadline = System.currentTimeMillis() - time;
        this.enumAvailable(new PoolEntryCallback<T, C>(){

            @Override
            public void process(PoolEntry<T, C> entry) {
                if (entry.getUpdated() <= deadline) {
                    entry.close();
                }
            }
        });
    }

    public void closeExpired() {
        final long now = System.currentTimeMillis();
        this.enumAvailable(new PoolEntryCallback<T, C>(){

            @Override
            public void process(PoolEntry<T, C> entry) {
                if (entry.isExpired(now)) {
                    entry.close();
                }
            }
        });
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(this.leased);
        buffer.append("][available: ");
        buffer.append(this.available);
        buffer.append("][pending: ");
        buffer.append(this.pending);
        buffer.append("]");
        return buffer.toString();
    }

    class InternalSessionRequestCallback
    implements SessionRequestCallback {
        InternalSessionRequestCallback() {
        }

        public void completed(SessionRequest request) {
            AbstractNIOConnPool.this.requestCompleted(request);
        }

        public void cancelled(SessionRequest request) {
            AbstractNIOConnPool.this.requestCancelled(request);
        }

        public void failed(SessionRequest request) {
            AbstractNIOConnPool.this.requestFailed(request);
        }

        public void timeout(SessionRequest request) {
            AbstractNIOConnPool.this.requestTimeout(request);
        }
    }

}

