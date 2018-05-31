/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http.cookie;

public interface Cookie
extends Comparable<Cookie> {
    public String name();

    public String value();

    public void setValue(String var1);

    public boolean wrap();

    public void setWrap(boolean var1);

    public String domain();

    public void setDomain(String var1);

    public String path();

    public void setPath(String var1);

    public long maxAge();

    public void setMaxAge(long var1);

    public boolean isSecure();

    public void setSecure(boolean var1);

    public boolean isHttpOnly();

    public void setHttpOnly(boolean var1);
}

