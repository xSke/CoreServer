/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http;

import org.apache.http.MalformedChunkCodingException;

public class TruncatedChunkException
extends MalformedChunkCodingException {
    private static final long serialVersionUID = -23506263930279460L;

    public TruncatedChunkException(String message) {
        super(message);
    }
}

