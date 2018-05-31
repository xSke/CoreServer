/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.AbstractMultiworkerIOReactor;
import org.apache.http.impl.nio.reactor.ChannelEntry;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.impl.nio.reactor.ListenerEndpointClosedCallback;
import org.apache.http.impl.nio.reactor.ListenerEndpointImpl;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public class DefaultListeningIOReactor
extends AbstractMultiworkerIOReactor
implements ListeningIOReactor {
    private final Queue<ListenerEndpointImpl> requestQueue = new ConcurrentLinkedQueue<ListenerEndpointImpl>();
    private final Set<ListenerEndpointImpl> endpoints = Collections.synchronizedSet(new HashSet());
    private final Set<SocketAddress> pausedEndpoints = new HashSet<SocketAddress>();
    private volatile boolean paused;

    public DefaultListeningIOReactor(IOReactorConfig config, ThreadFactory threadFactory) throws IOReactorException {
        super(config, threadFactory);
    }

    public DefaultListeningIOReactor(IOReactorConfig config) throws IOReactorException {
        this(config, null);
    }

    public DefaultListeningIOReactor() throws IOReactorException {
        this(null, null);
    }

    @Deprecated
    public DefaultListeningIOReactor(int workerCount, ThreadFactory threadFactory, HttpParams params) throws IOReactorException {
        this(DefaultListeningIOReactor.convert(workerCount, params), threadFactory);
    }

    @Deprecated
    public DefaultListeningIOReactor(int workerCount, HttpParams params) throws IOReactorException {
        this(DefaultListeningIOReactor.convert(workerCount, params), null);
    }

    @Override
    protected void cancelRequests() throws IOReactorException {
        ListenerEndpointImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            request.cancel();
        }
    }

    @Override
    protected void processEvents(int readyCount) throws IOReactorException {
        if (!this.paused) {
            this.processSessionRequests();
        }
        if (readyCount > 0) {
            Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                this.processEvent(key);
            }
            selectedKeys.clear();
        }
    }

    private void processEvent(SelectionKey key) throws IOReactorException {
        block7 : {
            try {
                if (!key.isAcceptable()) break block7;
                ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
                do {
                    SocketChannel socketChannel;
                    block9 : {
                        block8 : {
                            socketChannel = null;
                            try {
                                socketChannel = serverChannel.accept();
                            }
                            catch (IOException ex) {
                                if (this.exceptionHandler != null && this.exceptionHandler.handle(ex)) break block8;
                                throw new IOReactorException("Failure accepting connection", ex);
                            }
                        }
                        if (socketChannel == null) break block7;
                        try {
                            this.prepareSocket(socketChannel.socket());
                        }
                        catch (IOException ex) {
                            if (this.exceptionHandler != null && this.exceptionHandler.handle(ex)) break block9;
                            throw new IOReactorException("Failure initalizing socket", ex);
                        }
                    }
                    ChannelEntry entry = new ChannelEntry(socketChannel);
                    this.addChannel(entry);
                } while (true);
            }
            catch (CancelledKeyException ex) {
                ListenerEndpoint endpoint = (ListenerEndpoint)key.attachment();
                this.endpoints.remove(endpoint);
                key.attach(null);
            }
        }
    }

    private ListenerEndpointImpl createEndpoint(SocketAddress address) {
        return new ListenerEndpointImpl(address, new ListenerEndpointClosedCallback(){

            public void endpointClosed(ListenerEndpoint endpoint) {
                DefaultListeningIOReactor.this.endpoints.remove(endpoint);
            }
        });
    }

    @Override
    public ListenerEndpoint listen(SocketAddress address) {
        Asserts.check(this.status.compareTo(IOReactorStatus.ACTIVE) <= 0, "I/O reactor has been shut down");
        ListenerEndpointImpl request = this.createEndpoint(address);
        this.requestQueue.add(request);
        this.selector.wakeup();
        return request;
    }

    private void processSessionRequests() throws IOReactorException {
        ListenerEndpointImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            ServerSocketChannel serverChannel;
            SocketAddress address = request.getAddress();
            try {
                serverChannel = ServerSocketChannel.open();
            }
            catch (IOException ex) {
                throw new IOReactorException("Failure opening server socket", ex);
            }
            try {
                ServerSocket socket = serverChannel.socket();
                socket.setReuseAddress(this.config.isSoReuseAddress());
                if (this.config.getSoTimeout() > 0) {
                    socket.setSoTimeout(this.config.getSoTimeout());
                }
                if (this.config.getRcvBufSize() > 0) {
                    socket.setReceiveBufferSize(this.config.getRcvBufSize());
                }
                serverChannel.configureBlocking(false);
                socket.bind(address);
            }
            catch (IOException ex) {
                DefaultListeningIOReactor.closeChannel(serverChannel);
                request.failed(ex);
                if (this.exceptionHandler == null || !this.exceptionHandler.handle(ex)) {
                    throw new IOReactorException("Failure binding socket to address " + address, ex);
                }
                return;
            }
            try {
                SelectionKey key = serverChannel.register(this.selector, 16);
                key.attach(request);
                request.setKey(key);
            }
            catch (IOException ex) {
                DefaultListeningIOReactor.closeChannel(serverChannel);
                throw new IOReactorException("Failure registering channel with the selector", ex);
            }
            this.endpoints.add(request);
            request.completed(serverChannel.socket().getLocalSocketAddress());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Set<ListenerEndpoint> getEndpoints() {
        HashSet<ListenerEndpoint> set = new HashSet<ListenerEndpoint>();
        Set<ListenerEndpointImpl> set2 = this.endpoints;
        synchronized (set2) {
            Iterator<ListenerEndpointImpl> it = this.endpoints.iterator();
            while (it.hasNext()) {
                ListenerEndpoint endpoint = it.next();
                if (!endpoint.isClosed()) {
                    set.add(endpoint);
                    continue;
                }
                it.remove();
            }
        }
        return set;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void pause() throws IOException {
        if (this.paused) {
            return;
        }
        this.paused = true;
        Set<ListenerEndpointImpl> set = this.endpoints;
        synchronized (set) {
            for (ListenerEndpointImpl endpoint : this.endpoints) {
                if (endpoint.isClosed()) continue;
                endpoint.close();
                this.pausedEndpoints.add(endpoint.getAddress());
            }
            this.endpoints.clear();
        }
    }

    @Override
    public void resume() throws IOException {
        if (!this.paused) {
            return;
        }
        this.paused = false;
        for (SocketAddress address : this.pausedEndpoints) {
            ListenerEndpointImpl request = this.createEndpoint(address);
            this.requestQueue.add(request);
        }
        this.pausedEndpoints.clear();
        this.selector.wakeup();
    }

}

