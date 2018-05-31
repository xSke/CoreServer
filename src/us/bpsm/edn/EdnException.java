/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

public class EdnException
extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EdnException() {
    }

    public EdnException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EdnException(String msg) {
        super(msg);
    }

    public EdnException(Throwable cause) {
        super(cause);
    }
}

