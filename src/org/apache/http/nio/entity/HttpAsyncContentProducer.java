/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.Closeable;
import java.io.IOException;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;

public interface HttpAsyncContentProducer
extends Closeable {
    public void produceContent(ContentEncoder var1, IOControl var2) throws IOException;

    public boolean isRepeatable();
}

