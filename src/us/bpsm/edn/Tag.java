/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

import us.bpsm.edn.Named;
import us.bpsm.edn.Symbol;

public final class Tag
implements Named,
Comparable<Tag> {
    private final Symbol sym;

    @Override
    public final String getPrefix() {
        return this.sym.getPrefix();
    }

    @Override
    public final String getName() {
        return this.sym.getName();
    }

    public static Tag newTag(Symbol sym) {
        return new Tag(sym);
    }

    public static Tag newTag(String prefix, String name) {
        return Tag.newTag(Symbol.newSymbol(prefix, name));
    }

    public static Tag newTag(String name) {
        return Tag.newTag(Symbol.newSymbol(EMPTY, name));
    }

    private Tag(Symbol sym) {
        if (sym == null) {
            throw new NullPointerException();
        }
        this.sym = sym;
    }

    public String toString() {
        return "#" + this.sym.toString();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.sym == null ? 0 : this.sym.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Tag other = (Tag)obj;
        if (this.sym == null ? other.sym != null : !this.sym.equals(other.sym)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Tag o) {
        return this.sym.compareTo(o.sym);
    }
}

