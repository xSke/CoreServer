/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.client;

import org.apache.http.HttpResponse;

public interface ConnectionBackoffStrategy {
    public boolean shouldBackoff(Throwable var1);

    public boolean shouldBackoff(HttpResponse var1);
}

