/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.binary;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.binary.BaseNCodec;

public class BaseNCodecInputStream
extends FilterInputStream {
    private final boolean doEncode;
    private final BaseNCodec baseNCodec;
    private final byte[] singleByte = new byte[1];

    protected BaseNCodecInputStream(InputStream in, BaseNCodec baseNCodec, boolean doEncode) {
        super(in);
        this.doEncode = doEncode;
        this.baseNCodec = baseNCodec;
    }

    public int read() throws IOException {
        int r = this.read(this.singleByte, 0, 1);
        while (r == 0) {
            r = this.read(this.singleByte, 0, 1);
        }
        if (r > 0) {
            return this.singleByte[0] < 0 ? 256 + this.singleByte[0] : this.singleByte[0];
        }
        return -1;
    }

    public int read(byte[] b, int offset, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (offset > b.length || offset + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        int readLen = 0;
        while (readLen == 0) {
            if (!this.baseNCodec.hasData()) {
                byte[] buf = new byte[this.doEncode ? 4096 : 8192];
                int c = this.in.read(buf);
                if (this.doEncode) {
                    this.baseNCodec.encode(buf, 0, c);
                } else {
                    this.baseNCodec.decode(buf, 0, c);
                }
            }
            readLen = this.baseNCodec.readResults(b, offset, len);
        }
        return readLen;
    }

    public boolean markSupported() {
        return false;
    }
}

