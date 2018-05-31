/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MapUtil {
    public static List<NameValuePair> getList(Map<String, List<Object>> parameters) {
        ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();
        if (parameters != null) {
            for (Map.Entry<String, List<Object>> entry : parameters.entrySet()) {
                List<Object> entryValue = entry.getValue();
                if (entryValue == null) continue;
                for (Object cur : entryValue) {
                    if (cur == null) continue;
                    result.add(new BasicNameValuePair(entry.getKey(), cur.toString()));
                }
            }
        }
        return result;
    }
}

