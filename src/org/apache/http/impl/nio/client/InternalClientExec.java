/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.nio.client.InternalConnManager;
import org.apache.http.impl.nio.client.InternalState;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;

interface InternalClientExec {
    public void prepare(InternalState var1, HttpHost var2, HttpRequest var3) throws IOException, HttpException;

    public HttpRequest generateRequest(InternalState var1, InternalConnManager var2) throws IOException, HttpException;

    public void produceContent(InternalState var1, ContentEncoder var2, IOControl var3) throws IOException;

    public void requestCompleted(InternalState var1);

    public void responseReceived(InternalState var1, HttpResponse var2) throws IOException, HttpException;

    public void consumeContent(InternalState var1, ContentDecoder var2, IOControl var3) throws IOException;

    public void responseCompleted(InternalState var1, InternalConnManager var2) throws IOException, HttpException;
}

