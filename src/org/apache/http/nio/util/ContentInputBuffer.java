/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.util;

import java.io.IOException;
import org.apache.http.nio.ContentDecoder;

public interface ContentInputBuffer {
    @Deprecated
    public int consumeContent(ContentDecoder var1) throws IOException;

    public void reset();

    public int read(byte[] var1, int var2, int var3) throws IOException;

    public int read() throws IOException;
}

