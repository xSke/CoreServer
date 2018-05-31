/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.ParseException;
import org.apache.http.TruncatedChunkException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.AbstractContentDecoder;
import org.apache.http.message.BufferedHeader;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
public class ChunkDecoder
extends AbstractContentDecoder {
    private static final int READ_CONTENT = 0;
    private static final int READ_FOOTERS = 1;
    private static final int COMPLETED = 2;
    private int state = 0;
    private boolean endOfChunk = false;
    private boolean endOfStream = false;
    private CharArrayBuffer lineBuf;
    private int chunkSize = -1;
    private int pos = 0;
    private final List<CharArrayBuffer> trailerBufs = new ArrayList<CharArrayBuffer>();
    private Header[] footers;

    public ChunkDecoder(ReadableByteChannel channel, SessionInputBuffer buffer, HttpTransportMetricsImpl metrics) {
        super(channel, buffer, metrics);
    }

    private void readChunkHead() throws IOException {
        if (this.lineBuf == null) {
            this.lineBuf = new CharArrayBuffer(32);
        } else {
            this.lineBuf.clear();
        }
        if (this.endOfChunk) {
            if (this.buffer.readLine(this.lineBuf, this.endOfStream)) {
                if (!this.lineBuf.isEmpty()) {
                    throw new MalformedChunkCodingException("CRLF expected at end of chunk");
                }
            } else {
                if (this.buffer.length() > 2) {
                    throw new MalformedChunkCodingException("CRLF expected at end of chunk");
                }
                return;
            }
            this.endOfChunk = false;
        }
        if (this.buffer.readLine(this.lineBuf, this.endOfStream)) {
            int separator = this.lineBuf.indexOf(59);
            if (separator < 0) {
                separator = this.lineBuf.length();
            }
            try {
                String s = this.lineBuf.substringTrimmed(0, separator);
                this.chunkSize = Integer.parseInt(s, 16);
            }
            catch (NumberFormatException e) {
                throw new MalformedChunkCodingException("Bad chunk header");
            }
            this.pos = 0;
        }
    }

    private void parseHeader() {
        CharArrayBuffer current = this.lineBuf;
        int count = this.trailerBufs.size();
        if ((this.lineBuf.charAt(0) == ' ' || this.lineBuf.charAt(0) == '\t') && count > 0) {
            char ch;
            int i;
            CharArrayBuffer previous = this.trailerBufs.get(count - 1);
            for (i = 0; i < current.length() && ((ch = current.charAt(i)) == ' ' || ch == '\t'); ++i) {
            }
            previous.append(' ');
            previous.append(current, i, current.length() - i);
        } else {
            this.trailerBufs.add(current);
            this.lineBuf = null;
        }
    }

    private void processFooters() throws IOException {
        int count = this.trailerBufs.size();
        if (count > 0) {
            this.footers = new Header[this.trailerBufs.size()];
            for (int i = 0; i < this.trailerBufs.size(); ++i) {
                try {
                    this.footers[i] = new BufferedHeader(this.trailerBufs.get(i));
                    continue;
                }
                catch (ParseException ex) {
                    throw new IOException(ex.getMessage());
                }
            }
        }
        this.trailerBufs.clear();
    }

    public int read(ByteBuffer dst) throws IOException {
        Args.notNull(dst, "Byte buffer");
        if (this.state == 2) {
            return -1;
        }
        int totalRead = 0;
        while (this.state != 2) {
            int bytesRead;
            if (!(this.buffer.hasData() && this.chunkSize != -1 || (bytesRead = this.fillBufferFromChannel()) != -1)) {
                this.endOfStream = true;
            }
            switch (this.state) {
                case 0: {
                    int maxLen;
                    int len;
                    if (this.chunkSize == -1) {
                        this.readChunkHead();
                        if (this.chunkSize == -1) {
                            if (this.endOfStream) {
                                this.state = 2;
                                this.completed = true;
                            }
                            return totalRead;
                        }
                        if (this.chunkSize == 0) {
                            this.chunkSize = -1;
                            this.state = 1;
                            break;
                        }
                    }
                    if ((len = this.buffer.read(dst, maxLen = this.chunkSize - this.pos)) > 0) {
                        this.pos += len;
                        totalRead += len;
                    } else if (!this.buffer.hasData() && this.endOfStream) {
                        this.state = 2;
                        this.completed = true;
                        throw new TruncatedChunkException("Truncated chunk ( expected size: " + this.chunkSize + "; actual size: " + this.pos + ")");
                    }
                    if (this.pos == this.chunkSize) {
                        this.chunkSize = -1;
                        this.pos = 0;
                        this.endOfChunk = true;
                        break;
                    }
                    return totalRead;
                }
                case 1: {
                    if (this.lineBuf == null) {
                        this.lineBuf = new CharArrayBuffer(32);
                    } else {
                        this.lineBuf.clear();
                    }
                    if (!this.buffer.readLine(this.lineBuf, this.endOfStream)) {
                        if (this.endOfStream) {
                            this.state = 2;
                            this.completed = true;
                        }
                        return totalRead;
                    }
                    if (this.lineBuf.length() > 0) {
                        this.parseHeader();
                        break;
                    }
                    this.state = 2;
                    this.completed = true;
                    this.processFooters();
                }
            }
        }
        return totalRead;
    }

    public Header[] getFooters() {
        if (this.footers != null) {
            return (Header[])this.footers.clone();
        }
        return new Header[0];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[chunk-coded; completed: ");
        sb.append(this.completed);
        sb.append("]");
        return sb.toString();
    }
}

