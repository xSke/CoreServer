/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.util.ArrayList;
import java.util.List;
import us.bpsm.edn.parser.CollectionBuilder;
import us.bpsm.edn.parser.DelegatingList;

final class DefaultListFactory
implements CollectionBuilder.Factory {
    DefaultListFactory() {
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
                return new DelegatingList<Object>(this.list);
            }
        };
    }

}

