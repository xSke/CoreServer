/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http;

import org.apache.http.ProtocolVersion;

public interface RequestLine {
    public String getMethod();

    public ProtocolVersion getProtocolVersion();

    public String getUri();
}

