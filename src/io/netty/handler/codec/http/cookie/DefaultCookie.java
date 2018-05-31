/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http.cookie;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieUtil;
import io.netty.util.internal.ObjectUtil;

public class DefaultCookie
implements Cookie {
    private final String name;
    private String value;
    private boolean wrap;
    private String domain;
    private String path;
    private long maxAge = Long.MIN_VALUE;
    private boolean secure;
    private boolean httpOnly;

    public DefaultCookie(String name, String value) {
        name = ObjectUtil.checkNotNull(name, "name").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c > '') {
                throw new IllegalArgumentException("name contains non-ascii character: " + name);
            }
            switch (c) {
                case '\t': 
                case '\n': 
                case '\u000b': 
                case '\f': 
                case '\r': 
                case ' ': 
                case ',': 
                case ';': 
                case '=': {
                    throw new IllegalArgumentException("name contains one of the following prohibited characters: =,; \\t\\r\\n\\v\\f: " + name);
                }
            }
        }
        if (name.charAt(0) == '$') {
            throw new IllegalArgumentException("name starting with '$' not allowed: " + name);
        }
        this.name = name;
        this.setValue(value);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = ObjectUtil.checkNotNull(value, "value");
    }

    @Override
    public boolean wrap() {
        return this.wrap;
    }

    @Override
    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    @Override
    public String domain() {
        return this.domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = this.validateValue("domain", domain);
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public void setPath(String path) {
        this.path = this.validateValue("path", path);
    }

    @Override
    public long maxAge() {
        return this.maxAge;
    }

    @Override
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isHttpOnly() {
        return this.httpOnly;
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public int hashCode() {
        return this.name().hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cookie)) {
            return false;
        }
        Cookie that = (Cookie)o;
        if (!this.name().equalsIgnoreCase(that.name())) {
            return false;
        }
        if (this.path() == null) {
            if (that.path() != null) {
                return false;
            }
        } else {
            if (that.path() == null) {
                return false;
            }
            if (!this.path().equals(that.path())) {
                return false;
            }
        }
        if (this.domain() == null) {
            if (that.domain() != null) {
                return false;
            }
        } else {
            if (that.domain() == null) {
                return false;
            }
            return this.domain().equalsIgnoreCase(that.domain());
        }
        return true;
    }

    @Override
    public int compareTo(Cookie c) {
        int v = this.name().compareToIgnoreCase(c.name());
        if (v != 0) {
            return v;
        }
        if (this.path() == null) {
            if (c.path() != null) {
                return -1;
            }
        } else {
            if (c.path() == null) {
                return 1;
            }
            v = this.path().compareTo(c.path());
            if (v != 0) {
                return v;
            }
        }
        if (this.domain() == null) {
            if (c.domain() != null) {
                return -1;
            }
        } else {
            if (c.domain() == null) {
                return 1;
            }
            v = this.domain().compareToIgnoreCase(c.domain());
            return v;
        }
        return 0;
    }

    public String toString() {
        StringBuilder buf = CookieUtil.stringBuilder().append(this.name()).append('=').append(this.value());
        if (this.domain() != null) {
            buf.append(", domain=").append(this.domain());
        }
        if (this.path() != null) {
            buf.append(", path=").append(this.path());
        }
        if (this.maxAge() >= 0L) {
            buf.append(", maxAge=").append(this.maxAge()).append('s');
        }
        if (this.isSecure()) {
            buf.append(", secure");
        }
        if (this.isHttpOnly()) {
            buf.append(", HTTPOnly");
        }
        return buf.toString();
    }

    protected String validateValue(String name, String value) {
        if (value == null) {
            return null;
        }
        if ((value = value.trim()).isEmpty()) {
            return null;
        }
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '\n': 
                case '\u000b': 
                case '\f': 
                case '\r': 
                case ';': {
                    throw new IllegalArgumentException(name + " contains one of the following prohibited characters: " + ";\\r\\n\\f\\v (" + value + ')');
                }
            }
        }
        return value;
    }
}

