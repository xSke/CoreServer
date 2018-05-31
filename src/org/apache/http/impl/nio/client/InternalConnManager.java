/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import org.apache.http.nio.NHttpClientConnection;

interface InternalConnManager {
    public void releaseConnection();

    public void abortConnection();

    public NHttpClientConnection getConnection();
}

