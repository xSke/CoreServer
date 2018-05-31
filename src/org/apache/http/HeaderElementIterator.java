/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http;

import java.util.Iterator;
import org.apache.http.HeaderElement;

public interface HeaderElementIterator
extends Iterator<Object> {
    @Override
    public boolean hasNext();

    public HeaderElement nextElement();
}

