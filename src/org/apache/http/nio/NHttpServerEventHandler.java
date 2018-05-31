/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpServerConnection;

public interface NHttpServerEventHandler {
    public void connected(NHttpServerConnection var1) throws IOException, HttpException;

    public void requestReceived(NHttpServerConnection var1) throws IOException, HttpException;

    public void inputReady(NHttpServerConnection var1, ContentDecoder var2) throws IOException, HttpException;

    public void responseReady(NHttpServerConnection var1) throws IOException, HttpException;

    public void outputReady(NHttpServerConnection var1, ContentEncoder var2) throws IOException, HttpException;

    public void endOfInput(NHttpServerConnection var1) throws IOException;

    public void timeout(NHttpServerConnection var1) throws IOException;

    public void closed(NHttpServerConnection var1);

    public void exception(NHttpServerConnection var1, Exception var2);
}

