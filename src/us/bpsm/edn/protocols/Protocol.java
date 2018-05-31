/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.protocols;

public interface Protocol<F> {
    public String name();

    public F lookup(Class var1);

    public static interface Builder<F> {
        public Builder<F> put(Class var1, F var2);

        public Protocol<F> build();
    }

}

