/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import java.io.IOException;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorStatus;

public interface IOReactor {
    public IOReactorStatus getStatus();

    public void execute(IOEventDispatch var1) throws IOException;

    public void shutdown(long var1) throws IOException;

    public void shutdown() throws IOException;
}

