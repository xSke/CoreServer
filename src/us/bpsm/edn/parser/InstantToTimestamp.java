/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import us.bpsm.edn.parser.AbstractInstantHandler;
import us.bpsm.edn.parser.InstantUtils;
import us.bpsm.edn.parser.ParsedInstant;

public class InstantToTimestamp
extends AbstractInstantHandler {
    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeTimestamp(pi);
    }
}

