/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.AbstractMultiworkerIOReactor;
import org.apache.http.impl.nio.reactor.ChannelEntry;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.impl.nio.reactor.SessionRequestHandle;
import org.apache.http.impl.nio.reactor.SessionRequestImpl;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public class DefaultConnectingIOReactor
extends AbstractMultiworkerIOReactor
implements ConnectingIOReactor {
    private final Queue<SessionRequestImpl> requestQueue = new ConcurrentLinkedQueue<SessionRequestImpl>();
    private long lastTimeoutCheck = System.currentTimeMillis();

    public DefaultConnectingIOReactor(IOReactorConfig config, ThreadFactory threadFactory) throws IOReactorException {
        super(config, threadFactory);
    }

    public DefaultConnectingIOReactor(IOReactorConfig config) throws IOReactorException {
        this(config, null);
    }

    public DefaultConnectingIOReactor() throws IOReactorException {
        this(null, null);
    }

    @Deprecated
    public DefaultConnectingIOReactor(int workerCount, ThreadFactory threadFactory, HttpParams params) throws IOReactorException {
        this(DefaultConnectingIOReactor.convert(workerCount, params), threadFactory);
    }

    @Deprecated
    public DefaultConnectingIOReactor(int workerCount, HttpParams params) throws IOReactorException {
        this(DefaultConnectingIOReactor.convert(workerCount, params), null);
    }

    @Override
    protected void cancelRequests() throws IOReactorException {
        SessionRequestImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            request.cancel();
        }
    }

    @Override
    protected void processEvents(int readyCount) throws IOReactorException {
        long currentTime;
        this.processSessionRequests();
        if (readyCount > 0) {
            Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                this.processEvent(key);
            }
            selectedKeys.clear();
        }
        if ((currentTime = System.currentTimeMillis()) - this.lastTimeoutCheck >= this.selectTimeout) {
            this.lastTimeoutCheck = currentTime;
            Set<SelectionKey> keys = this.selector.keys();
            this.processTimeouts(keys);
        }
    }

    private void processEvent(SelectionKey key) {
        block8 : {
            try {
                if (!key.isConnectable()) break block8;
                SocketChannel channel = (SocketChannel)key.channel();
                SessionRequestHandle requestHandle = (SessionRequestHandle)key.attachment();
                SessionRequestImpl sessionRequest = requestHandle.getSessionRequest();
                try {
                    channel.finishConnect();
                }
                catch (IOException ex) {
                    sessionRequest.failed(ex);
                }
                key.cancel();
                key.attach(null);
                if (!sessionRequest.isCompleted()) {
                    this.addChannel(new ChannelEntry(channel, sessionRequest));
                } else {
                    try {
                        channel.close();
                    }
                    catch (IOException ignore) {}
                }
            }
            catch (CancelledKeyException ex) {
                SessionRequestImpl sessionRequest;
                SessionRequestHandle requestHandle = (SessionRequestHandle)key.attachment();
                key.attach(null);
                if (requestHandle == null || (sessionRequest = requestHandle.getSessionRequest()) == null) break block8;
                sessionRequest.cancel();
            }
        }
    }

    private void processTimeouts(Set<SelectionKey> keys) {
        long now = System.currentTimeMillis();
        for (SelectionKey key : keys) {
            int timeout;
            SessionRequestHandle handle;
            SessionRequestImpl sessionRequest;
            Object attachment = key.attachment();
            if (!(attachment instanceof SessionRequestHandle) || (timeout = (sessionRequest = (handle = (SessionRequestHandle)key.attachment()).getSessionRequest()).getConnectTimeout()) <= 0 || handle.getRequestTime() + (long)timeout >= now) continue;
            sessionRequest.timeout();
        }
    }

    @Override
    public SessionRequest connect(SocketAddress remoteAddress, SocketAddress localAddress, Object attachment, SessionRequestCallback callback) {
        Asserts.check(this.status.compareTo(IOReactorStatus.ACTIVE) <= 0, "I/O reactor has been shut down");
        SessionRequestImpl sessionRequest = new SessionRequestImpl(remoteAddress, localAddress, attachment, callback);
        sessionRequest.setConnectTimeout(this.config.getConnectTimeout());
        this.requestQueue.add(sessionRequest);
        this.selector.wakeup();
        return sessionRequest;
    }

    private void validateAddress(SocketAddress address) throws UnknownHostException {
        InetSocketAddress endpoint;
        if (address == null) {
            return;
        }
        if (address instanceof InetSocketAddress && (endpoint = (InetSocketAddress)address).isUnresolved()) {
            throw new UnknownHostException(endpoint.getHostName());
        }
    }

    private void processSessionRequests() throws IOReactorException {
        SessionRequestImpl request;
        while ((request = this.requestQueue.poll()) != null) {
            SocketChannel socketChannel;
            if (request.isCompleted()) continue;
            try {
                socketChannel = SocketChannel.open();
            }
            catch (IOException ex) {
                throw new IOReactorException("Failure opening socket", ex);
            }
            try {
                socketChannel.configureBlocking(false);
                this.validateAddress(request.getLocalAddress());
                this.validateAddress(request.getRemoteAddress());
                if (request.getLocalAddress() != null) {
                    Socket sock = socketChannel.socket();
                    sock.setReuseAddress(this.config.isSoReuseAddress());
                    sock.bind(request.getLocalAddress());
                }
                this.prepareSocket(socketChannel.socket());
                boolean connected = socketChannel.connect(request.getRemoteAddress());
                if (connected) {
                    ChannelEntry entry = new ChannelEntry(socketChannel, request);
                    this.addChannel(entry);
                    continue;
                }
            }
            catch (IOException ex) {
                DefaultConnectingIOReactor.closeChannel(socketChannel);
                request.failed(ex);
                return;
            }
            SessionRequestHandle requestHandle = new SessionRequestHandle(request);
            try {
                SelectionKey key = socketChannel.register(this.selector, 8, requestHandle);
                request.setKey(key);
            }
            catch (IOException ex) {
                DefaultConnectingIOReactor.closeChannel(socketChannel);
                throw new IOReactorException("Failure registering channel with the selector", ex);
            }
        }
    }
}

