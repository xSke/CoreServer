/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.params;

import org.apache.http.nio.params.NIOReactorPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

@Deprecated
public final class NIOReactorParams
implements NIOReactorPNames {
    private NIOReactorParams() {
    }

    public static int getContentBufferSize(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getIntParameter("http.nio.content-buffer-size", 4096);
    }

    public static void setContentBufferSize(HttpParams params, int size) {
        Args.notNull(params, "HTTP parameters");
        params.setIntParameter("http.nio.content-buffer-size", size);
    }

    public static long getSelectInterval(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getLongParameter("http.nio.select-interval", 1000L);
    }

    public static void setSelectInterval(HttpParams params, long ms) {
        Args.notNull(params, "HTTP parameters");
        params.setLongParameter("http.nio.select-interval", ms);
    }

    public static long getGracePeriod(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getLongParameter("http.nio.grace-period", 500L);
    }

    public static void setGracePeriod(HttpParams params, long ms) {
        Args.notNull(params, "HTTP parameters");
        params.setLongParameter("http.nio.grace-period", ms);
    }

    public static boolean getInterestOpsQueueing(HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getBooleanParameter("http.nio.interest-ops-queueing", false);
    }

    public static void setInterestOpsQueueing(HttpParams params, boolean interestOpsQueueing) {
        Args.notNull(params, "HTTP parameters");
        params.setBooleanParameter("http.nio.interest-ops-queueing", interestOpsQueueing);
    }
}

