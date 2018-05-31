/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.http.nio.ContentDecoder;

public interface FileContentDecoder
extends ContentDecoder {
    public long transfer(FileChannel var1, long var2, long var4) throws IOException;
}

