/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;

public final class StringUtil {
    public static final String NEWLINE;
    public static final char DOUBLE_QUOTE = '\"';
    public static final char COMMA = ',';
    public static final char LINE_FEED = '\n';
    public static final char CARRIAGE_RETURN = '\r';
    public static final String EMPTY_STRING = "";
    private static final String[] BYTE2HEX_PAD;
    private static final String[] BYTE2HEX_NOPAD;
    private static final char PACKAGE_SEPARATOR_CHAR = '.';

    public static String[] split(String value, char delim) {
        int i;
        int end = value.length();
        ArrayList<String> res = new ArrayList<String>();
        int start = 0;
        for (i = 0; i < end; ++i) {
            if (value.charAt(i) != delim) continue;
            if (start == i) {
                res.add(EMPTY_STRING);
            } else {
                res.add(value.substring(start, i));
            }
            start = i + 1;
        }
        if (start == 0) {
            res.add(value);
        } else if (start != end) {
            res.add(value.substring(start, end));
        } else {
            for (i = res.size() - 1; i >= 0 && ((String)res.get(i)).isEmpty(); --i) {
                res.remove(i);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    public static String[] split(String value, char delim, int maxParts) {
        int i;
        int end = value.length();
        ArrayList<String> res = new ArrayList<String>();
        int start = 0;
        int cpt = 1;
        for (i = 0; i < end && cpt < maxParts; ++i) {
            if (value.charAt(i) != delim) continue;
            if (start == i) {
                res.add(EMPTY_STRING);
            } else {
                res.add(value.substring(start, i));
            }
            start = i + 1;
            ++cpt;
        }
        if (start == 0) {
            res.add(value);
        } else if (start != end) {
            res.add(value.substring(start, end));
        } else {
            for (i = res.size() - 1; i >= 0 && ((String)res.get(i)).isEmpty(); --i) {
                res.remove(i);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    public static String substringAfter(String value, char delim) {
        int pos = value.indexOf(delim);
        if (pos >= 0) {
            return value.substring(pos + 1);
        }
        return null;
    }

    public static String byteToHexStringPadded(int value) {
        return BYTE2HEX_PAD[value & 255];
    }

    public static <T extends Appendable> T byteToHexStringPadded(T buf, int value) {
        try {
            buf.append(StringUtil.byteToHexStringPadded(value));
        }
        catch (IOException e) {
            PlatformDependent.throwException(e);
        }
        return buf;
    }

    public static String toHexStringPadded(byte[] src) {
        return StringUtil.toHexStringPadded(src, 0, src.length);
    }

    public static String toHexStringPadded(byte[] src, int offset, int length) {
        return StringUtil.toHexStringPadded(new StringBuilder(length << 1), src, offset, length).toString();
    }

    public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src) {
        return StringUtil.toHexStringPadded(dst, src, 0, src.length);
    }

    public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src, int offset, int length) {
        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            StringUtil.byteToHexStringPadded(dst, src[i]);
        }
        return dst;
    }

    public static String byteToHexString(int value) {
        return BYTE2HEX_NOPAD[value & 255];
    }

    public static <T extends Appendable> T byteToHexString(T buf, int value) {
        try {
            buf.append(StringUtil.byteToHexString(value));
        }
        catch (IOException e) {
            PlatformDependent.throwException(e);
        }
        return buf;
    }

    public static String toHexString(byte[] src) {
        return StringUtil.toHexString(src, 0, src.length);
    }

    public static String toHexString(byte[] src, int offset, int length) {
        return StringUtil.toHexString(new StringBuilder(length << 1), src, offset, length).toString();
    }

    public static <T extends Appendable> T toHexString(T dst, byte[] src) {
        return StringUtil.toHexString(dst, src, 0, src.length);
    }

    public static <T extends Appendable> T toHexString(T dst, byte[] src, int offset, int length) {
        int i;
        assert (length >= 0);
        if (length == 0) {
            return dst;
        }
        int end = offset + length;
        int endMinusOne = end - 1;
        for (i = offset; i < endMinusOne && src[i] == 0; ++i) {
        }
        StringUtil.byteToHexString(dst, src[i++]);
        int remaining = end - i;
        StringUtil.toHexStringPadded(dst, src, i, remaining);
        return dst;
    }

    public static String simpleClassName(Object o) {
        if (o == null) {
            return "null_object";
        }
        return StringUtil.simpleClassName(o.getClass());
    }

    public static String simpleClassName(Class<?> clazz) {
        String className = ObjectUtil.checkNotNull(clazz, "clazz").getName();
        int lastDotIdx = className.lastIndexOf(46);
        if (lastDotIdx > -1) {
            return className.substring(lastDotIdx + 1);
        }
        return className;
    }

    private StringUtil() {
    }

    static {
        int i;
        StringBuilder buf;
        String newLine;
        BYTE2HEX_PAD = new String[256];
        BYTE2HEX_NOPAD = new String[256];
        try {
            newLine = new Formatter().format("%n", new Object[0]).toString();
        }
        catch (Exception e) {
            newLine = "\n";
        }
        NEWLINE = newLine;
        for (i = 0; i < 10; ++i) {
            buf = new StringBuilder(2);
            buf.append('0');
            buf.append(i);
            StringUtil.BYTE2HEX_PAD[i] = buf.toString();
            StringUtil.BYTE2HEX_NOPAD[i] = String.valueOf(i);
        }
        while (i < 16) {
            buf = new StringBuilder(2);
            char c = (char)(97 + i - 10);
            buf.append('0');
            buf.append(c);
            StringUtil.BYTE2HEX_PAD[i] = buf.toString();
            StringUtil.BYTE2HEX_NOPAD[i] = String.valueOf(c);
            ++i;
        }
        while (i < BYTE2HEX_PAD.length) {
            String str;
            buf = new StringBuilder(2);
            buf.append(Integer.toHexString(i));
            StringUtil.BYTE2HEX_PAD[i] = str = buf.toString();
            StringUtil.BYTE2HEX_NOPAD[i] = str;
            ++i;
        }
    }
}

