/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.ErrorResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncResponseProducer;
import org.apache.http.nio.protocol.NullRequestConsumer;
import org.apache.http.protocol.HttpContext;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class NullRequestHandler
implements HttpAsyncRequestHandler<Object> {
    @Override
    public HttpAsyncRequestConsumer<Object> processRequest(HttpRequest request, HttpContext context) {
        return new NullRequestConsumer();
    }

    @Override
    public void handle(Object obj, HttpAsyncExchange httpexchange, HttpContext context) {
        HttpResponse response = httpexchange.getResponse();
        response.setStatusCode(501);
        httpexchange.submitResponse(new ErrorResponseProducer(response, new NStringEntity("Service not implemented", ContentType.TEXT_PLAIN), true));
    }
}

