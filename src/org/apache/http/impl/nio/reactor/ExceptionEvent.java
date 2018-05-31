/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.util.Date;
import org.apache.http.annotation.Immutable;

@Immutable
public class ExceptionEvent {
    private final Throwable ex;
    private final long time;

    public ExceptionEvent(Throwable ex, Date timestamp) {
        this.ex = ex;
        this.time = timestamp != null ? timestamp.getTime() : 0L;
    }

    public ExceptionEvent(Exception ex) {
        this(ex, new Date());
    }

    public Throwable getCause() {
        return this.ex;
    }

    public Date getTimestamp() {
        return new Date(this.time);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(new Date(this.time));
        buffer.append(" ");
        buffer.append(this.ex);
        return buffer.toString();
    }
}

