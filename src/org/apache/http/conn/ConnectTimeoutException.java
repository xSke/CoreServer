/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.conn;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.Arrays;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;

@Immutable
public class ConnectTimeoutException
extends InterruptedIOException {
    private static final long serialVersionUID = -4816682903149535989L;
    private final HttpHost host;

    public ConnectTimeoutException() {
        this.host = null;
    }

    public ConnectTimeoutException(String message) {
        super(message);
        this.host = null;
    }

    public /* varargs */ ConnectTimeoutException(IOException cause, HttpHost host, InetAddress ... remoteAddresses) {
        super("Connect to " + (host != null ? host.toHostString() : "remote host") + (remoteAddresses != null && remoteAddresses.length > 0 ? new StringBuilder().append(" ").append(Arrays.asList(remoteAddresses)).toString() : "") + (cause != null && cause.getMessage() != null ? new StringBuilder().append(" failed: ").append(cause.getMessage()).toString() : " timed out"));
        this.host = host;
        this.initCause(cause);
    }

    public HttpHost getHost() {
        return this.host;
    }
}

