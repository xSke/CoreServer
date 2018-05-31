/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http.utils;

public class URLParamEncoder {
    public static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (URLParamEncoder.isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(URLParamEncoder.toHex(ch / 16));
                resultStr.append(URLParamEncoder.toHex(ch % 16));
                continue;
            }
            resultStr.append(ch);
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char)(ch < 10 ? 48 + ch : 65 + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > '' || ch < '\u0000') {
            return true;
        }
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
}

