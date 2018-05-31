/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;

@GwtCompatible
public final class MoreObjects {
    public static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
        return first != null ? first : Preconditions.checkNotNull(second);
    }

    public static ToStringHelper toStringHelper(Object self) {
        return new ToStringHelper(MoreObjects.simpleName(self.getClass()));
    }

    public static ToStringHelper toStringHelper(Class<?> clazz) {
        return new ToStringHelper(MoreObjects.simpleName(clazz));
    }

    public static ToStringHelper toStringHelper(String className) {
        return new ToStringHelper(className);
    }

    static String simpleName(Class<?> clazz) {
        String name = clazz.getName();
        int start = (name = name.replaceAll("\\$[0-9]+", "\\$")).lastIndexOf(36);
        if (start == -1) {
            start = name.lastIndexOf(46);
        }
        return name.substring(start + 1);
    }

    private MoreObjects() {
    }

    public static final class ToStringHelper {
        private final String className;
        private ValueHolder holderHead;
        private ValueHolder holderTail;
        private boolean omitNullValues;

        private ToStringHelper(String className) {
            this.holderTail = this.holderHead = new ValueHolder();
            this.omitNullValues = false;
            this.className = Preconditions.checkNotNull(className);
        }

        public ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }

        public ToStringHelper add(String name, @Nullable Object value) {
            return this.addHolder(name, value);
        }

        public ToStringHelper add(String name, boolean value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, char value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, double value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, float value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, int value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, long value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper addValue(@Nullable Object value) {
            return this.addHolder(value);
        }

        public ToStringHelper addValue(boolean value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(char value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(double value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(float value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(int value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(long value) {
            return this.addHolder(String.valueOf(value));
        }

        public String toString() {
            boolean omitNullValuesSnapshot = this.omitNullValues;
            String nextSeparator = "";
            StringBuilder builder = new StringBuilder(32).append(this.className).append('{');
            ValueHolder valueHolder = this.holderHead.next;
            while (valueHolder != null) {
                if (!omitNullValuesSnapshot || valueHolder.value != null) {
                    builder.append(nextSeparator);
                    nextSeparator = ", ";
                    if (valueHolder.name != null) {
                        builder.append(valueHolder.name).append('=');
                    }
                    builder.append(valueHolder.value);
                }
                valueHolder = valueHolder.next;
            }
            return builder.append('}').toString();
        }

        private ValueHolder addHolder() {
            ValueHolder valueHolder;
            this.holderTail = this.holderTail.next = (valueHolder = new ValueHolder());
            return valueHolder;
        }

        private ToStringHelper addHolder(@Nullable Object value) {
            ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            return this;
        }

        private ToStringHelper addHolder(String name, @Nullable Object value) {
            ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            valueHolder.name = Preconditions.checkNotNull(name);
            return this;
        }

        private static final class ValueHolder {
            String name;
            Object value;
            ValueHolder next;

            private ValueHolder() {
            }
        }

    }

}

