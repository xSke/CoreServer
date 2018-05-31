/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.client.methods;

import org.apache.http.concurrent.Cancellable;

public interface HttpExecutionAware {
    public boolean isAborted();

    public void setCancellable(Cancellable var1);
}

