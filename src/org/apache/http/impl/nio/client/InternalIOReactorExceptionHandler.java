/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;

class InternalIOReactorExceptionHandler
implements IOReactorExceptionHandler {
    private final Log log;

    InternalIOReactorExceptionHandler(Log log) {
        this.log = log;
    }

    public boolean handle(IOException ex) {
        this.log.error("Fatal I/O error", ex);
        return false;
    }

    public boolean handle(RuntimeException ex) {
        this.log.error("Fatal runtime error", ex);
        return false;
    }
}

