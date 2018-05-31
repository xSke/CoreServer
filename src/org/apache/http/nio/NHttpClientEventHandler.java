/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;

public interface NHttpClientEventHandler {
    public void connected(NHttpClientConnection var1, Object var2) throws IOException, HttpException;

    public void requestReady(NHttpClientConnection var1) throws IOException, HttpException;

    public void responseReceived(NHttpClientConnection var1) throws IOException, HttpException;

    public void inputReady(NHttpClientConnection var1, ContentDecoder var2) throws IOException, HttpException;

    public void outputReady(NHttpClientConnection var1, ContentEncoder var2) throws IOException, HttpException;

    public void endOfInput(NHttpClientConnection var1) throws IOException;

    public void timeout(NHttpClientConnection var1) throws IOException, HttpException;

    public void closed(NHttpClientConnection var1);

    public void exception(NHttpClientConnection var1, Exception var2);
}

