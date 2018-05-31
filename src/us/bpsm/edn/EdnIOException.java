/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

import java.io.IOException;
import us.bpsm.edn.EdnException;

public class EdnIOException
extends EdnException {
    private static final long serialVersionUID = 1L;

    public EdnIOException(String msg, IOException cause) {
        super(msg, cause);
    }

    public EdnIOException(IOException cause) {
        super(cause);
    }

    @Override
    public IOException getCause() {
        return (IOException)super.getCause();
    }
}

