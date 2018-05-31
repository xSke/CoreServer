/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

public enum Token {
    END_OF_INPUT,
    BEGIN_LIST,
    END_LIST,
    BEGIN_VECTOR,
    END_VECTOR,
    BEGIN_SET,
    BEGIN_MAP,
    END_MAP_OR_SET,
    NIL,
    DISCARD;
    

    private Token() {
    }
}

