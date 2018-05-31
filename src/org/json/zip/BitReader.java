/*
 * Decompiled with CFR 0_129.
 */
package org.json.zip;

import java.io.IOException;

public interface BitReader {
    public boolean bit() throws IOException;

    public long nrBits();

    public boolean pad(int var1) throws IOException;

    public int read(int var1) throws IOException;
}

