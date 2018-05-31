/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.nio.ByteBuffer;
import org.apache.commons.logging.Log;

class Wire {
    private final Log log;
    private final String id;

    public Wire(Log log, String id) {
        this.log = log;
        this.id = id;
    }

    private void wire(String header, byte[] b, int pos, int off) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < off; ++i) {
            byte ch = b[pos + i];
            if (ch == 13) {
                buffer.append("[\\r]");
                continue;
            }
            if (ch == 10) {
                buffer.append("[\\n]\"");
                buffer.insert(0, "\"");
                buffer.insert(0, header);
                this.log.debug(this.id + " " + buffer.toString());
                buffer.setLength(0);
                continue;
            }
            if (ch < 32 || ch > 127) {
                buffer.append("[0x");
                buffer.append(Integer.toHexString(ch));
                buffer.append("]");
                continue;
            }
            buffer.append((char)ch);
        }
        if (buffer.length() > 0) {
            buffer.append('\"');
            buffer.insert(0, '\"');
            buffer.insert(0, header);
            this.log.debug(this.id + " " + buffer.toString());
        }
    }

    public boolean isEnabled() {
        return this.log.isDebugEnabled();
    }

    public void output(byte[] b, int pos, int off) {
        this.wire(">> ", b, pos, off);
    }

    public void input(byte[] b, int pos, int off) {
        this.wire("<< ", b, pos, off);
    }

    public void output(byte[] b) {
        this.output(b, 0, b.length);
    }

    public void input(byte[] b) {
        this.input(b, 0, b.length);
    }

    public void output(int b) {
        this.output(new byte[]{(byte)b});
    }

    public void input(int b) {
        this.input(new byte[]{(byte)b});
    }

    public void output(ByteBuffer b) {
        if (b.hasArray()) {
            this.output(b.array(), b.arrayOffset() + b.position(), b.remaining());
        } else {
            byte[] tmp = new byte[b.remaining()];
            b.get(tmp);
            this.output(tmp);
        }
    }

    public void input(ByteBuffer b) {
        if (b.hasArray()) {
            this.input(b.array(), b.arrayOffset() + b.position(), b.remaining());
        } else {
            byte[] tmp = new byte[b.remaining()];
            b.get(tmp);
            this.input(tmp);
        }
    }
}

