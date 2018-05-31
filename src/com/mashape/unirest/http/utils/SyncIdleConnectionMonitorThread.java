/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http.utils;

import java.util.concurrent.TimeUnit;
import org.apache.http.conn.HttpClientConnectionManager;

public class SyncIdleConnectionMonitorThread
extends Thread {
    private final HttpClientConnectionManager connMgr;

    public SyncIdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
        super.setDaemon(true);
        this.connMgr = connMgr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                SyncIdleConnectionMonitorThread syncIdleConnectionMonitorThread = this;
                synchronized (syncIdleConnectionMonitorThread) {
                    this.wait(5000L);
                    this.connMgr.closeExpiredConnections();
                    this.connMgr.closeIdleConnections(30L, TimeUnit.SECONDS);
                }
            }
            return;
        }
        catch (InterruptedException ex) {
            // empty catch block
        }
    }
}

