/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http.cookie;

import io.netty.handler.codec.http.HttpHeaderDateFormat;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieEncoder;
import io.netty.handler.codec.http.cookie.CookieUtil;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public final class ServerCookieEncoder
extends CookieEncoder {
    public static final ServerCookieEncoder STRICT = new ServerCookieEncoder(true);
    public static final ServerCookieEncoder LAX = new ServerCookieEncoder(false);

    private ServerCookieEncoder(boolean strict) {
        super(strict);
    }

    public String encode(String name, String value) {
        return this.encode((Cookie)new DefaultCookie(name, value));
    }

    public String encode(Cookie cookie) {
        String name = ObjectUtil.checkNotNull(cookie, "cookie").name();
        String value = cookie.value() != null ? cookie.value() : "";
        this.validateCookie(name, value);
        StringBuilder buf = CookieUtil.stringBuilder();
        if (cookie.wrap()) {
            CookieUtil.addQuoted(buf, name, value);
        } else {
            CookieUtil.add(buf, name, value);
        }
        if (cookie.maxAge() != Long.MIN_VALUE) {
            CookieUtil.add(buf, "Max-Age", cookie.maxAge());
            Date expires = new Date(cookie.maxAge() * 1000L + System.currentTimeMillis());
            CookieUtil.add(buf, "Expires", HttpHeaderDateFormat.get().format(expires));
        }
        if (cookie.path() != null) {
            CookieUtil.add(buf, "Path", cookie.path());
        }
        if (cookie.domain() != null) {
            CookieUtil.add(buf, "Domain", cookie.domain());
        }
        if (cookie.isSecure()) {
            CookieUtil.add(buf, "Secure");
        }
        if (cookie.isHttpOnly()) {
            CookieUtil.add(buf, "HTTPOnly");
        }
        return CookieUtil.stripTrailingSeparator(buf);
    }

    public /* varargs */ List<String> encode(Cookie ... cookies) {
        if (ObjectUtil.checkNotNull(cookies, "cookies").length == 0) {
            return Collections.emptyList();
        }
        ArrayList<String> encoded = new ArrayList<String>(cookies.length);
        for (Cookie c : cookies) {
            if (c == null) break;
            encoded.add(this.encode(c));
        }
        return encoded;
    }

    public List<String> encode(Collection<? extends Cookie> cookies) {
        if (ObjectUtil.checkNotNull(cookies, "cookies").isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> encoded = new ArrayList<String>(cookies.size());
        for (Cookie c : cookies) {
            if (c == null) break;
            encoded.add(this.encode(c));
        }
        return encoded;
    }

    public List<String> encode(Iterable<? extends Cookie> cookies) {
        if (!ObjectUtil.checkNotNull(cookies, "cookies").iterator().hasNext()) {
            return Collections.emptyList();
        }
        ArrayList<String> encoded = new ArrayList<String>();
        for (Cookie c : cookies) {
            if (c == null) break;
            encoded.add(this.encode(c));
        }
        return encoded;
    }
}

