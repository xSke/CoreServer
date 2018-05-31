/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.impl.nio.codecs.AbstractMessageWriter;
import org.apache.http.message.LineFormatter;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@NotThreadSafe
public class DefaultHttpRequestWriter
extends AbstractMessageWriter<HttpRequest> {
    @Deprecated
    public DefaultHttpRequestWriter(SessionOutputBuffer buffer, LineFormatter formatter, HttpParams params) {
        super(buffer, formatter, params);
    }

    public DefaultHttpRequestWriter(SessionOutputBuffer buffer, LineFormatter formatter) {
        super(buffer, formatter);
    }

    public DefaultHttpRequestWriter(SessionOutputBuffer buffer) {
        super(buffer, null);
    }

    @Override
    protected void writeHeadLine(HttpRequest message) throws IOException {
        CharArrayBuffer buffer = this.lineFormatter.formatRequestLine(this.lineBuf, message.getRequestLine());
        this.sessionBuffer.writeLine(buffer);
    }
}

