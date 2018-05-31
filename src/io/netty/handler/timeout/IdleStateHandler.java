/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.timeout;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class IdleStateHandler
extends ChannelDuplexHandler {
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
    private final ChannelFutureListener writeListener = new ChannelFutureListener(){

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            IdleStateHandler.this.lastWriteTime = System.nanoTime();
            IdleStateHandler.this.firstWriterIdleEvent = (IdleStateHandler.this.firstAllIdleEvent = true);
        }
    };
    private final long readerIdleTimeNanos;
    private final long writerIdleTimeNanos;
    private final long allIdleTimeNanos;
    volatile ScheduledFuture<?> readerIdleTimeout;
    volatile long lastReadTime;
    private boolean firstReaderIdleEvent = true;
    volatile ScheduledFuture<?> writerIdleTimeout;
    volatile long lastWriteTime;
    private boolean firstWriterIdleEvent = true;
    volatile ScheduledFuture<?> allIdleTimeout;
    private boolean firstAllIdleEvent = true;
    private volatile int state;
    private volatile boolean reading;

    public IdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        this.readerIdleTimeNanos = readerIdleTime <= 0L ? 0L : Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS);
        this.writerIdleTimeNanos = writerIdleTime <= 0L ? 0L : Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS);
        this.allIdleTimeNanos = allIdleTime <= 0L ? 0L : Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS);
    }

    public long getReaderIdleTimeInMillis() {
        return TimeUnit.NANOSECONDS.toMillis(this.readerIdleTimeNanos);
    }

    public long getWriterIdleTimeInMillis() {
        return TimeUnit.NANOSECONDS.toMillis(this.writerIdleTimeNanos);
    }

    public long getAllIdleTimeInMillis() {
        return TimeUnit.NANOSECONDS.toMillis(this.allIdleTimeNanos);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            this.initialize(ctx);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.destroy();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            this.initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (this.readerIdleTimeNanos > 0L || this.allIdleTimeNanos > 0L) {
            this.reading = true;
            this.firstAllIdleEvent = true;
            this.firstReaderIdleEvent = true;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (this.readerIdleTimeNanos > 0L || this.allIdleTimeNanos > 0L) {
            this.lastReadTime = System.nanoTime();
            this.reading = false;
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (this.writerIdleTimeNanos > 0L || this.allIdleTimeNanos > 0L) {
            promise.addListener(this.writeListener);
        }
        ctx.write(msg, promise);
    }

    private void initialize(ChannelHandlerContext ctx) {
        switch (this.state) {
            case 1: 
            case 2: {
                return;
            }
        }
        this.state = 1;
        EventExecutor loop = ctx.executor();
        this.lastReadTime = this.lastWriteTime = System.nanoTime();
        if (this.readerIdleTimeNanos > 0L) {
            this.readerIdleTimeout = loop.schedule(new ReaderIdleTimeoutTask(ctx), this.readerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (this.writerIdleTimeNanos > 0L) {
            this.writerIdleTimeout = loop.schedule(new WriterIdleTimeoutTask(ctx), this.writerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (this.allIdleTimeNanos > 0L) {
            this.allIdleTimeout = loop.schedule(new AllIdleTimeoutTask(ctx), this.allIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
    }

    private void destroy() {
        this.state = 2;
        if (this.readerIdleTimeout != null) {
            this.readerIdleTimeout.cancel(false);
            this.readerIdleTimeout = null;
        }
        if (this.writerIdleTimeout != null) {
            this.writerIdleTimeout.cancel(false);
            this.writerIdleTimeout = null;
        }
        if (this.allIdleTimeout != null) {
            this.allIdleTimeout.cancel(false);
            this.allIdleTimeout = null;
        }
    }

    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    private final class AllIdleTimeoutTask
    implements Runnable {
        private final ChannelHandlerContext ctx;

        AllIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!this.ctx.channel().isOpen()) {
                return;
            }
            long nextDelay = IdleStateHandler.this.allIdleTimeNanos;
            if (!IdleStateHandler.this.reading) {
                nextDelay -= System.nanoTime() - Math.max(IdleStateHandler.this.lastReadTime, IdleStateHandler.this.lastWriteTime);
            }
            if (nextDelay <= 0L) {
                IdleStateHandler.this.allIdleTimeout = this.ctx.executor().schedule(this, IdleStateHandler.this.allIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event;
                    if (IdleStateHandler.this.firstAllIdleEvent) {
                        IdleStateHandler.this.firstAllIdleEvent = false;
                        event = IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.ALL_IDLE_STATE_EVENT;
                    }
                    IdleStateHandler.this.channelIdle(this.ctx, event);
                }
                catch (Throwable t) {
                    this.ctx.fireExceptionCaught(t);
                }
            } else {
                IdleStateHandler.this.allIdleTimeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class WriterIdleTimeoutTask
    implements Runnable {
        private final ChannelHandlerContext ctx;

        WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!this.ctx.channel().isOpen()) {
                return;
            }
            long lastWriteTime = IdleStateHandler.this.lastWriteTime;
            long nextDelay = IdleStateHandler.this.writerIdleTimeNanos - (System.nanoTime() - lastWriteTime);
            if (nextDelay <= 0L) {
                IdleStateHandler.this.writerIdleTimeout = this.ctx.executor().schedule(this, IdleStateHandler.this.writerIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event;
                    if (IdleStateHandler.this.firstWriterIdleEvent) {
                        IdleStateHandler.this.firstWriterIdleEvent = false;
                        event = IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.WRITER_IDLE_STATE_EVENT;
                    }
                    IdleStateHandler.this.channelIdle(this.ctx, event);
                }
                catch (Throwable t) {
                    this.ctx.fireExceptionCaught(t);
                }
            } else {
                IdleStateHandler.this.writerIdleTimeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class ReaderIdleTimeoutTask
    implements Runnable {
        private final ChannelHandlerContext ctx;

        ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!this.ctx.channel().isOpen()) {
                return;
            }
            long nextDelay = IdleStateHandler.this.readerIdleTimeNanos;
            if (!IdleStateHandler.this.reading) {
                nextDelay -= System.nanoTime() - IdleStateHandler.this.lastReadTime;
            }
            if (nextDelay <= 0L) {
                IdleStateHandler.this.readerIdleTimeout = this.ctx.executor().schedule(this, IdleStateHandler.this.readerIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event;
                    if (IdleStateHandler.this.firstReaderIdleEvent) {
                        IdleStateHandler.this.firstReaderIdleEvent = false;
                        event = IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.READER_IDLE_STATE_EVENT;
                    }
                    IdleStateHandler.this.channelIdle(this.ctx, event);
                }
                catch (Throwable t) {
                    this.ctx.fireExceptionCaught(t);
                }
            } else {
                IdleStateHandler.this.readerIdleTimeout = this.ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

}

