/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import org.apache.http.nio.conn.ClientAsyncConnection;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.params.HttpParams;

@Deprecated
public interface ClientAsyncConnectionFactory {
    public ClientAsyncConnection create(String var1, IOSession var2, HttpParams var3);
}

