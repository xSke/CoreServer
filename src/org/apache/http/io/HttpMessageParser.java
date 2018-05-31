/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.io;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;

public interface HttpMessageParser<T extends HttpMessage> {
    public T parse() throws IOException, HttpException;
}

