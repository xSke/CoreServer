/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http.cookie;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieEncoder;
import io.netty.handler.codec.http.cookie.CookieUtil;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.internal.ObjectUtil;
import java.util.Iterator;

public final class ClientCookieEncoder
extends CookieEncoder {
    public static final ClientCookieEncoder STRICT = new ClientCookieEncoder(true);
    public static final ClientCookieEncoder LAX = new ClientCookieEncoder(false);

    private ClientCookieEncoder(boolean strict) {
        super(strict);
    }

    public String encode(String name, String value) {
        return this.encode((Cookie)new DefaultCookie(name, value));
    }

    public String encode(Cookie cookie) {
        StringBuilder buf = CookieUtil.stringBuilder();
        this.encode(buf, ObjectUtil.checkNotNull(cookie, "cookie"));
        return CookieUtil.stripTrailingSeparator(buf);
    }

    public /* varargs */ String encode(Cookie ... cookies) {
        if (ObjectUtil.checkNotNull(cookies, "cookies").length == 0) {
            return null;
        }
        StringBuilder buf = CookieUtil.stringBuilder();
        for (Cookie c : cookies) {
            if (c == null) break;
            this.encode(buf, c);
        }
        return CookieUtil.stripTrailingSeparatorOrNull(buf);
    }

    public String encode(Iterable<? extends Cookie> cookies) {
        Cookie c;
        Iterator<? extends Cookie> cookiesIt = ObjectUtil.checkNotNull(cookies, "cookies").iterator();
        if (!cookiesIt.hasNext()) {
            return null;
        }
        StringBuilder buf = CookieUtil.stringBuilder();
        while (cookiesIt.hasNext() && (c = cookiesIt.next()) != null) {
            this.encode(buf, c);
        }
        return CookieUtil.stripTrailingSeparatorOrNull(buf);
    }

    private void encode(StringBuilder buf, Cookie c) {
        String name = c.name();
        String value = c.value() != null ? c.value() : "";
        this.validateCookie(name, value);
        if (c.wrap()) {
            CookieUtil.addQuoted(buf, name, value);
        } else {
            CookieUtil.add(buf, name, value);
        }
    }
}

