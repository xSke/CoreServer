/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.hash;

import com.google.common.hash.Hasher;
import com.google.common.hash.PrimitiveSink;
import java.nio.charset.Charset;

abstract class AbstractHasher
implements Hasher {
    AbstractHasher() {
    }

    @Override
    public final Hasher putBoolean(boolean b) {
        return this.putByte(b ? 1 : 0);
    }

    @Override
    public final Hasher putDouble(double d) {
        return this.putLong(Double.doubleToRawLongBits(d));
    }

    @Override
    public final Hasher putFloat(float f) {
        return this.putInt(Float.floatToRawIntBits(f));
    }

    @Override
    public Hasher putUnencodedChars(CharSequence charSequence) {
        int len = charSequence.length();
        for (int i = 0; i < len; ++i) {
            this.putChar(charSequence.charAt(i));
        }
        return this;
    }

    @Override
    public Hasher putString(CharSequence charSequence, Charset charset) {
        return this.putBytes(charSequence.toString().getBytes(charset));
    }
}

