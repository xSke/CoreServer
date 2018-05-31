/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ExpandableBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.CharsetUtils;

@NotThreadSafe
public class SessionInputBufferImpl
extends ExpandableBuffer
implements SessionInputBuffer {
    private final CharsetDecoder chardecoder;
    private final int lineBuffersize;
    private CharBuffer charbuffer;

    public SessionInputBufferImpl(int buffersize, int lineBuffersize, CharsetDecoder chardecoder, ByteBufferAllocator allocator) {
        super(buffersize, allocator != null ? allocator : HeapByteBufferAllocator.INSTANCE);
        this.lineBuffersize = Args.positive(lineBuffersize, "Line buffer size");
        this.chardecoder = chardecoder;
    }

    @Deprecated
    public SessionInputBufferImpl(int buffersize, int lineBuffersize, ByteBufferAllocator allocator, HttpParams params) {
        super(buffersize, allocator);
        this.lineBuffersize = Args.positive(lineBuffersize, "Line buffer size");
        String charsetName = (String)params.getParameter("http.protocol.element-charset");
        Charset charset = CharsetUtils.lookup(charsetName);
        if (charset != null) {
            this.chardecoder = charset.newDecoder();
            CodingErrorAction a1 = (CodingErrorAction)params.getParameter("http.malformed.input.action");
            this.chardecoder.onMalformedInput(a1 != null ? a1 : CodingErrorAction.REPORT);
            CodingErrorAction a2 = (CodingErrorAction)params.getParameter("http.unmappable.input.action");
            this.chardecoder.onUnmappableCharacter(a2 != null ? a2 : CodingErrorAction.REPORT);
        } else {
            this.chardecoder = null;
        }
    }

    @Deprecated
    public SessionInputBufferImpl(int buffersize, int linebuffersize, HttpParams params) {
        this(buffersize, linebuffersize, HeapByteBufferAllocator.INSTANCE, params);
    }

    public SessionInputBufferImpl(int buffersize, int lineBuffersize, Charset charset) {
        this(buffersize, lineBuffersize, charset != null ? charset.newDecoder() : null, HeapByteBufferAllocator.INSTANCE);
    }

    public SessionInputBufferImpl(int buffersize, int lineBuffersize) {
        this(buffersize, lineBuffersize, null, HeapByteBufferAllocator.INSTANCE);
    }

    public SessionInputBufferImpl(int buffersize) {
        this(buffersize, 256, null, HeapByteBufferAllocator.INSTANCE);
    }

    public int fill(ReadableByteChannel channel) throws IOException {
        Args.notNull(channel, "Channel");
        this.setInputMode();
        if (!this.buffer.hasRemaining()) {
            this.expand();
        }
        return channel.read(this.buffer);
    }

    public int read() {
        this.setOutputMode();
        return this.buffer.get() & 255;
    }

    public int read(ByteBuffer dst, int maxLen) {
        if (dst == null) {
            return 0;
        }
        this.setOutputMode();
        int len = Math.min(dst.remaining(), maxLen);
        int chunk = Math.min(this.buffer.remaining(), len);
        if (this.buffer.remaining() > chunk) {
            int oldLimit = this.buffer.limit();
            int newLimit = this.buffer.position() + chunk;
            this.buffer.limit(newLimit);
            dst.put(this.buffer);
            this.buffer.limit(oldLimit);
            return len;
        }
        dst.put(this.buffer);
        return chunk;
    }

    public int read(ByteBuffer dst) {
        if (dst == null) {
            return 0;
        }
        return this.read(dst, dst.remaining());
    }

    public int read(WritableByteChannel dst, int maxLen) throws IOException {
        int bytesRead;
        if (dst == null) {
            return 0;
        }
        this.setOutputMode();
        if (this.buffer.remaining() > maxLen) {
            int oldLimit = this.buffer.limit();
            int newLimit = oldLimit - (this.buffer.remaining() - maxLen);
            this.buffer.limit(newLimit);
            bytesRead = dst.write(this.buffer);
            this.buffer.limit(oldLimit);
        } else {
            bytesRead = dst.write(this.buffer);
        }
        return bytesRead;
    }

    public int read(WritableByteChannel dst) throws IOException {
        if (dst == null) {
            return 0;
        }
        this.setOutputMode();
        return dst.write(this.buffer);
    }

    public boolean readLine(CharArrayBuffer linebuffer, boolean endOfStream) throws CharacterCodingException {
        this.setOutputMode();
        int pos = -1;
        boolean hasLine = false;
        for (int i = this.buffer.position(); i < this.buffer.limit(); ++i) {
            byte b = this.buffer.get(i);
            if (b != 10) continue;
            hasLine = true;
            pos = i + 1;
            break;
        }
        if (!hasLine) {
            if (endOfStream && this.buffer.hasRemaining()) {
                pos = this.buffer.limit();
            } else {
                return false;
            }
        }
        int origLimit = this.buffer.limit();
        this.buffer.limit(pos);
        int requiredCapacity = this.buffer.limit() - this.buffer.position();
        linebuffer.ensureCapacity(requiredCapacity);
        if (this.chardecoder == null) {
            if (this.buffer.hasArray()) {
                byte[] b = this.buffer.array();
                int off = this.buffer.position();
                int len = this.buffer.remaining();
                linebuffer.append(b, off, len);
                this.buffer.position(off + len);
            } else {
                while (this.buffer.hasRemaining()) {
                    linebuffer.append((char)(this.buffer.get() & 255));
                }
            }
        } else {
            CoderResult result;
            if (this.charbuffer == null) {
                this.charbuffer = CharBuffer.allocate(this.lineBuffersize);
            }
            this.chardecoder.reset();
            do {
                if ((result = this.chardecoder.decode(this.buffer, this.charbuffer, true)).isError()) {
                    result.throwException();
                }
                if (!result.isOverflow()) continue;
                this.charbuffer.flip();
                linebuffer.append(this.charbuffer.array(), this.charbuffer.position(), this.charbuffer.remaining());
                this.charbuffer.clear();
            } while (!result.isUnderflow());
            this.chardecoder.flush(this.charbuffer);
            this.charbuffer.flip();
            if (this.charbuffer.hasRemaining()) {
                linebuffer.append(this.charbuffer.array(), this.charbuffer.position(), this.charbuffer.remaining());
            }
        }
        this.buffer.limit(origLimit);
        int l = linebuffer.length();
        if (l > 0) {
            if (linebuffer.charAt(l - 1) == '\n') {
                linebuffer.setLength(--l);
            }
            if (l > 0 && linebuffer.charAt(l - 1) == '\r') {
                linebuffer.setLength(--l);
            }
        }
        return true;
    }

    public String readLine(boolean endOfStream) throws CharacterCodingException {
        CharArrayBuffer buffer = new CharArrayBuffer(64);
        boolean found = this.readLine(buffer, endOfStream);
        if (found) {
            return buffer.toString();
        }
        return null;
    }
}

