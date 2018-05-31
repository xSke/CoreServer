/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.util;

import java.nio.ByteBuffer;
import org.apache.http.annotation.Immutable;
import org.apache.http.nio.util.ByteBufferAllocator;

@Immutable
public class DirectByteBufferAllocator
implements ByteBufferAllocator {
    public static final DirectByteBufferAllocator INSTANCE = new DirectByteBufferAllocator();

    public ByteBuffer allocate(int size) {
        return ByteBuffer.allocateDirect(size);
    }
}

