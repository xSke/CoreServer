/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.marker;

import java.util.HashMap;
import java.util.Map;

public class Marker {
    public static int nextId = 0;
    private int id;
    private Map<String, String> properties;

    public Marker(int id) {
        this.id = id;
        this.properties = new HashMap<String, String>();
    }

    public int getId() {
        return this.id;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }
}

