/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.conn.ssl;

import java.net.Socket;
import java.util.Map;
import org.apache.http.conn.ssl.PrivateKeyDetails;

public interface PrivateKeyStrategy {
    public String chooseAlias(Map<String, PrivateKeyDetails> var1, Socket var2);
}

