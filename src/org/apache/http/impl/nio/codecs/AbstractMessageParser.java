/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.MessageConstraintException;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.config.MessageConstraints;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.nio.NHttpMessageParser;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.params.HttpParamConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@NotThreadSafe
public abstract class AbstractMessageParser<T extends HttpMessage>
implements NHttpMessageParser<T> {
    private final SessionInputBuffer sessionBuffer;
    private static final int READ_HEAD_LINE = 0;
    private static final int READ_HEADERS = 1;
    private static final int COMPLETED = 2;
    private int state;
    private boolean endOfStream;
    private T message;
    private CharArrayBuffer lineBuf;
    private final List<CharArrayBuffer> headerBufs;
    protected final LineParser lineParser;
    private final MessageConstraints constraints;

    @Deprecated
    public AbstractMessageParser(SessionInputBuffer buffer, LineParser lineParser, HttpParams params) {
        Args.notNull(buffer, "Session input buffer");
        Args.notNull(params, "HTTP parameters");
        this.sessionBuffer = buffer;
        this.state = 0;
        this.endOfStream = false;
        this.headerBufs = new ArrayList<CharArrayBuffer>();
        this.constraints = HttpParamConfig.getMessageConstraints(params);
        this.lineParser = lineParser != null ? lineParser : BasicLineParser.INSTANCE;
    }

    public AbstractMessageParser(SessionInputBuffer buffer, LineParser lineParser, MessageConstraints constraints) {
        this.sessionBuffer = Args.notNull(buffer, "Session input buffer");
        this.lineParser = lineParser != null ? lineParser : BasicLineParser.INSTANCE;
        this.constraints = constraints != null ? constraints : MessageConstraints.DEFAULT;
        this.headerBufs = new ArrayList<CharArrayBuffer>();
        this.state = 0;
        this.endOfStream = false;
    }

    @Override
    public void reset() {
        this.state = 0;
        this.endOfStream = false;
        this.headerBufs.clear();
        this.message = null;
    }

    @Override
    public int fillBuffer(ReadableByteChannel channel) throws IOException {
        int bytesRead = this.sessionBuffer.fill(channel);
        if (bytesRead == -1) {
            this.endOfStream = true;
        }
        return bytesRead;
    }

    protected abstract T createMessage(CharArrayBuffer var1) throws HttpException, ParseException;

    private void parseHeadLine() throws HttpException, ParseException {
        this.message = this.createMessage(this.lineBuf);
    }

    private void parseHeader() throws IOException {
        CharArrayBuffer current = this.lineBuf;
        int count = this.headerBufs.size();
        if ((this.lineBuf.charAt(0) == ' ' || this.lineBuf.charAt(0) == '\t') && count > 0) {
            char ch;
            int i;
            CharArrayBuffer previous = this.headerBufs.get(count - 1);
            for (i = 0; i < current.length() && ((ch = current.charAt(i)) == ' ' || ch == '\t'); ++i) {
            }
            int maxLineLen = this.constraints.getMaxLineLength();
            if (maxLineLen > 0 && previous.length() + 1 + current.length() - i > maxLineLen) {
                throw new MessageConstraintException("Maximum line length limit exceeded");
            }
            previous.append(' ');
            previous.append(current, i, current.length() - i);
        } else {
            this.headerBufs.add(current);
            this.lineBuf = null;
        }
    }

    @Override
    public T parse() throws IOException, HttpException {
        while (this.state != 2) {
            if (this.lineBuf == null) {
                this.lineBuf = new CharArrayBuffer(64);
            } else {
                this.lineBuf.clear();
            }
            boolean lineComplete = this.sessionBuffer.readLine(this.lineBuf, this.endOfStream);
            int maxLineLen = this.constraints.getMaxLineLength();
            if (maxLineLen > 0 && (this.lineBuf.length() > maxLineLen || !lineComplete && this.sessionBuffer.length() > maxLineLen)) {
                throw new MessageConstraintException("Maximum line length limit exceeded");
            }
            if (!lineComplete) break;
            switch (this.state) {
                case 0: {
                    try {
                        this.parseHeadLine();
                    }
                    catch (ParseException px) {
                        throw new ProtocolException(px.getMessage(), px);
                    }
                    this.state = 1;
                    break;
                }
                case 1: {
                    if (this.lineBuf.length() > 0) {
                        int maxHeaderCount = this.constraints.getMaxHeaderCount();
                        if (maxHeaderCount > 0 && this.headerBufs.size() >= maxHeaderCount) {
                            throw new MessageConstraintException("Maximum header count exceeded");
                        }
                        this.parseHeader();
                        break;
                    }
                    this.state = 2;
                }
            }
            if (!this.endOfStream || this.sessionBuffer.hasData()) continue;
            this.state = 2;
        }
        if (this.state == 2) {
            for (CharArrayBuffer buffer : this.headerBufs) {
                try {
                    this.message.addHeader(this.lineParser.parseHeader(buffer));
                }
                catch (ParseException ex) {
                    throw new ProtocolException(ex.getMessage(), ex);
                }
            }
            return this.message;
        }
        return null;
    }
}

