/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;

@Deprecated
public interface HttpAsyncRequestExecutionHandler<T>
extends HttpAsyncRequestProducer,
HttpAsyncResponseConsumer<T> {
    public HttpContext getContext();

    public HttpProcessor getHttpProcessor();

    public ConnectionReuseStrategy getConnectionReuseStrategy();
}

