/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.impl.nio.codecs.AbstractMessageWriter;
import org.apache.http.message.LineFormatter;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class HttpRequestWriter
extends AbstractMessageWriter {
    public HttpRequestWriter(SessionOutputBuffer buffer, LineFormatter formatter, HttpParams params) {
        super(buffer, formatter, params);
    }

    protected void writeHeadLine(HttpMessage message) throws IOException {
        CharArrayBuffer buffer = this.lineFormatter.formatRequestLine(this.lineBuf, ((HttpRequest)message).getRequestLine());
        this.sessionBuffer.writeLine(buffer);
    }
}

