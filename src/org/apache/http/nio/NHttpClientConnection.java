/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.NHttpConnection;

public interface NHttpClientConnection
extends NHttpConnection {
    public void submitRequest(HttpRequest var1) throws IOException, HttpException;

    public boolean isRequestSubmitted();

    public void resetOutput();

    public void resetInput();
}

