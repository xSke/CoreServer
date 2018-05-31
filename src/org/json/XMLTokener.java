/*
 * Decompiled with CFR 0_129.
 */
package org.json;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONTokener;
import org.json.XML;

public class XMLTokener
extends JSONTokener {
    public static final HashMap entity = new HashMap(8);

    public XMLTokener(String s) {
        super(s);
    }

    public String nextCDATA() throws JSONException {
        int i;
        StringBuffer sb = new StringBuffer();
        do {
            char c = this.next();
            if (this.end()) {
                throw this.syntaxError("Unclosed CDATA");
            }
            sb.append(c);
        } while ((i = sb.length() - 3) < 0 || sb.charAt(i) != ']' || sb.charAt(i + 1) != ']' || sb.charAt(i + 2) != '>');
        sb.setLength(i);
        return sb.toString();
    }

    public Object nextContent() throws JSONException {
        char c;
        while (Character.isWhitespace(c = this.next())) {
        }
        if (c == '\u0000') {
            return null;
        }
        if (c == '<') {
            return XML.LT;
        }
        StringBuffer sb = new StringBuffer();
        do {
            if (c == '<' || c == '\u0000') {
                this.back();
                return sb.toString().trim();
            }
            if (c == '&') {
                sb.append(this.nextEntity(c));
            } else {
                sb.append(c);
            }
            c = this.next();
        } while (true);
    }

    public Object nextEntity(char ampersand) throws JSONException {
        char c;
        StringBuffer sb = new StringBuffer();
        while (Character.isLetterOrDigit(c = this.next()) || c == '#') {
            sb.append(Character.toLowerCase(c));
        }
        if (c != ';') {
            throw this.syntaxError("Missing ';' in XML entity: &" + sb);
        }
        String string = sb.toString();
        Object object = entity.get(string);
        return object != null ? object : "" + ampersand + string + ";";
    }

    public Object nextMeta() throws JSONException {
        char c;
        while (Character.isWhitespace(c = this.next())) {
        }
        switch (c) {
            case '\u0000': {
                throw this.syntaxError("Misshaped meta tag");
            }
            case '<': {
                return XML.LT;
            }
            case '>': {
                return XML.GT;
            }
            case '/': {
                return XML.SLASH;
            }
            case '=': {
                return XML.EQ;
            }
            case '!': {
                return XML.BANG;
            }
            case '?': {
                return XML.QUEST;
            }
            case '\"': 
            case '\'': {
                char q = c;
                do {
                    if ((c = this.next()) != '\u0000') continue;
                    throw this.syntaxError("Unterminated string");
                } while (c != q);
                return Boolean.TRUE;
            }
        }
        while (!Character.isWhitespace(c = this.next())) {
            switch (c) {
                case '\u0000': 
                case '!': 
                case '\"': 
                case '\'': 
                case '/': 
                case '<': 
                case '=': 
                case '>': 
                case '?': {
                    this.back();
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.TRUE;
    }

    public Object nextToken() throws JSONException {
        char c;
        while (Character.isWhitespace(c = this.next())) {
        }
        switch (c) {
            case '\u0000': {
                throw this.syntaxError("Misshaped element");
            }
            case '<': {
                throw this.syntaxError("Misplaced '<'");
            }
            case '>': {
                return XML.GT;
            }
            case '/': {
                return XML.SLASH;
            }
            case '=': {
                return XML.EQ;
            }
            case '!': {
                return XML.BANG;
            }
            case '?': {
                return XML.QUEST;
            }
            case '\"': 
            case '\'': {
                char q = c;
                StringBuffer sb = new StringBuffer();
                do {
                    if ((c = this.next()) == '\u0000') {
                        throw this.syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return sb.toString();
                    }
                    if (c == '&') {
                        sb.append(this.nextEntity(c));
                        continue;
                    }
                    sb.append(c);
                } while (true);
            }
        }
        StringBuffer sb = new StringBuffer();
        do {
            sb.append(c);
            c = this.next();
            if (Character.isWhitespace(c)) {
                return sb.toString();
            }
            switch (c) {
                case '\u0000': {
                    return sb.toString();
                }
                case '!': 
                case '/': 
                case '=': 
                case '>': 
                case '?': 
                case '[': 
                case ']': {
                    this.back();
                    return sb.toString();
                }
                case '\"': 
                case '\'': 
                case '<': {
                    throw this.syntaxError("Bad character in a name");
                }
            }
        } while (true);
    }

    public boolean skipPast(String to) throws JSONException {
        char c;
        int i;
        int offset = 0;
        int length = to.length();
        char[] circle = new char[length];
        for (i = 0; i < length; ++i) {
            c = this.next();
            if (c == '\u0000') {
                return false;
            }
            circle[i] = c;
        }
        do {
            int j = offset;
            boolean b = true;
            for (i = 0; i < length; ++i) {
                if (circle[j] != to.charAt(i)) {
                    b = false;
                    break;
                }
                if (++j < length) continue;
                j -= length;
            }
            if (b) {
                return true;
            }
            c = this.next();
            if (c == '\u0000') {
                return false;
            }
            circle[offset] = c;
            if (++offset < length) continue;
            offset -= length;
        } while (true);
    }

    static {
        entity.put("amp", XML.AMP);
        entity.put("apos", XML.APOS);
        entity.put("gt", XML.GT);
        entity.put("lt", XML.LT);
        entity.put("quot", XML.QUOT);
    }
}

