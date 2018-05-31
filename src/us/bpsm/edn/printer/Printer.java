/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.printer;

public interface Printer {
    public Printer printValue(Object var1);

    public Printer append(CharSequence var1);

    public Printer append(char var1);

    public Printer softspace();

    public void close();

    public static interface Fn<E> {
        public void eval(E var1, Printer var2);
    }

}

