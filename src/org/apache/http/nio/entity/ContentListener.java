/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.IOException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;

@Deprecated
public interface ContentListener {
    public void contentAvailable(ContentDecoder var1, IOControl var2) throws IOException;

    public void finished();
}

