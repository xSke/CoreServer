/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.client.util;

import java.io.IOException;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

public class HttpAsyncClientUtils {
    private HttpAsyncClientUtils() {
    }

    public static void closeQuietly(CloseableHttpAsyncClient httpAsyncClient) {
        if (httpAsyncClient != null) {
            try {
                httpAsyncClient.close();
            }
            catch (IOException ignore) {
                // empty catch block
            }
        }
    }
}

