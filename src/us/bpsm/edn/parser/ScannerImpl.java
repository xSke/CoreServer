/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import us.bpsm.edn.EdnIOException;
import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import us.bpsm.edn.Tag;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Scanner;
import us.bpsm.edn.parser.TagHandler;
import us.bpsm.edn.parser.Token;
import us.bpsm.edn.util.CharClassify;

class ScannerImpl
implements Scanner {
    static final Symbol NIL_SYMBOL = Symbol.newSymbol("nil");
    static final Symbol TRUE_SYMBOL = Symbol.newSymbol("true");
    static final Symbol FALSE_SYMBOL = Symbol.newSymbol("false");
    static final Symbol SLASH_SYMBOL = Symbol.newSymbol("/");
    static final int END = -1;
    private final TagHandler longHandler;
    private final TagHandler bigDecimalHandler;
    private final TagHandler bigIntegerHandler;
    private final TagHandler doubleHandler;
    private static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    ScannerImpl(Parser.Config cfg) {
        if (cfg == null) {
            throw new IllegalArgumentException("cfg must not be null");
        }
        this.longHandler = cfg.getTagHandler(Parser.Config.LONG_TAG);
        this.bigIntegerHandler = cfg.getTagHandler(Parser.Config.BIG_INTEGER_TAG);
        this.doubleHandler = cfg.getTagHandler(Parser.Config.DOUBLE_TAG);
        this.bigDecimalHandler = cfg.getTagHandler(Parser.Config.BIG_DECIMAL_TAG);
    }

    @Override
    public Object nextToken(Parseable pbr) {
        try {
            return this.scanNextToken(pbr);
        }
        catch (IOException e) {
            throw new EdnIOException(e);
        }
    }

    private Object scanNextToken(Parseable pbr) throws IOException {
        this.skipWhitespaceAndComments(pbr);
        int curr = pbr.read();
        switch (curr) {
            case -1: {
                return Token.END_OF_INPUT;
            }
            case 97: 
            case 98: 
            case 99: 
            case 100: 
            case 101: {
                return this.readSymbol(curr, pbr);
            }
            case 102: {
                return this.readSymbolOrFalse(curr, pbr);
            }
            case 103: 
            case 104: 
            case 105: 
            case 106: 
            case 107: 
            case 108: 
            case 109: {
                return this.readSymbol(curr, pbr);
            }
            case 110: {
                return this.readSymbolOrNil(curr, pbr);
            }
            case 111: 
            case 112: 
            case 113: 
            case 114: 
            case 115: {
                return this.readSymbol(curr, pbr);
            }
            case 116: {
                return this.readSymbolOrTrue(curr, pbr);
            }
            case 33: 
            case 36: 
            case 37: 
            case 38: 
            case 42: 
            case 47: 
            case 60: 
            case 61: 
            case 62: 
            case 63: 
            case 65: 
            case 66: 
            case 67: 
            case 68: 
            case 69: 
            case 70: 
            case 71: 
            case 72: 
            case 73: 
            case 74: 
            case 75: 
            case 76: 
            case 77: 
            case 78: 
            case 79: 
            case 80: 
            case 81: 
            case 82: 
            case 83: 
            case 84: 
            case 85: 
            case 86: 
            case 87: 
            case 88: 
            case 89: 
            case 90: 
            case 95: 
            case 117: 
            case 118: 
            case 119: 
            case 120: 
            case 121: 
            case 122: {
                return this.readSymbol(curr, pbr);
            }
            case 46: {
                return this.readSymbol(curr, pbr);
            }
            case 43: 
            case 45: {
                return this.readSymbolOrNumber(curr, pbr);
            }
            case 58: {
                return this.readKeyword(pbr);
            }
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: 
            case 56: 
            case 57: {
                return this.readNumber(curr, pbr);
            }
            case 123: {
                return Token.BEGIN_MAP;
            }
            case 125: {
                return Token.END_MAP_OR_SET;
            }
            case 91: {
                return Token.BEGIN_VECTOR;
            }
            case 93: {
                return Token.END_VECTOR;
            }
            case 40: {
                return Token.BEGIN_LIST;
            }
            case 41: {
                return Token.END_LIST;
            }
            case 35: {
                return this.readHashDispatched(pbr);
            }
            case 34: {
                return this.readStringLiteral(pbr);
            }
            case 92: {
                return Character.valueOf(this.readCharacterLiteral(pbr));
            }
        }
        throw new EdnSyntaxException(String.format("Unexpected character '%c', \\u%04x", Character.valueOf((char)curr), curr));
    }

    private Object readHashDispatched(Parseable pbr) throws IOException {
        int peek = pbr.read();
        switch (peek) {
            case -1: {
                throw new EdnSyntaxException("Unexpected end of input following '#'");
            }
            case 123: {
                return Token.BEGIN_SET;
            }
            case 95: {
                return Token.DISCARD;
            }
        }
        return Tag.newTag(this.readSymbol(peek, pbr));
    }

    private Object readSymbolOrNumber(int curr, Parseable pbr) throws IOException {
        int peek = pbr.read();
        if (peek == -1) {
            return this.readSymbol(curr, pbr);
        }
        ScannerImpl.unread(pbr, peek);
        if (CharClassify.isDigit((char)peek)) {
            return this.readNumber(curr, pbr);
        }
        return this.readSymbol(curr, pbr);
    }

    private static Parseable unread(Parseable pbr, int ch) throws IOException {
        pbr.unread(ch);
        return pbr;
    }

    private Object readSymbolOrTrue(int curr, Parseable pbr) throws IOException {
        Symbol sym = this.readSymbol(curr, pbr);
        return TRUE_SYMBOL.equals(sym) ? Boolean.valueOf(true) : sym;
    }

    private Object readSymbolOrNil(int curr, Parseable pbr) throws IOException {
        Symbol sym = this.readSymbol(curr, pbr);
        return NIL_SYMBOL.equals(sym) ? Token.NIL : sym;
    }

    private Object readSymbolOrFalse(int curr, Parseable pbr) throws IOException {
        Symbol sym = this.readSymbol(curr, pbr);
        return FALSE_SYMBOL.equals(sym) ? Boolean.valueOf(false) : sym;
    }

    private void skipWhitespaceAndComments(Parseable pbr) throws IOException {
        int curr;
        do {
            this.skipWhitespace(pbr);
            curr = pbr.read();
            if (curr != 59) break;
            this.skipComment(pbr);
        } while (true);
        ScannerImpl.unread(pbr, curr);
    }

    private void skipWhitespace(Parseable pbr) throws IOException {
        int curr;
        while ((curr = pbr.read()) != -1 && CharClassify.isWhitespace((char)curr)) {
        }
        ScannerImpl.unread(pbr, curr);
    }

    private void skipComment(Parseable pbr) throws IOException {
        int curr;
        while ((curr = pbr.read()) != -1 && curr != 10 && curr != 13) {
        }
        ScannerImpl.unread(pbr, curr);
    }

    private char readCharacterLiteral(Parseable pbr) throws IOException {
        int curr = pbr.read();
        if (curr == -1) {
            throw new EdnSyntaxException("Unexpected end of input following ''");
        }
        if (CharClassify.isWhitespace((char)curr) && curr != 44) {
            throw new EdnSyntaxException("A backslash introducing character literal must not be immediately followed by whitespace.");
        }
        StringBuilder b = new StringBuilder();
        do {
            b.append((char)curr);
        } while ((curr = pbr.read()) != -1 && !CharClassify.separatesTokens((char)curr));
        ScannerImpl.unread(pbr, curr);
        if (b.length() == 1) {
            return b.charAt(0);
        }
        return ScannerImpl.charForName(b.toString());
    }

    private static char charForName(String name) {
        switch (name.charAt(0)) {
            case 'n': {
                if ("newline".equals(name)) {
                    return '\n';
                }
            }
            case 's': {
                if ("space".equals(name)) {
                    return ' ';
                }
            }
            case 't': {
                if ("tab".equals(name)) {
                    return '\t';
                }
            }
            case 'b': {
                if ("backspace".equals(name)) {
                    return '\b';
                }
            }
            case 'f': {
                if ("formfeed".equals(name)) {
                    return '\f';
                }
            }
            case 'r': {
                if (!"return".equals(name)) break;
                return '\r';
            }
        }
        throw new EdnSyntaxException("The character \\" + name + " was not recognized.");
    }

    private String readStringLiteral(Parseable pbr) throws IOException {
        StringBuffer b = new StringBuffer();
        block16 : do {
            int curr = pbr.read();
            switch (curr) {
                case -1: {
                    throw new EdnSyntaxException("Unexpected end of input in string literal");
                }
                case 34: {
                    return b.toString();
                }
                case 92: {
                    curr = pbr.read();
                    switch (curr) {
                        case -1: {
                            throw new EdnSyntaxException("Unexpected end of input in string literal");
                        }
                        case 98: {
                            b.append('\b');
                            continue block16;
                        }
                        case 116: {
                            b.append('\t');
                            continue block16;
                        }
                        case 110: {
                            b.append('\n');
                            continue block16;
                        }
                        case 102: {
                            b.append('\f');
                            continue block16;
                        }
                        case 114: {
                            b.append('\r');
                            continue block16;
                        }
                        case 34: {
                            b.append('\"');
                            continue block16;
                        }
                        case 39: {
                            b.append('\'');
                            continue block16;
                        }
                        case 92: {
                            b.append('\\');
                            continue block16;
                        }
                    }
                    throw new EdnSyntaxException("Unsupported '" + (char)curr + "' escape in string");
                }
            }
            b.append((char)curr);
        } while (true);
    }

    private Object readNumber(int curr, Parseable pbr) throws IOException {
        boolean bigint;
        assert (curr != -1 && CharClassify.startsNumber((char)curr));
        StringBuffer digits = new StringBuffer();
        if (curr != 43) {
            digits.append((char)curr);
        }
        curr = pbr.read();
        while (curr != -1 && CharClassify.isDigit((char)curr)) {
            digits.append((char)curr);
            curr = pbr.read();
        }
        if (curr == 46 || curr == 101 || curr == 69 || curr == 77) {
            boolean decimal;
            if (curr == 46) {
                do {
                    digits.append((char)curr);
                } while ((curr = pbr.read()) != -1 && CharClassify.isDigit((char)curr));
            }
            if (curr == 101 || curr == 69) {
                digits.append((char)curr);
                curr = pbr.read();
                if (curr == -1) {
                    throw new EdnSyntaxException("Unexpected end of input in numeric literal");
                }
                if (curr != 45 && curr != 43 && !CharClassify.isDigit((char)curr)) {
                    throw new EdnSyntaxException("Not a number: '" + digits + (char)curr + "'.");
                }
                do {
                    digits.append((char)curr);
                } while ((curr = pbr.read()) != -1 && CharClassify.isDigit((char)curr));
            }
            boolean bl = decimal = curr == 77;
            if (decimal) {
                curr = pbr.read();
            }
            if (curr != -1 && !CharClassify.separatesTokens((char)curr)) {
                throw new EdnSyntaxException("Not a number: '" + digits + (char)curr + "'.");
            }
            ScannerImpl.unread(pbr, curr);
            if (decimal) {
                BigDecimal d = new BigDecimal(digits.toString());
                return this.bigDecimalHandler.transform(Parser.Config.BIG_DECIMAL_TAG, d);
            }
            double d = Double.parseDouble(digits.toString());
            return this.doubleHandler.transform(Parser.Config.DOUBLE_TAG, d);
        }
        boolean bl = bigint = curr == 78;
        if (bigint) {
            curr = pbr.read();
        }
        if (curr != -1 && !CharClassify.separatesTokens((char)curr)) {
            throw new EdnSyntaxException("Not a number: '" + digits + (char)curr + "'.");
        }
        ScannerImpl.unread(pbr, curr);
        BigInteger n = new BigInteger(digits.toString());
        if (bigint || MIN_LONG.compareTo(n) > 0 || n.compareTo(MAX_LONG) > 0) {
            return this.bigIntegerHandler.transform(Parser.Config.BIG_INTEGER_TAG, n);
        }
        return this.longHandler.transform(Parser.Config.LONG_TAG, n.longValue());
    }

    private Keyword readKeyword(Parseable pbr) throws IOException {
        Symbol sym = this.readSymbol(pbr);
        if (SLASH_SYMBOL.equals(sym)) {
            throw new EdnSyntaxException("':/' is not a valid keyword.");
        }
        return Keyword.newKeyword(sym);
    }

    private Symbol readSymbol(Parseable pbr) throws IOException {
        return this.readSymbol(pbr.read(), pbr);
    }

    private Symbol readSymbol(int curr, Parseable pbr) throws IOException {
        if (curr == -1) {
            throw new EdnSyntaxException("Unexpected end of input while reading an identifier");
        }
        StringBuilder b = new StringBuilder();
        int n = 0;
        int p = Integer.MIN_VALUE;
        do {
            if (curr == 47) {
                ++n;
                p = b.length();
            }
            b.append((char)curr);
        } while ((curr = pbr.read()) != -1 && !CharClassify.separatesTokens((char)curr));
        ScannerImpl.unread(pbr, curr);
        this.validateUseOfSlash(b, n, p);
        return this.makeSymbol(b, n, p);
    }

    private Symbol makeSymbol(StringBuilder b, int slashCount, int slashPos) {
        if (slashCount == 0) {
            return Symbol.newSymbol(b.toString());
        }
        if (slashCount == 1) {
            if (slashPos == 0) {
                assert (b.length() == 1 && b.charAt(0) == '/');
                return Symbol.newSymbol(b.toString());
            }
            return Symbol.newSymbol(b.substring(0, slashPos), b.substring(slashPos + 1));
        }
        assert (slashCount == 2 && slashPos == b.length() - 1 && b.charAt(b.length() - 2) == '/');
        return Symbol.newSymbol(b.substring(0, slashPos - 1), "/");
    }

    private void validateUseOfSlash(CharSequence s, int slashCount, int lastSlashPos) {
        assert (s.length() > 0);
        if (slashCount == 1) {
            if (s.length() != 1) {
                if (lastSlashPos == s.length() - 1) {
                    throw new EdnSyntaxException("The name '" + s + "' must not end with '/'.");
                }
                if (lastSlashPos == 0) {
                    throw new EdnSyntaxException("The name '" + s + "' must not start with '/'.");
                }
            }
        } else if (slashCount == 2) {
            if (s.length() == 2) {
                throw new EdnSyntaxException("The name '//' is not valid.");
            }
            if (lastSlashPos != s.length() - 1 || s.charAt(lastSlashPos - 1) != '/') {
                throw new EdnSyntaxException("Incorrect use of '/' in name.");
            }
        } else if (slashCount > 3) {
            throw new EdnSyntaxException("Too many '/' in name.");
        }
    }
}

