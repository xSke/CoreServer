/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.util.UUID;
import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;
import us.bpsm.edn.parser.TagHandler;

class UuidHandler
implements TagHandler {
    UuidHandler() {
    }

    @Override
    public Object transform(Tag tag, Object value) {
        if (!(value instanceof String)) {
            throw new EdnSyntaxException(tag.toString() + " expectes a String.");
        }
        return UUID.fromString((String)value);
    }
}

