/*
 * Decompiled with CFR 0_129.
 */
package com.google.thirdparty.publicsuffix;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
enum PublicSuffixType {
    PRIVATE(':', ','),
    ICANN('!', '?');
    
    private final char innerNodeCode;
    private final char leafNodeCode;

    private PublicSuffixType(char innerNodeCode, char leafNodeCode) {
        this.innerNodeCode = innerNodeCode;
        this.leafNodeCode = leafNodeCode;
    }

    char getLeafNodeCode() {
        return this.leafNodeCode;
    }

    char getInnerNodeCode() {
        return this.innerNodeCode;
    }

    static PublicSuffixType fromCode(char code) {
        for (PublicSuffixType value : PublicSuffixType.values()) {
            if (value.getInnerNodeCode() != code && value.getLeafNodeCode() != code) continue;
            return value;
        }
        char arr$ = code;
        throw new IllegalArgumentException(new StringBuilder(38).append("No enum corresponding to given code: ").append(arr$).toString());
    }

    static PublicSuffixType fromIsPrivate(boolean isPrivate) {
        return isPrivate ? PRIVATE : ICANN;
    }
}

