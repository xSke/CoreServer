/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;

@Deprecated
public interface NHttpClientHandler {
    public void connected(NHttpClientConnection var1, Object var2);

    public void requestReady(NHttpClientConnection var1);

    public void responseReceived(NHttpClientConnection var1);

    public void inputReady(NHttpClientConnection var1, ContentDecoder var2);

    public void outputReady(NHttpClientConnection var1, ContentEncoder var2);

    public void exception(NHttpClientConnection var1, IOException var2);

    public void exception(NHttpClientConnection var1, HttpException var2);

    public void timeout(NHttpClientConnection var1);

    public void closed(NHttpClientConnection var1);
}

