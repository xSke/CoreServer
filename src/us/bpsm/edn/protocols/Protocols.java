/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.protocols;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import us.bpsm.edn.protocols.C3;
import us.bpsm.edn.protocols.Protocol;

public class Protocols {
    static final String SINGLE_USE_MSG = "This builder can only be used to build a single Protocol.";
    static final String NO_MODIFY_MSG = "This builder is single-use and may not be modified after the Protocol has been built.";
    static final String MUST_HAVE_NAME = "Each Protocol must have a name";
    static final String FN_MUST_NOT_BE_NULL = "The value ('fn') associated with a class must not be null.";

    private Protocols() {
        throw new UnsupportedOperationException();
    }

    public static <F> Protocol.Builder<F> builder(String name) {
        return new ProtocolImpl(name);
    }

    static <E> List<E> butfirst(List<E> es) {
        return es.isEmpty() ? es : es.subList(1, es.size());
    }

    static class ProtocolImpl<F>
    implements Protocol.Builder<F>,
    Protocol<F> {
        final String name;
        F nullFn = null;
        final Map<Class<?>, F> m = new HashMap();
        boolean built = false;

        public String toString() {
            if (this.built) {
                return "Protocol '" + this.name + "'";
            }
            return "Protocol.Builder '" + this.name + "'";
        }

        ProtocolImpl(String name) {
            if (name == null) {
                throw new NullPointerException(Protocols.MUST_HAVE_NAME);
            }
            this.name = name;
        }

        @Override
        public Protocol.Builder<F> put(Class selfClass, F fn) {
            if (this.built) {
                throw new IllegalStateException(Protocols.NO_MODIFY_MSG);
            }
            if (fn == null) {
                throw new NullPointerException(Protocols.FN_MUST_NOT_BE_NULL);
            }
            if (selfClass == null) {
                this.nullFn = fn;
            } else {
                this.m.put(selfClass, fn);
            }
            return this;
        }

        @Override
        public Protocol<F> build() {
            if (this.built) {
                throw new IllegalStateException(Protocols.SINGLE_USE_MSG);
            }
            this.built = true;
            return this;
        }

        @Override
        public String name() {
            return this.name;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public F lookup(Class selfClass) {
            if (selfClass == null) {
                return this.nullFn;
            }
            Map map = this.m;
            synchronized (map) {
                F fn = this.m.get(selfClass);
                if (fn != null) {
                    return fn;
                }
                for (Class c : Protocols.butfirst(C3.methodResolutionOrder(selfClass))) {
                    fn = this.m.get(c);
                    if (fn == null) continue;
                    this.m.put(selfClass, fn);
                    return fn;
                }
                return null;
            }
        }
    }

}

