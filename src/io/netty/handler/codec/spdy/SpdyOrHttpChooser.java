/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.spdy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.spdy.SpdyVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.List;

public abstract class SpdyOrHttpChooser
extends ByteToMessageDecoder {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SpdyOrHttpChooser.class);

    protected SpdyOrHttpChooser() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (this.configurePipeline(ctx)) {
            ctx.pipeline().remove(this);
        }
    }

    private boolean configurePipeline(ChannelHandlerContext ctx) {
        SelectedProtocol protocol;
        SslHandler handler = ctx.pipeline().get(SslHandler.class);
        if (handler == null) {
            throw new IllegalStateException("cannot find a SslHandler in the pipeline (required for SPDY)");
        }
        if (!handler.handshakeFuture().isDone()) {
            return false;
        }
        try {
            protocol = this.selectProtocol(handler);
        }
        catch (Exception e) {
            throw new IllegalStateException("failed to get the selected protocol", e);
        }
        if (protocol == null) {
            throw new IllegalStateException("unknown protocol");
        }
        switch (protocol) {
            case SPDY_3_1: {
                try {
                    this.configureSpdy(ctx, SpdyVersion.SPDY_3_1);
                    break;
                }
                catch (Exception e) {
                    throw new IllegalStateException("failed to configure a SPDY pipeline", e);
                }
            }
            case HTTP_1_0: 
            case HTTP_1_1: {
                try {
                    this.configureHttp1(ctx);
                    break;
                }
                catch (Exception e) {
                    throw new IllegalStateException("failed to configure a HTTP/1 pipeline", e);
                }
            }
        }
        return true;
    }

    protected SelectedProtocol selectProtocol(SslHandler sslHandler) throws Exception {
        String appProto = sslHandler.applicationProtocol();
        return appProto != null ? SelectedProtocol.protocol(appProto) : SelectedProtocol.HTTP_1_1;
    }

    protected abstract void configureSpdy(ChannelHandlerContext var1, SpdyVersion var2) throws Exception;

    protected abstract void configureHttp1(ChannelHandlerContext var1) throws Exception;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("{} Failed to select the application-level protocol:", (Object)ctx.channel(), (Object)cause);
        ctx.close();
    }

    public static enum SelectedProtocol {
        SPDY_3_1("spdy/3.1"),
        HTTP_1_1("http/1.1"),
        HTTP_1_0("http/1.0");
        
        private final String name;

        private SelectedProtocol(String defaultName) {
            this.name = defaultName;
        }

        public String protocolName() {
            return this.name;
        }

        public static SelectedProtocol protocol(String name) {
            for (SelectedProtocol protocol : SelectedProtocol.values()) {
                if (!protocol.protocolName().equals(name)) continue;
                return protocol;
            }
            return null;
        }
    }

}

