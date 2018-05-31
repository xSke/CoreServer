/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import org.apache.http.annotation.Immutable;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public abstract class AbstractIODispatch<T>
implements IOEventDispatch {
    protected abstract T createConnection(IOSession var1);

    protected abstract void onConnected(T var1);

    protected abstract void onClosed(T var1);

    protected abstract void onException(T var1, IOException var2);

    protected abstract void onInputReady(T var1);

    protected abstract void onOutputReady(T var1);

    protected abstract void onTimeout(T var1);

    private void ensureNotNull(T conn) {
        Asserts.notNull(conn, "HTTP connection");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void connected(IOSession session) {
        block9 : {
            Object conn = session.getAttribute("http.connection");
            try {
                if (conn == null) {
                    conn = this.createConnection(session);
                    session.setAttribute("http.connection", conn);
                }
                this.onConnected(conn);
                SSLIOSession ssliosession = (SSLIOSession)session.getAttribute("http.session.ssl");
                if (ssliosession == null) break block9;
                try {
                    SSLIOSession sSLIOSession = ssliosession;
                    synchronized (sSLIOSession) {
                        if (!ssliosession.isInitialized()) {
                            ssliosession.initialize();
                        }
                    }
                }
                catch (IOException ex) {
                    this.onException(conn, ex);
                    ssliosession.shutdown();
                }
            }
            catch (RuntimeException ex) {
                session.shutdown();
                throw ex;
            }
        }
    }

    @Override
    public void disconnected(IOSession session) {
        Object conn = session.getAttribute("http.connection");
        if (conn != null) {
            this.onClosed(conn);
        }
    }

    @Override
    public void inputReady(IOSession session) {
        Object conn = session.getAttribute("http.connection");
        try {
            this.ensureNotNull(conn);
            SSLIOSession ssliosession = (SSLIOSession)session.getAttribute("http.session.ssl");
            if (ssliosession == null) {
                this.onInputReady(conn);
            } else {
                try {
                    if (!ssliosession.isInitialized()) {
                        ssliosession.initialize();
                    }
                    if (ssliosession.isAppInputReady()) {
                        this.onInputReady(conn);
                    }
                    ssliosession.inboundTransport();
                }
                catch (IOException ex) {
                    this.onException(conn, ex);
                    ssliosession.shutdown();
                }
            }
        }
        catch (RuntimeException ex) {
            session.shutdown();
            throw ex;
        }
    }

    @Override
    public void outputReady(IOSession session) {
        Object conn = session.getAttribute("http.connection");
        try {
            this.ensureNotNull(conn);
            SSLIOSession ssliosession = (SSLIOSession)session.getAttribute("http.session.ssl");
            if (ssliosession == null) {
                this.onOutputReady(conn);
            } else {
                try {
                    if (!ssliosession.isInitialized()) {
                        ssliosession.initialize();
                    }
                    if (ssliosession.isAppOutputReady()) {
                        this.onOutputReady(conn);
                    }
                    ssliosession.outboundTransport();
                }
                catch (IOException ex) {
                    this.onException(conn, ex);
                    ssliosession.shutdown();
                }
            }
        }
        catch (RuntimeException ex) {
            session.shutdown();
            throw ex;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void timeout(IOSession session) {
        block6 : {
            Object conn = session.getAttribute("http.connection");
            try {
                SSLIOSession ssliosession = (SSLIOSession)session.getAttribute("http.session.ssl");
                this.ensureNotNull(conn);
                this.onTimeout(conn);
                if (ssliosession == null) break block6;
                SSLIOSession sSLIOSession = ssliosession;
                synchronized (sSLIOSession) {
                    if (ssliosession.isOutboundDone() && !ssliosession.isInboundDone()) {
                        ssliosession.shutdown();
                    }
                }
            }
            catch (RuntimeException ex) {
                session.shutdown();
                throw ex;
            }
        }
    }
}

