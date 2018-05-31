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

public interface SessionOutputBuffer {
    public boolean hasData();

    public int length();

    public int flush(WritableByteChannel var1) throws IOException;

    public void write(ByteBuffer var1);

    public void write(ReadableByteChannel var1) throws IOException;

    public void writeLine(CharArrayBuffer var1) throws CharacterCodingException;

    public void writeLine(String var1) throws IOException;
}

