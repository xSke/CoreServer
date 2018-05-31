/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import org.apache.http.annotation.Immutable;
import org.apache.http.impl.nio.reactor.SessionRequestImpl;
import org.apache.http.util.Args;

@Immutable
public class SessionRequestHandle {
    private final SessionRequestImpl sessionRequest;
    private final long requestTime;

    public SessionRequestHandle(SessionRequestImpl sessionRequest) {
        Args.notNull(sessionRequest, "Session request");
        this.sessionRequest = sessionRequest;
        this.requestTime = System.currentTimeMillis();
    }

    public SessionRequestImpl getSessionRequest() {
        return this.sessionRequest;
    }

    public long getRequestTime() {
        return this.requestTime;
    }
}

