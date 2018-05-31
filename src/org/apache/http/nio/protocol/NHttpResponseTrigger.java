/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

@Deprecated
public interface NHttpResponseTrigger {
    public void submitResponse(HttpResponse var1);

    public void handleException(HttpException var1);

    public void handleException(IOException var1);
}

