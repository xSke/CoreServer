/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.params;

import org.apache.http.nio.params.NIOReactorParams;
import org.apache.http.params.HttpAbstractParamBean;
import org.apache.http.params.HttpParams;

@Deprecated
public class NIOReactorParamBean
extends HttpAbstractParamBean {
    public NIOReactorParamBean(HttpParams params) {
        super(params);
    }

    public void setContentBufferSize(int contentBufferSize) {
        NIOReactorParams.setContentBufferSize(this.params, contentBufferSize);
    }

    public void setSelectInterval(long selectInterval) {
        NIOReactorParams.setSelectInterval(this.params, selectInterval);
    }
}

