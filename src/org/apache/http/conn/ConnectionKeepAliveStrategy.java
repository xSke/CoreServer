/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.conn;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public interface ConnectionKeepAliveStrategy {
    public long getKeepAliveDuration(HttpResponse var1, HttpContext var2);
}

