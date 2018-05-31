/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.util;

import mikera.vectorz.Vector3;

public class StringUtil {
    public static Vector3 stringToVector(String input) {
        if (input.split(",").length != 3) {
            throw new RuntimeException("Input string doesn't have 3 comma-seperated segments");
        }
        return new Vector3(Double.parseDouble(input.split(",")[0]), Double.parseDouble(input.split(",")[1]), Double.parseDouble(input.split(",")[2]));
    }
}

