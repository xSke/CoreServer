/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ContentEncoder {
    public int write(ByteBuffer var1) throws IOException;

    public void complete() throws IOException;

    public boolean isCompleted();
}

