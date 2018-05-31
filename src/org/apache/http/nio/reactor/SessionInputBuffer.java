/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import org.apache.http.util.CharArrayBuffer;

public interface SessionInputBuffer {
    public boolean hasData();

    public int length();

    public int fill(ReadableByteChannel var1) throws IOException;

    public int read();

    public int read(ByteBuffer var1, int var2);

    public int read(ByteBuffer var1);

    public int read(WritableByteChannel var1, int var2) throws IOException;

    public int read(WritableByteChannel var1) throws IOException;

    public boolean readLine(CharArrayBuffer var1, boolean var2) throws CharacterCodingException;

    public String readLine(boolean var1) throws CharacterCodingException;
}

