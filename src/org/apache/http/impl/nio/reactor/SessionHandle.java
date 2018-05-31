/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import org.apache.http.nio.reactor.IOSession;
import org.apache.http.util.Args;

@Deprecated
public class SessionHandle {
    private final IOSession session;
    private final long startedTime;
    private long lastReadTime;
    private long lastWriteTime;
    private long lastAccessTime;

    public SessionHandle(IOSession session) {
        long now;
        Args.notNull(session, "Session");
        this.session = session;
        this.startedTime = now = System.currentTimeMillis();
        this.lastReadTime = now;
        this.lastWriteTime = now;
        this.lastAccessTime = now;
    }

    public IOSession getSession() {
        return this.session;
    }

    public long getStartedTime() {
        return this.startedTime;
    }

    public long getLastReadTime() {
        return this.lastReadTime;
    }

    public long getLastWriteTime() {
        return this.lastWriteTime;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public void resetLastRead() {
        long now;
        this.lastReadTime = now = System.currentTimeMillis();
        this.lastAccessTime = now;
    }

    public void resetLastWrite() {
        long now;
        this.lastWriteTime = now = System.currentTimeMillis();
        this.lastAccessTime = now;
    }
}

