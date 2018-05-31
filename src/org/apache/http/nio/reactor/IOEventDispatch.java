/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import org.apache.http.nio.reactor.IOSession;

public interface IOEventDispatch {
    public static final String CONNECTION_KEY = "http.connection";

    public void connected(IOSession var1);

    public void inputReady(IOSession var1);

    public void outputReady(IOSession var1);

    public void timeout(IOSession var1);

    public void disconnected(IOSession var1);
}

