/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;

@Deprecated
public interface ConsumingNHttpEntity
extends HttpEntity {
    public void consumeContent(ContentDecoder var1, IOControl var2) throws IOException;

    public void finish() throws IOException;
}

