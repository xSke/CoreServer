/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import org.apache.http.nio.reactor.SessionRequest;

public interface SessionRequestCallback {
    public void completed(SessionRequest var1);

    public void failed(SessionRequest var1);

    public void timeout(SessionRequest var1);

    public void cancelled(SessionRequest var1);
}

