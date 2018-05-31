/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

import us.bpsm.edn.EdnException;
import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Named;
import us.bpsm.edn.util.CharClassify;

public final class Symbol
implements Named,
Comparable<Symbol> {
    private final String prefix;
    private final String name;

    @Override
    public final String getPrefix() {
        return this.prefix;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    private Symbol(String prefix, String name) {
        this.prefix = prefix.length() > 0 ? prefix : EMPTY;
        this.name = name;
    }

    public static Symbol newSymbol(String prefix, String name) {
        Symbol.checkArguments(prefix, name);
        return new Symbol(prefix, name);
    }

    public static Symbol newSymbol(String name) {
        return Symbol.newSymbol(EMPTY, name);
    }

    public final int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.getClass().getName().hashCode();
        result = 31 * result + this.name.hashCode();
        result = 31 * result + (this.prefix == null ? 0 : this.prefix.hashCode());
        return result;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Symbol other = (Symbol)obj;
        if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.prefix == null ? other.prefix != null : !this.prefix.equals(other.prefix)) {
            return false;
        }
        return true;
    }

    public String toString() {
        if (this.prefix.length() == 0) {
            return this.name;
        }
        return this.prefix + "/" + this.name;
    }

    private static void checkArguments(String prefix, String name) {
        if (prefix == null) {
            throw new EdnException("prefix must not be null.");
        }
        if (name == null) {
            throw new EdnException("name must not be null.");
        }
        Symbol.checkName("name", name);
        if (prefix.length() != 0) {
            Symbol.checkName("prefix", prefix);
        }
    }

    private static void checkName(String label, String ident) {
        if (ident.length() == 0) {
            throw new EdnSyntaxException("The " + label + " must not be empty.");
        }
        char first = ident.charAt(0);
        if (CharClassify.isDigit(first)) {
            throw new EdnSyntaxException("The " + label + " '" + ident + "' must not begin with a digit.");
        }
        if (!CharClassify.symbolStart(first)) {
            throw new EdnSyntaxException("The " + label + " '" + ident + "' begins with a forbidden character.");
        }
        if ((first == '.' || first == '-') && ident.length() > 1 && CharClassify.isDigit(ident.charAt(1))) {
            throw new EdnSyntaxException("The " + label + " '" + ident + "' begins with a '-' or '.' followed by digit, " + "which is forbidden.");
        }
        int n = ident.length();
        for (int i = 1; i < n; ++i) {
            if (CharClassify.symbolConstituent(ident.charAt(i))) continue;
            throw new EdnSyntaxException("The " + label + " '" + ident + "' contains the illegal character '" + ident.charAt(i) + "' at offset " + i + ".");
        }
    }

    @Override
    public int compareTo(Symbol right) {
        int cmp = this.prefix.compareTo(right.prefix);
        return cmp != 0 ? cmp : this.name.compareTo(right.name);
    }
}

