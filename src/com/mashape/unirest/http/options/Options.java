/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http.options;

import com.mashape.unirest.http.async.utils.AsyncIdleConnectionMonitorThread;
import com.mashape.unirest.http.options.Option;
import com.mashape.unirest.http.utils.SyncIdleConnectionMonitorThread;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

public class Options {
    public static final long CONNECTION_TIMEOUT = 10000L;
    private static final long SOCKET_TIMEOUT = 60000L;
    public static final int MAX_TOTAL = 200;
    public static final int MAX_PER_ROUTE = 20;
    private static Map<Option, Object> options = new HashMap<Option, Object>();
    private static boolean customClientSet = false;

    public static void customClientSet() {
        customClientSet = true;
    }

    public static void setOption(Option option, Object value) {
        if ((option == Option.CONNECTION_TIMEOUT || option == Option.SOCKET_TIMEOUT) && customClientSet) {
            throw new RuntimeException("You can't set custom timeouts when providing custom client implementations. Set the timeouts directly in your custom client configuration instead.");
        }
        options.put(option, value);
    }

    public static Object getOption(Option option) {
        return options.get((Object)option);
    }

    public static void refresh() {
        Object maxTotal;
        Object maxPerRoute;
        Object socketTimeout;
        PoolingNHttpClientConnectionManager asyncConnectionManager;
        Object connectionTimeout = Options.getOption(Option.CONNECTION_TIMEOUT);
        if (connectionTimeout == null) {
            connectionTimeout = 10000L;
        }
        if ((socketTimeout = Options.getOption(Option.SOCKET_TIMEOUT)) == null) {
            socketTimeout = 60000L;
        }
        if ((maxTotal = Options.getOption(Option.MAX_TOTAL)) == null) {
            maxTotal = 200;
        }
        if ((maxPerRoute = Options.getOption(Option.MAX_PER_ROUTE)) == null) {
            maxPerRoute = 20;
        }
        HttpHost proxy = (HttpHost)Options.getOption(Option.PROXY);
        RequestConfig clientConfig = RequestConfig.custom().setConnectTimeout(((Long)connectionTimeout).intValue()).setSocketTimeout(((Long)socketTimeout).intValue()).setConnectionRequestTimeout(((Long)socketTimeout).intValue()).setProxy(proxy).build();
        PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager();
        syncConnectionManager.setMaxTotal((Integer)maxTotal);
        syncConnectionManager.setDefaultMaxPerRoute((Integer)maxPerRoute);
        Options.setOption(Option.HTTPCLIENT, HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).setConnectionManager(syncConnectionManager).build());
        SyncIdleConnectionMonitorThread syncIdleConnectionMonitorThread = new SyncIdleConnectionMonitorThread(syncConnectionManager);
        Options.setOption(Option.SYNC_MONITOR, syncIdleConnectionMonitorThread);
        syncIdleConnectionMonitorThread.start();
        try {
            DefaultConnectingIOReactor ioreactor = new DefaultConnectingIOReactor();
            asyncConnectionManager = new PoolingNHttpClientConnectionManager(ioreactor);
            asyncConnectionManager.setMaxTotal((Integer)maxTotal);
            asyncConnectionManager.setDefaultMaxPerRoute((Integer)maxPerRoute);
        }
        catch (IOReactorException e) {
            throw new RuntimeException(e);
        }
        CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create().setDefaultRequestConfig(clientConfig).setConnectionManager(asyncConnectionManager).build();
        Options.setOption(Option.ASYNCHTTPCLIENT, asyncClient);
        Options.setOption(Option.ASYNC_MONITOR, new AsyncIdleConnectionMonitorThread(asyncConnectionManager));
    }

    static {
        Options.refresh();
    }
}

