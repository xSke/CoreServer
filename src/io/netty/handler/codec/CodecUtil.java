/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec;

import io.netty.channel.ChannelHandlerAdapter;

final class CodecUtil {
    static void ensureNotSharable(ChannelHandlerAdapter handler) {
        if (handler.isSharable()) {
            throw new IllegalStateException("@Sharable annotation is not allowed");
        }
    }

    private CodecUtil() {
    }
}

