/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import us.bpsm.edn.parser.CollectionBuilder;

final class DefaultSetFactory
implements CollectionBuilder.Factory {
    DefaultSetFactory() {
    }

    @Override
    public CollectionBuilder builder() {
        return new CollectionBuilder(){
            Set<Object> set = new HashSet<Object>();

            @Override
            public void add(Object o) {
                this.set.add(o);
            }

            @Override
            public Object build() {
                return Collections.unmodifiableSet(this.set);
            }
        };
    }

}

