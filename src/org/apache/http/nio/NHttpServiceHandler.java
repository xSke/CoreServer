/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpServerConnection;

@Deprecated
public interface NHttpServiceHandler {
    public void connected(NHttpServerConnection var1);

    public void requestReceived(NHttpServerConnection var1);

    public void inputReady(NHttpServerConnection var1, ContentDecoder var2);

    public void responseReady(NHttpServerConnection var1);

    public void outputReady(NHttpServerConnection var1, ContentEncoder var2);

    public void exception(NHttpServerConnection var1, IOException var2);

    public void exception(NHttpServerConnection var1, HttpException var2);

    public void timeout(NHttpServerConnection var1);

    public void closed(NHttpServerConnection var1);
}

