/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http.websocketx;

public class WebSocketHandshakeException
extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WebSocketHandshakeException(String s) {
        super(s);
    }

    public WebSocketHandshakeException(String s, Throwable throwable) {
        super(s, throwable);
    }
}

