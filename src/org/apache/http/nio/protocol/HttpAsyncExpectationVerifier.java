/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.protocol.HttpContext;

public interface HttpAsyncExpectationVerifier {
    public void verify(HttpAsyncExchange var1, HttpContext var2) throws HttpException, IOException;
}

