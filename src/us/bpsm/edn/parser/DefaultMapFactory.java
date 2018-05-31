/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import us.bpsm.edn.parser.CollectionBuilder;

final class DefaultMapFactory
implements CollectionBuilder.Factory {
    DefaultMapFactory() {
    }

    @Override
    public CollectionBuilder builder() {
        return new CollectionBuilder(){
            final Object none = new Object();
            final Map<Object, Object> map = new HashMap<Object, Object>();
            Object key = this.none;

            @Override
            public void add(Object o) {
                if (this.key == this.none) {
                    this.key = o;
                } else {
                    this.map.put(this.key, o);
                    this.key = this.none;
                }
            }

            @Override
            public Object build() {
                if (this.key != this.none) {
                    throw new IllegalStateException("Every map must have an equal number of keys and values.");
                }
                return Collections.unmodifiableMap(this.map);
            }
        };
    }

}

