/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.embedded;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedEventLoop;
import io.netty.channel.embedded.EmbeddedSocketAddress;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.RecyclableArrayList;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;

public class EmbeddedChannel
extends AbstractChannel {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(EmbeddedChannel.class);
    private static final ChannelMetadata METADATA = new ChannelMetadata(false);
    private final EmbeddedEventLoop loop = new EmbeddedEventLoop();
    private final ChannelConfig config = new DefaultChannelConfig(this);
    private final SocketAddress localAddress = new EmbeddedSocketAddress();
    private final SocketAddress remoteAddress = new EmbeddedSocketAddress();
    private Queue<Object> inboundMessages;
    private Queue<Object> outboundMessages;
    private Throwable lastException;
    private int state;

    public /* varargs */ EmbeddedChannel(final ChannelHandler ... handlers) {
        super(null);
        if (handlers == null) {
            throw new NullPointerException("handlers");
        }
        ChannelPipeline p = this.pipeline();
        p.addLast(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                for (ChannelHandler h : handlers) {
                    if (h == null) break;
                    pipeline.addLast(h);
                }
            }
        });
        ChannelFuture future = this.loop.register(this);
        assert (future.isDone());
        p.addLast(new LastInboundHandler());
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    public ChannelConfig config() {
        return this.config;
    }

    @Override
    public boolean isOpen() {
        return this.state < 2;
    }

    @Override
    public boolean isActive() {
        return this.state == 1;
    }

    public Queue<Object> inboundMessages() {
        if (this.inboundMessages == null) {
            this.inboundMessages = new ArrayDeque<Object>();
        }
        return this.inboundMessages;
    }

    @Deprecated
    public Queue<Object> lastInboundBuffer() {
        return this.inboundMessages();
    }

    public Queue<Object> outboundMessages() {
        if (this.outboundMessages == null) {
            this.outboundMessages = new ArrayDeque<Object>();
        }
        return this.outboundMessages;
    }

    @Deprecated
    public Queue<Object> lastOutboundBuffer() {
        return this.outboundMessages();
    }

    public Object readInbound() {
        return EmbeddedChannel.poll(this.inboundMessages);
    }

    public Object readOutbound() {
        return EmbeddedChannel.poll(this.outboundMessages);
    }

    public /* varargs */ boolean writeInbound(Object ... msgs) {
        this.ensureOpen();
        if (msgs.length == 0) {
            return EmbeddedChannel.isNotEmpty(this.inboundMessages);
        }
        ChannelPipeline p = this.pipeline();
        for (Object m : msgs) {
            p.fireChannelRead(m);
        }
        p.fireChannelReadComplete();
        this.runPendingTasks();
        this.checkException();
        return EmbeddedChannel.isNotEmpty(this.inboundMessages);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public /* varargs */ boolean writeOutbound(Object ... msgs) {
        this.ensureOpen();
        if (msgs.length == 0) {
            return EmbeddedChannel.isNotEmpty(this.outboundMessages);
        }
        RecyclableArrayList futures = RecyclableArrayList.newInstance(msgs.length);
        try {
            int i;
            for (Object m : msgs) {
                if (m == null) break;
                futures.add(this.write(m));
            }
            this.flush();
            int size = futures.size();
            for (i = 0; i < size; i += 1) {
                ChannelFuture future = (ChannelFuture)futures.get(i);
                assert (future.isDone());
                if (future.cause() == null) continue;
                this.recordException(future.cause());
            }
            this.runPendingTasks();
            this.checkException();
            i = EmbeddedChannel.isNotEmpty(this.outboundMessages) ? 1 : 0;
            return (boolean)i;
        }
        finally {
            futures.recycle();
        }
    }

    public boolean finish() {
        this.close();
        this.runPendingTasks();
        this.loop.cancelScheduledTasks();
        this.checkException();
        return EmbeddedChannel.isNotEmpty(this.inboundMessages) || EmbeddedChannel.isNotEmpty(this.outboundMessages);
    }

    private static boolean isNotEmpty(Queue<Object> queue) {
        return queue != null && !queue.isEmpty();
    }

    private static Object poll(Queue<Object> queue) {
        return queue != null ? queue.poll() : null;
    }

    public void runPendingTasks() {
        try {
            this.loop.runTasks();
        }
        catch (Exception e) {
            this.recordException(e);
        }
        try {
            this.loop.runScheduledTasks();
        }
        catch (Exception e) {
            this.recordException(e);
        }
    }

    public long runScheduledPendingTasks() {
        try {
            return this.loop.runScheduledTasks();
        }
        catch (Exception e) {
            this.recordException(e);
            return this.loop.nextScheduledTask();
        }
    }

    private void recordException(Throwable cause) {
        if (this.lastException == null) {
            this.lastException = cause;
        } else {
            logger.warn("More than one exception was raised. Will report only the first one and log others.", cause);
        }
    }

    public void checkException() {
        Throwable t = this.lastException;
        if (t == null) {
            return;
        }
        this.lastException = null;
        PlatformDependent.throwException(t);
    }

    protected final void ensureOpen() {
        if (!this.isOpen()) {
            this.recordException(new ClosedChannelException());
            this.checkException();
        }
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof EmbeddedEventLoop;
    }

    @Override
    protected SocketAddress localAddress0() {
        return this.isActive() ? this.localAddress : null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return this.isActive() ? this.remoteAddress : null;
    }

    @Override
    protected void doRegister() throws Exception {
        this.state = 1;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
    }

    @Override
    protected void doDisconnect() throws Exception {
        this.doClose();
    }

    @Override
    protected void doClose() throws Exception {
        this.state = 2;
    }

    @Override
    protected void doBeginRead() throws Exception {
    }

    @Override
    protected AbstractChannel.AbstractUnsafe newUnsafe() {
        return new DefaultUnsafe();
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        Object msg;
        while ((msg = in.current()) != null) {
            ReferenceCountUtil.retain(msg);
            this.outboundMessages().add(msg);
            in.remove();
        }
    }

    private final class LastInboundHandler
    extends ChannelInboundHandlerAdapter {
        private LastInboundHandler() {
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            EmbeddedChannel.this.inboundMessages().add(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            EmbeddedChannel.this.recordException(cause);
        }
    }

    private class DefaultUnsafe
    extends AbstractChannel.AbstractUnsafe {
        private DefaultUnsafe() {
        }

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            this.safeSetSuccess(promise);
        }
    }

}

