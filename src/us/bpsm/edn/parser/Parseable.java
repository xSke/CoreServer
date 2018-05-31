/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.io.Closeable;
import java.io.IOException;

public interface Parseable
extends Closeable {
    public static final int END_OF_INPUT = -1;

    public int read() throws IOException;

    public void unread(int var1) throws IOException;
}

