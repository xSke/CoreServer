/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import org.apache.http.annotation.Immutable;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.IOReactorUtils;
import org.apache.http.impl.nio.client.MinimalHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.util.Args;

@Immutable
public class HttpAsyncClients {
    private HttpAsyncClients() {
    }

    public static HttpAsyncClientBuilder custom() {
        return HttpAsyncClientBuilder.create();
    }

    public static CloseableHttpAsyncClient createDefault() {
        return HttpAsyncClientBuilder.create().build();
    }

    public static CloseableHttpAsyncClient createSystem() {
        return HttpAsyncClientBuilder.create().useSystemProperties().build();
    }

    public static CloseableHttpAsyncClient createMinimal() {
        return new MinimalHttpAsyncClient(new PoolingNHttpClientConnectionManager(IOReactorUtils.create(IOReactorConfig.DEFAULT)));
    }

    public static CloseableHttpAsyncClient createMinimal(ConnectingIOReactor ioreactor) {
        Args.notNull(ioreactor, "I/O reactor");
        return new MinimalHttpAsyncClient(new PoolingNHttpClientConnectionManager(ioreactor));
    }

    public static CloseableHttpAsyncClient createMinimal(NHttpClientConnectionManager connManager) {
        Args.notNull(connManager, "Connection manager");
        return new MinimalHttpAsyncClient(connManager);
    }
}

