/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;

@GwtCompatible
final class CollectPreconditions {
    CollectPreconditions() {
    }

    static void checkEntryNotNull(Object key, Object value) {
        if (key == null) {
            String string = String.valueOf(String.valueOf(value));
            throw new NullPointerException(new StringBuilder(24 + string.length()).append("null key in entry: null=").append(string).toString());
        }
        if (value == null) {
            String string = String.valueOf(String.valueOf(key));
            throw new NullPointerException(new StringBuilder(26 + string.length()).append("null value in entry: ").append(string).append("=null").toString());
        }
    }

    static int checkNonnegative(int value, String name) {
        if (value < 0) {
            String string = String.valueOf(String.valueOf(name));
            int n = value;
            throw new IllegalArgumentException(new StringBuilder(40 + string.length()).append(string).append(" cannot be negative but was: ").append(n).toString());
        }
        return value;
    }

    static void checkRemove(boolean canRemove) {
        Preconditions.checkState(canRemove, "no calls to next() since the last call to remove()");
    }
}

