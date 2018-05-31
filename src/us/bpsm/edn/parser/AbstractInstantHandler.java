/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;
import us.bpsm.edn.parser.InstantUtils;
import us.bpsm.edn.parser.ParsedInstant;
import us.bpsm.edn.parser.TagHandler;

public abstract class AbstractInstantHandler
implements TagHandler {
    @Override
    public final Object transform(Tag tag, Object value) {
        if (!(value instanceof String)) {
            throw new EdnSyntaxException(tag.toString() + " expects a String.");
        }
        return this.transform(InstantUtils.parse((String)value));
    }

    protected abstract Object transform(ParsedInstant var1);
}

