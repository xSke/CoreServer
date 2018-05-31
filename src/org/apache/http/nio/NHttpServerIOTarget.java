/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServiceHandler;

@Deprecated
public interface NHttpServerIOTarget
extends NHttpServerConnection {
    public void consumeInput(NHttpServiceHandler var1);

    public void produceOutput(NHttpServiceHandler var1);
}

