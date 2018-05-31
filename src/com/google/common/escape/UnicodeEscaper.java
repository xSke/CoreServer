/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.escape;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.escape.Escaper;
import com.google.common.escape.Platform;

@Beta
@GwtCompatible
public abstract class UnicodeEscaper
extends Escaper {
    private static final int DEST_PAD = 32;

    protected UnicodeEscaper() {
    }

    protected abstract char[] escape(int var1);

    protected int nextEscapeIndex(CharSequence csq, int start, int end) {
        int index;
        int cp;
        for (index = start; index < end && (cp = UnicodeEscaper.codePointAt(csq, index, end)) >= 0 && this.escape(cp) == null; index += Character.isSupplementaryCodePoint((int)cp) != false ? 2 : 1) {
        }
        return index;
    }

    @Override
    public String escape(String string) {
        Preconditions.checkNotNull(string);
        int end = string.length();
        int index = this.nextEscapeIndex(string, 0, end);
        return index == end ? string : this.escapeSlow(string, index);
    }

    protected final String escapeSlow(String s, int index) {
        int end = s.length();
        char[] dest = Platform.charBufferFromThreadLocal();
        int destIndex = 0;
        int unescapedChunkStart = 0;
        while (index < end) {
            int cp = UnicodeEscaper.codePointAt(s, index, end);
            if (cp < 0) {
                throw new IllegalArgumentException("Trailing high surrogate at end of input");
            }
            char[] escaped = this.escape(cp);
            int nextIndex = index + (Character.isSupplementaryCodePoint(cp) ? 2 : 1);
            if (escaped != null) {
                int charsSkipped = index - unescapedChunkStart;
                int sizeNeeded = destIndex + charsSkipped + escaped.length;
                if (dest.length < sizeNeeded) {
                    int destLength = sizeNeeded + (end - index) + 32;
                    dest = UnicodeEscaper.growBuffer(dest, destIndex, destLength);
                }
                if (charsSkipped > 0) {
                    s.getChars(unescapedChunkStart, index, dest, destIndex);
                    destIndex += charsSkipped;
                }
                if (escaped.length > 0) {
                    System.arraycopy(escaped, 0, dest, destIndex, escaped.length);
                    destIndex += escaped.length;
                }
                unescapedChunkStart = nextIndex;
            }
            index = this.nextEscapeIndex(s, nextIndex, end);
        }
        int charsSkipped = end - unescapedChunkStart;
        if (charsSkipped > 0) {
            int endIndex = destIndex + charsSkipped;
            if (dest.length < endIndex) {
                dest = UnicodeEscaper.growBuffer(dest, destIndex, endIndex);
            }
            s.getChars(unescapedChunkStart, end, dest, destIndex);
            destIndex = endIndex;
        }
        return new String(dest, 0, destIndex);
    }

    protected static int codePointAt(CharSequence seq, int index, int end) {
        Preconditions.checkNotNull(seq);
        if (index < end) {
            char c1;
            if ((c1 = seq.charAt(index++)) < '\ud800' || c1 > '\udfff') {
                return c1;
            }
            if (c1 <= '\udbff') {
                if (index == end) {
                    return - c1;
                }
                char c2 = seq.charAt(index);
                if (Character.isLowSurrogate(c2)) {
                    return Character.toCodePoint(c1, c2);
                }
                char c = c2;
                char c3 = c2;
                int n = index;
                String string = String.valueOf(String.valueOf(seq));
                throw new IllegalArgumentException(new StringBuilder(89 + string.length()).append("Expected low surrogate but got char '").append(c).append("' with value ").append((int)c3).append(" at index ").append(n).append(" in '").append(string).append("'").toString());
            }
            char c2 = c1;
            char c = c1;
            int n = index - 1;
            String string = String.valueOf(String.valueOf(seq));
            throw new IllegalArgumentException(new StringBuilder(88 + string.length()).append("Unexpected low surrogate character '").append(c2).append("' with value ").append((int)c).append(" at index ").append(n).append(" in '").append(string).append("'").toString());
        }
        throw new IndexOutOfBoundsException("Index exceeds specified range");
    }

    private static char[] growBuffer(char[] dest, int index, int size) {
        char[] copy = new char[size];
        if (index > 0) {
            System.arraycopy(dest, 0, copy, 0, index);
        }
        return copy;
    }
}

