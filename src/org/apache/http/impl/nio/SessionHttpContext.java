/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import org.apache.http.nio.reactor.IOSession;
import org.apache.http.protocol.HttpContext;

class SessionHttpContext
implements HttpContext {
    private final IOSession iosession;

    public SessionHttpContext(IOSession iosession) {
        this.iosession = iosession;
    }

    public Object getAttribute(String id) {
        return this.iosession.getAttribute(id);
    }

    public Object removeAttribute(String id) {
        return this.iosession.removeAttribute(id);
    }

    public void setAttribute(String id, Object obj) {
        this.iosession.setAttribute(id, obj);
    }
}

