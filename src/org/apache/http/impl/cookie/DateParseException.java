/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.cookie;

import org.apache.http.annotation.Immutable;

@Deprecated
@Immutable
public class DateParseException
extends Exception {
    private static final long serialVersionUID = 4417696455000643370L;

    public DateParseException() {
    }

    public DateParseException(String message) {
        super(message);
    }
}

