/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslEngine;

interface OpenSslEngineMap {
    public static final OpenSslEngineMap EMPTY = new OpenSslEngineMap(){

        @Override
        public OpenSslEngine remove(long ssl) {
            return null;
        }

        @Override
        public void add(OpenSslEngine engine) {
        }
    };

    public OpenSslEngine remove(long var1);

    public void add(OpenSslEngine var1);

}

