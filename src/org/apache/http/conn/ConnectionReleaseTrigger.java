/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.conn;

import java.io.IOException;

public interface ConnectionReleaseTrigger {
    public void releaseConnection() throws IOException;

    public void abortConnection() throws IOException;
}

