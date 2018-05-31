/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;

public interface NHttpMessageParser<T extends HttpMessage> {
    public void reset();

    public int fillBuffer(ReadableByteChannel var1) throws IOException;

    public T parse() throws IOException, HttpException;
}

