/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http;

import java.util.Iterator;

public interface TokenIterator
extends Iterator<Object> {
    @Override
    public boolean hasNext();

    public String nextToken();
}

