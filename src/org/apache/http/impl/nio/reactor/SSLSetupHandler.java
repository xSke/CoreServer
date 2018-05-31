/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.params.HttpParams;

@Deprecated
public interface SSLSetupHandler {
    public void initalize(SSLEngine var1, HttpParams var2) throws SSLException;

    public void verify(IOSession var1, SSLSession var2) throws SSLException;
}

