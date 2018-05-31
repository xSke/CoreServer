/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

import us.bpsm.edn.EdnException;

public class EdnSyntaxException
extends EdnException {
    private static final long serialVersionUID = 1L;

    public EdnSyntaxException() {
    }

    public EdnSyntaxException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EdnSyntaxException(String msg) {
        super(msg);
    }

    public EdnSyntaxException(Throwable cause) {
        super(cause);
    }
}

