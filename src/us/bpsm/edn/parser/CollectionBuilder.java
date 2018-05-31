/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

public interface CollectionBuilder {
    public void add(Object var1);

    public Object build();

    public static interface Factory {
        public CollectionBuilder builder();
    }

}

