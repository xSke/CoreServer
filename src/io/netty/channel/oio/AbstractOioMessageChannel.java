/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.oio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.AbstractOioChannel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOioMessageChannel
extends AbstractOioChannel {
    private final List<Object> readBuf = new ArrayList<Object>();

    protected AbstractOioMessageChannel(Channel parent) {
        super(parent);
    }

    @Override
    protected void doRead() {
        int localRead;
        boolean closed;
        ChannelPipeline pipeline;
        Throwable exception;
        ChannelConfig config = this.config();
        pipeline = this.pipeline();
        closed = false;
        int maxMessagesPerRead = config.getMaxMessagesPerRead();
        exception = null;
        localRead = 0;
        int totalRead = 0;
        try {
            while ((localRead = this.doReadMessages(this.readBuf)) != 0) {
                if (localRead < 0) {
                    closed = true;
                } else {
                    int size = this.readBuf.size();
                    for (int i = 0; i < size; ++i) {
                        pipeline.fireChannelRead(this.readBuf.get(i));
                    }
                    this.readBuf.clear();
                    if ((totalRead += localRead) < maxMessagesPerRead && config.isAutoRead()) continue;
                }
                break;
            }
        }
        catch (Throwable t) {
            exception = t;
        }
        pipeline.fireChannelReadComplete();
        if (exception != null) {
            if (exception instanceof IOException) {
                closed = true;
            }
            this.pipeline().fireExceptionCaught(exception);
        }
        if (closed) {
            if (this.isOpen()) {
                this.unsafe().close(this.unsafe().voidPromise());
            }
        } else if (localRead == 0 && this.isActive()) {
            this.read();
        }
    }

    protected abstract int doReadMessages(List<Object> var1) throws Exception;
}

