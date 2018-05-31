/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

import us.bpsm.edn.Interner;
import us.bpsm.edn.Named;
import us.bpsm.edn.Symbol;

public final class Keyword
implements Named,
Comparable<Keyword> {
    private final Symbol sym;
    private static final Interner<Symbol, Keyword> INTERNER = new Interner();

    @Override
    public final String getPrefix() {
        return this.sym.getPrefix();
    }

    @Override
    public final String getName() {
        return this.sym.getName();
    }

    public static Keyword newKeyword(Symbol sym) {
        return INTERNER.intern(sym, new Keyword(sym));
    }

    public static Keyword newKeyword(String prefix, String name) {
        return Keyword.newKeyword(Symbol.newSymbol(prefix, name));
    }

    public static Keyword newKeyword(String name) {
        return Keyword.newKeyword(Symbol.newSymbol(EMPTY, name));
    }

    private Keyword(Symbol sym) {
        if (sym == null) {
            throw new NullPointerException();
        }
        this.sym = sym;
    }

    public String toString() {
        return ":" + this.sym.toString();
    }

    @Override
    public int compareTo(Keyword o) {
        if (this == o) {
            return 0;
        }
        return this.sym.compareTo(o.sym);
    }
}

