/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.cookie;

import org.apache.http.cookie.CookieSpec;
import org.apache.http.protocol.HttpContext;

public interface CookieSpecProvider {
    public CookieSpec create(HttpContext var1);
}

