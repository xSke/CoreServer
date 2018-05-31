/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;

public interface NHttpMessageWriter<T extends HttpMessage> {
    public void reset();

    public void write(T var1) throws IOException, HttpException;
}

