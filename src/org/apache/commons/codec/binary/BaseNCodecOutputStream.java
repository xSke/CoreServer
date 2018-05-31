/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.binary;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.codec.binary.BaseNCodec;

public class BaseNCodecOutputStream
extends FilterOutputStream {
    private final boolean doEncode;
    private final BaseNCodec baseNCodec;
    private final byte[] singleByte = new byte[1];

    public BaseNCodecOutputStream(OutputStream out, BaseNCodec basedCodec, boolean doEncode) {
        super(out);
        this.baseNCodec = basedCodec;
        this.doEncode = doEncode;
    }

    public void write(int i) throws IOException {
        this.singleByte[0] = (byte)i;
        this.write(this.singleByte, 0, 1);
    }

    public void write(byte[] b, int offset, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (offset > b.length || offset + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > 0) {
            if (this.doEncode) {
                this.baseNCodec.encode(b, offset, len);
            } else {
                this.baseNCodec.decode(b, offset, len);
            }
            this.flush(false);
        }
    }

    private void flush(boolean propogate) throws IOException {
        byte[] buf;
        int c;
        int avail = this.baseNCodec.available();
        if (avail > 0 && (c = this.baseNCodec.readResults(buf = new byte[avail], 0, avail)) > 0) {
            this.out.write(buf, 0, c);
        }
        if (propogate) {
            this.out.flush();
        }
    }

    public void flush() throws IOException {
        this.flush(true);
    }

    public void close() throws IOException {
        if (this.doEncode) {
            this.baseNCodec.encode(this.singleByte, 0, -1);
        } else {
            this.baseNCodec.decode(this.singleByte, 0, -1);
        }
        this.flush();
        this.out.close();
    }
}

