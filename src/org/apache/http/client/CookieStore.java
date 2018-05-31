/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.client;

import java.util.Date;
import java.util.List;
import org.apache.http.cookie.Cookie;

public interface CookieStore {
    public void addCookie(Cookie var1);

    public List<Cookie> getCookies();

    public boolean clearExpired(Date var1);

    public void clear();
}

