/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.util;

import java.util.BitSet;

public class CharClassify {
    private static final BitSet WHITESPACE = new BitSet(128);
    private static final BitSet DIGIT;
    private static final BitSet LETTER;
    private static final BitSet NUMBER_START;
    private static final BitSet SYMBOL_START;
    private static final BitSet SYMBOL_CONSTITUENTS;
    private static final BitSet TOKEN_SEPARATORS;

    private CharClassify() {
        throw new UnsupportedOperationException();
    }

    public static boolean symbolConstituent(char c) {
        return SYMBOL_CONSTITUENTS.get(c);
    }

    public static boolean symbolStart(char c) {
        return SYMBOL_START.get(c);
    }

    public static boolean isDigit(char c) {
        return DIGIT.get(c);
    }

    public static boolean isWhitespace(char c) {
        return WHITESPACE.get(c);
    }

    public static boolean separatesTokens(char c) {
        return TOKEN_SEPARATORS.get(c);
    }

    public static boolean startsNumber(char c) {
        return NUMBER_START.get(c);
    }

    static {
        WHITESPACE.set(0, 33);
        WHITESPACE.set(44);
        DIGIT = new BitSet(128);
        DIGIT.set(48, 58);
        LETTER = new BitSet(128);
        LETTER.set(65, 91);
        LETTER.set(97, 123);
        NUMBER_START = new BitSet(128);
        NUMBER_START.or(DIGIT);
        NUMBER_START.set(45);
        NUMBER_START.set(43);
        SYMBOL_START = new BitSet(128);
        SYMBOL_START.or(LETTER);
        for (char c : "!*+-./?_$%&=<>".toCharArray()) {
            SYMBOL_START.set(c);
        }
        SYMBOL_CONSTITUENTS = new BitSet(128);
        SYMBOL_CONSTITUENTS.or(SYMBOL_START);
        SYMBOL_CONSTITUENTS.or(DIGIT);
        SYMBOL_CONSTITUENTS.set(35);
        SYMBOL_CONSTITUENTS.set(58);
        TOKEN_SEPARATORS = new BitSet(128);
        TOKEN_SEPARATORS.or(WHITESPACE);
        for (char c : "\"#();[\\]{}".toCharArray()) {
            TOKEN_SEPARATORS.set(c);
        }
    }
}

