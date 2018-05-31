/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.net;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringDecoder;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.net.Utils;

public class URLCodec
implements BinaryEncoder,
BinaryDecoder,
StringEncoder,
StringDecoder {
    static final int RADIX = 16;
    protected String charset;
    protected static final byte ESCAPE_CHAR = 37;
    protected static final BitSet WWW_FORM_URL;

    public URLCodec() {
        this("UTF-8");
    }

    public URLCodec(String charset) {
        this.charset = charset;
    }

    public static final byte[] encodeUrl(BitSet urlsafe, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (urlsafe == null) {
            urlsafe = WWW_FORM_URL;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int c : bytes) {
            int b = c;
            if (b < 0) {
                b = 256 + b;
            }
            if (urlsafe.get(b)) {
                if (b == 32) {
                    b = 43;
                }
                buffer.write(b);
                continue;
            }
            buffer.write(37);
            char hex1 = Character.toUpperCase(Character.forDigit(b >> 4 & 15, 16));
            char hex2 = Character.toUpperCase(Character.forDigit(b & 15, 16));
            buffer.write(hex1);
            buffer.write(hex2);
        }
        return buffer.toByteArray();
    }

    public static final byte[] decodeUrl(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];
            if (b == 43) {
                buffer.write(32);
                continue;
            }
            if (b == 37) {
                try {
                    int u = Utils.digit16(bytes[++i]);
                    int l = Utils.digit16(bytes[++i]);
                    buffer.write((char)((u << 4) + l));
                    continue;
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid URL encoding: ", e);
                }
            }
            buffer.write(b);
        }
        return buffer.toByteArray();
    }

    public byte[] encode(byte[] bytes) {
        return URLCodec.encodeUrl(WWW_FORM_URL, bytes);
    }

    public byte[] decode(byte[] bytes) throws DecoderException {
        return URLCodec.decodeUrl(bytes);
    }

    public String encode(String pString, String charset) throws UnsupportedEncodingException {
        if (pString == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(this.encode(pString.getBytes(charset)));
    }

    public String encode(String pString) throws EncoderException {
        if (pString == null) {
            return null;
        }
        try {
            return this.encode(pString, this.getDefaultCharset());
        }
        catch (UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }

    public String decode(String pString, String charset) throws DecoderException, UnsupportedEncodingException {
        if (pString == null) {
            return null;
        }
        return new String(this.decode(StringUtils.getBytesUsAscii(pString)), charset);
    }

    public String decode(String pString) throws DecoderException {
        if (pString == null) {
            return null;
        }
        try {
            return this.decode(pString, this.getDefaultCharset());
        }
        catch (UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    public Object encode(Object pObject) throws EncoderException {
        if (pObject == null) {
            return null;
        }
        if (pObject instanceof byte[]) {
            return this.encode((byte[])pObject);
        }
        if (pObject instanceof String) {
            return this.encode((String)pObject);
        }
        throw new EncoderException("Objects of type " + pObject.getClass().getName() + " cannot be URL encoded");
    }

    public Object decode(Object pObject) throws DecoderException {
        if (pObject == null) {
            return null;
        }
        if (pObject instanceof byte[]) {
            return this.decode((byte[])pObject);
        }
        if (pObject instanceof String) {
            return this.decode((String)pObject);
        }
        throw new DecoderException("Objects of type " + pObject.getClass().getName() + " cannot be URL decoded");
    }

    public String getDefaultCharset() {
        return this.charset;
    }

    public String getEncoding() {
        return this.charset;
    }

    static {
        int i;
        WWW_FORM_URL = new BitSet(256);
        for (i = 97; i <= 122; ++i) {
            WWW_FORM_URL.set(i);
        }
        for (i = 65; i <= 90; ++i) {
            WWW_FORM_URL.set(i);
        }
        for (i = 48; i <= 57; ++i) {
            WWW_FORM_URL.set(i);
        }
        WWW_FORM_URL.set(45);
        WWW_FORM_URL.set(95);
        WWW_FORM_URL.set(46);
        WWW_FORM_URL.set(42);
        WWW_FORM_URL.set(32);
    }
}

