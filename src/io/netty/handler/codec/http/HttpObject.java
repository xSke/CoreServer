/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.DecoderResult;

public interface HttpObject {
    public DecoderResult getDecoderResult();

    public void setDecoderResult(DecoderResult var1);
}

