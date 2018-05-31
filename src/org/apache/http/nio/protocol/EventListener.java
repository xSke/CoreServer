/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.nio.NHttpConnection;

@Deprecated
public interface EventListener {
    public void fatalIOException(IOException var1, NHttpConnection var2);

    public void fatalProtocolException(HttpException var1, NHttpConnection var2);

    public void connectionOpen(NHttpConnection var1);

    public void connectionClosed(NHttpConnection var1);

    public void connectionTimeout(NHttpConnection var1);
}

