/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.auth;

import org.apache.http.auth.AuthScheme;
import org.apache.http.protocol.HttpContext;

public interface AuthSchemeProvider {
    public AuthScheme create(HttpContext var1);
}
