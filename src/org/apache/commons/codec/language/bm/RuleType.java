/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.language.bm;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum RuleType {
    APPROX("approx"),
    EXACT("exact"),
    RULES("rules");
    
    private final String name;

    private RuleType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

