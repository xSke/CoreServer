/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.conn;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface DnsResolver {
    public InetAddress[] resolve(String var1) throws UnknownHostException;
}

