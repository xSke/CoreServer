/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCounted;

public class PingWebSocketFrame
extends WebSocketFrame {
    public PingWebSocketFrame() {
        super(true, 0, Unpooled.buffer(0));
    }

    public PingWebSocketFrame(ByteBuf binaryData) {
        super(binaryData);
    }

    public PingWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    @Override
    public PingWebSocketFrame copy() {
        return new PingWebSocketFrame(this.isFinalFragment(), this.rsv(), this.content().copy());
    }

    @Override
    public PingWebSocketFrame duplicate() {
        return new PingWebSocketFrame(this.isFinalFragment(), this.rsv(), this.content().duplicate());
    }

    @Override
    public PingWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public PingWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }
}

