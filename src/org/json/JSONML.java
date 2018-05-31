/*
 * Decompiled with CFR 0_129.
 */
package org.json;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLTokener;

public class JSONML {
    private static Object parse(XMLTokener x, boolean arrayForm, JSONArray ja) throws JSONException {
        String closeTag = null;
        JSONArray newja = null;
        JSONObject newjo = null;
        String tagName = null;
        do {
            if (!x.more()) {
                throw x.syntaxError("Bad XML");
            }
            Object token = x.nextContent();
            if (token == XML.LT) {
                token = x.nextToken();
                if (token instanceof Character) {
                    if (token == XML.SLASH) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw new JSONException("Expected a closing name instead of '" + token + "'.");
                        }
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped close tag");
                        }
                        return token;
                    }
                    if (token == XML.BANG) {
                        char c = x.next();
                        if (c == '-') {
                            if (x.next() == '-') {
                                x.skipPast("-->");
                                continue;
                            }
                            x.back();
                            continue;
                        }
                        if (c == '[') {
                            token = x.nextToken();
                            if (token.equals("CDATA") && x.next() == '[') {
                                if (ja == null) continue;
                                ja.put(x.nextCDATA());
                                continue;
                            }
                            throw x.syntaxError("Expected 'CDATA['");
                        }
                        int i = 1;
                        do {
                            if ((token = x.nextMeta()) == null) {
                                throw x.syntaxError("Missing '>' after '<!'.");
                            }
                            if (token == XML.LT) {
                                ++i;
                                continue;
                            }
                            if (token != XML.GT) continue;
                            --i;
                        } while (i > 0);
                        continue;
                    }
                    if (token == XML.QUEST) {
                        x.skipPast("?>");
                        continue;
                    }
                    throw x.syntaxError("Misshaped tag");
                }
                if (!(token instanceof String)) {
                    throw x.syntaxError("Bad tagName '" + token + "'.");
                }
                tagName = (String)token;
                newja = new JSONArray();
                newjo = new JSONObject();
                if (arrayForm) {
                    newja.put(tagName);
                    if (ja != null) {
                        ja.put(newja);
                    }
                } else {
                    newjo.put("tagName", tagName);
                    if (ja != null) {
                        ja.put(newjo);
                    }
                }
                token = null;
                do {
                    if (token == null) {
                        token = x.nextToken();
                    }
                    if (token == null) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (!(token instanceof String)) break;
                    String attribute = (String)token;
                    if (!arrayForm && ("tagName".equals(attribute) || "childNode".equals(attribute))) {
                        throw x.syntaxError("Reserved attribute.");
                    }
                    token = x.nextToken();
                    if (token == XML.EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }
                        newjo.accumulate(attribute, XML.stringToValue((String)token));
                        token = null;
                        continue;
                    }
                    newjo.accumulate(attribute, "");
                } while (true);
                if (arrayForm && newjo.length() > 0) {
                    newja.put(newjo);
                }
                if (token == XML.SLASH) {
                    if (x.nextToken() != XML.GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (ja != null) continue;
                    if (arrayForm) {
                        return newja;
                    }
                    return newjo;
                }
                if (token != XML.GT) {
                    throw x.syntaxError("Misshaped tag");
                }
                closeTag = (String)JSONML.parse(x, arrayForm, newja);
                if (closeTag == null) continue;
                if (!closeTag.equals(tagName)) {
                    throw x.syntaxError("Mismatched '" + tagName + "' and '" + closeTag + "'");
                }
                tagName = null;
                if (!arrayForm && newja.length() > 0) {
                    newjo.put("childNodes", newja);
                }
                if (ja != null) continue;
                if (arrayForm) {
                    return newja;
                }
                return newjo;
            }
            if (ja == null) continue;
            ja.put(token instanceof String ? XML.stringToValue((String)token) : token);
        } while (true);
    }

    public static JSONArray toJSONArray(String string) throws JSONException {
        return JSONML.toJSONArray(new XMLTokener(string));
    }

    public static JSONArray toJSONArray(XMLTokener x) throws JSONException {
        return (JSONArray)JSONML.parse(x, true, null);
    }

    public static JSONObject toJSONObject(XMLTokener x) throws JSONException {
        return (JSONObject)JSONML.parse(x, false, null);
    }

    public static JSONObject toJSONObject(String string) throws JSONException {
        return JSONML.toJSONObject(new XMLTokener(string));
    }

    public static String toString(JSONArray ja) throws JSONException {
        int i;
        int length;
        StringBuffer sb = new StringBuffer();
        String tagName = ja.getString(0);
        XML.noSpace(tagName);
        tagName = XML.escape(tagName);
        sb.append('<');
        sb.append(tagName);
        Object object = ja.opt(1);
        if (object instanceof JSONObject) {
            i = 2;
            JSONObject jo = (JSONObject)object;
            Iterator keys = jo.keys();
            while (keys.hasNext()) {
                String key = keys.next().toString();
                XML.noSpace(key);
                String value = jo.optString(key);
                if (value == null) continue;
                sb.append(' ');
                sb.append(XML.escape(key));
                sb.append('=');
                sb.append('\"');
                sb.append(XML.escape(value));
                sb.append('\"');
            }
        } else {
            i = 1;
        }
        if (i >= (length = ja.length())) {
            sb.append('/');
            sb.append('>');
        } else {
            sb.append('>');
            do {
                object = ja.get(i);
                ++i;
                if (object == null) continue;
                if (object instanceof String) {
                    sb.append(XML.escape(object.toString()));
                    continue;
                }
                if (object instanceof JSONObject) {
                    sb.append(JSONML.toString((JSONObject)object));
                    continue;
                }
                if (!(object instanceof JSONArray)) continue;
                sb.append(JSONML.toString((JSONArray)object));
            } while (i < length);
            sb.append('<');
            sb.append('/');
            sb.append(tagName);
            sb.append('>');
        }
        return sb.toString();
    }

    public static String toString(JSONObject jo) throws JSONException {
        StringBuffer sb = new StringBuffer();
        String tagName = jo.optString("tagName");
        if (tagName == null) {
            return XML.escape(jo.toString());
        }
        XML.noSpace(tagName);
        tagName = XML.escape(tagName);
        sb.append('<');
        sb.append(tagName);
        Iterator keys = jo.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            if ("tagName".equals(key) || "childNodes".equals(key)) continue;
            XML.noSpace(key);
            String value = jo.optString(key);
            if (value == null) continue;
            sb.append(' ');
            sb.append(XML.escape(key));
            sb.append('=');
            sb.append('\"');
            sb.append(XML.escape(value));
            sb.append('\"');
        }
        JSONArray ja = jo.optJSONArray("childNodes");
        if (ja == null) {
            sb.append('/');
            sb.append('>');
        } else {
            sb.append('>');
            int length = ja.length();
            for (int i = 0; i < length; ++i) {
                Object object = ja.get(i);
                if (object == null) continue;
                if (object instanceof String) {
                    sb.append(XML.escape(object.toString()));
                    continue;
                }
                if (object instanceof JSONObject) {
                    sb.append(JSONML.toString((JSONObject)object));
                    continue;
                }
                if (object instanceof JSONArray) {
                    sb.append(JSONML.toString((JSONArray)object));
                    continue;
                }
                sb.append(object.toString());
            }
            sb.append('<');
            sb.append('/');
            sb.append(tagName);
            sb.append('>');
        }
        return sb.toString();
    }
}

