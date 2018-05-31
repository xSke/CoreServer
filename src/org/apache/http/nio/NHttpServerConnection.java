/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.NHttpConnection;

public interface NHttpServerConnection
extends NHttpConnection {
    public void submitResponse(HttpResponse var1) throws IOException, HttpException;

    public boolean isResponseSubmitted();

    public void resetInput();

    public void resetOutput();
}

