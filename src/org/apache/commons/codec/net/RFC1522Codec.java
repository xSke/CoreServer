/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.StringUtils;

abstract class RFC1522Codec {
    protected static final char SEP = '?';
    protected static final String POSTFIX = "?=";
    protected static final String PREFIX = "=?";

    RFC1522Codec() {
    }

    protected String encodeText(String text, String charset) throws EncoderException, UnsupportedEncodingException {
        if (text == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(PREFIX);
        buffer.append(charset);
        buffer.append('?');
        buffer.append(this.getEncoding());
        buffer.append('?');
        byte[] rawdata = this.doEncoding(text.getBytes(charset));
        buffer.append(StringUtils.newStringUsAscii(rawdata));
        buffer.append(POSTFIX);
        return buffer.toString();
    }

    protected String decodeText(String text) throws DecoderException, UnsupportedEncodingException {
        if (text == null) {
            return null;
        }
        if (!text.startsWith(PREFIX) || !text.endsWith(POSTFIX)) {
            throw new DecoderException("RFC 1522 violation: malformed encoded content");
        }
        int terminator = text.length() - 2;
        int from = 2;
        int to = text.indexOf(63, from);
        if (to == terminator) {
            throw new DecoderException("RFC 1522 violation: charset token not found");
        }
        String charset = text.substring(from, to);
        if (charset.equals("")) {
            throw new DecoderException("RFC 1522 violation: charset not specified");
        }
        from = to + 1;
        to = text.indexOf(63, from);
        if (to == terminator) {
            throw new DecoderException("RFC 1522 violation: encoding token not found");
        }
        String encoding = text.substring(from, to);
        if (!this.getEncoding().equalsIgnoreCase(encoding)) {
            throw new DecoderException("This codec cannot decode " + encoding + " encoded content");
        }
        from = to + 1;
        to = text.indexOf(63, from);
        byte[] data = StringUtils.getBytesUsAscii(text.substring(from, to));
        data = this.doDecoding(data);
        return new String(data, charset);
    }

    protected abstract String getEncoding();

    protected abstract byte[] doEncoding(byte[] var1) throws EncoderException;

    protected abstract byte[] doDecoding(byte[] var1) throws DecoderException;
}

