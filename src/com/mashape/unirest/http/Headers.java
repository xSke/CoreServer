/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http;

import java.util.HashMap;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Headers
extends HashMap<String, List<String>> {
    private static final long serialVersionUID = 71310341388734766L;

    public String getFirst(Object key) {
        List list = (List)this.get(key);
        if (list != null && list.size() > 0) {
            return (String)list.get(0);
        }
        return null;
    }
}

