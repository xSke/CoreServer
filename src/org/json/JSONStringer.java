/*
 * Decompiled with CFR 0_129.
 */
package org.json;

import java.io.StringWriter;
import java.io.Writer;
import org.json.JSONWriter;

public class JSONStringer
extends JSONWriter {
    public JSONStringer() {
        super(new StringWriter());
    }

    public String toString() {
        return this.mode == 'd' ? this.writer.toString() : null;
    }
}

