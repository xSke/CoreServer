/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.util.ArrayList;
import java.util.Collections;
import us.bpsm.edn.parser.CollectionBuilder;

final class DefaultVectorFactory
implements CollectionBuilder.Factory {
    DefaultVectorFactory() {
    }

    @Override
    public CollectionBuilder builder() {
        return new CollectionBuilder(){
            ArrayList<Object> list = new ArrayList();

            @Override
            public void add(Object o) {
                this.list.add(o);
            }

            @Override
            public Object build() {
                return Collections.unmodifiableList(this.list);
            }
        };
    }

}

