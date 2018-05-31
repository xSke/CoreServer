/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.InternalIODispatch;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.IOEventDispatch;

abstract class CloseableHttpAsyncClientBase
extends CloseableHttpAsyncClient {
    private final Log log = LogFactory.getLog(this.getClass());
    private final NHttpClientConnectionManager connmgr;
    private final Thread reactorThread;
    private final AtomicReference<Status> status;

    public CloseableHttpAsyncClientBase(NHttpClientConnectionManager connmgr, ThreadFactory threadFactory) {
        this.connmgr = connmgr;
        this.reactorThread = threadFactory.newThread(new Runnable(){

            public void run() {
                CloseableHttpAsyncClientBase.this.doExecute();
            }
        });
        this.status = new AtomicReference<Status>(Status.INACTIVE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doExecute() {
        try {
            InternalIODispatch ioEventDispatch = new InternalIODispatch();
            this.connmgr.execute(ioEventDispatch);
        }
        catch (Exception ex) {
            this.log.error("I/O reactor terminated abnormally", ex);
        }
        finally {
            this.status.set(Status.STOPPED);
        }
    }

    public void start() {
        if (this.status.compareAndSet(Status.INACTIVE, Status.ACTIVE)) {
            this.reactorThread.start();
        }
    }

    public void shutdown() {
        if (this.status.compareAndSet(Status.ACTIVE, Status.STOPPED)) {
            try {
                this.connmgr.shutdown();
            }
            catch (IOException ex) {
                this.log.error("I/O error shutting down connection manager", ex);
            }
            try {
                this.reactorThread.join();
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void close() {
        this.shutdown();
    }

    public boolean isRunning() {
        return this.getStatus() == Status.ACTIVE;
    }

    Status getStatus() {
        return this.status.get();
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static enum Status {
        INACTIVE,
        ACTIVE,
        STOPPED;
        

        private Status() {
        }
    }

}

