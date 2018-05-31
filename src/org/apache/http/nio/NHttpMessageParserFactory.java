/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import org.apache.http.HttpMessage;
import org.apache.http.config.MessageConstraints;
import org.apache.http.nio.NHttpMessageParser;
import org.apache.http.nio.reactor.SessionInputBuffer;

public interface NHttpMessageParserFactory<T extends HttpMessage> {
    public NHttpMessageParser<T> create(SessionInputBuffer var1, MessageConstraints var2);
}

