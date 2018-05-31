/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.util;

import java.io.IOException;
import org.apache.http.nio.ContentEncoder;

public interface ContentOutputBuffer {
    @Deprecated
    public int produceContent(ContentEncoder var1) throws IOException;

    public void reset();

    @Deprecated
    public void flush() throws IOException;

    public void write(byte[] var1, int var2, int var3) throws IOException;

    public void write(int var1) throws IOException;

    public void writeCompleted() throws IOException;
}

