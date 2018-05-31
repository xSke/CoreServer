/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.ComposedLastHttpContent;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpMessage;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.util.List;

public abstract class HttpContentDecoder
extends MessageToMessageDecoder<HttpObject> {
    private EmbeddedChannel decoder;
    private boolean continueResponse;

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        if (msg instanceof HttpResponse && ((HttpResponse)msg).getStatus().code() == 100) {
            if (!(msg instanceof LastHttpContent)) {
                this.continueResponse = true;
            }
            out.add(ReferenceCountUtil.retain(msg));
            return;
        }
        if (this.continueResponse) {
            if (msg instanceof LastHttpContent) {
                this.continueResponse = false;
            }
            out.add(ReferenceCountUtil.retain(msg));
            return;
        }
        if (msg instanceof HttpMessage) {
            this.cleanup();
            HttpMessage message = (HttpMessage)msg;
            HttpHeaders headers = message.headers();
            String contentEncoding = headers.get("Content-Encoding");
            contentEncoding = contentEncoding != null ? contentEncoding.trim() : "identity";
            this.decoder = this.newContentDecoder(contentEncoding);
            if (this.decoder == null) {
                if (message instanceof HttpContent) {
                    ((HttpContent)((Object)message)).retain();
                }
                out.add(message);
                return;
            }
            headers.remove("Content-Length");
            String targetContentEncoding = this.getTargetContentEncoding(contentEncoding);
            if ("identity".equals(targetContentEncoding)) {
                headers.remove("Content-Encoding");
            } else {
                headers.set("Content-Encoding", (Object)targetContentEncoding);
            }
            if (message instanceof HttpContent) {
                DefaultHttpMessage copy;
                if (message instanceof HttpRequest) {
                    HttpRequest r = (HttpRequest)message;
                    copy = new DefaultHttpRequest(r.getProtocolVersion(), r.getMethod(), r.getUri());
                } else if (message instanceof HttpResponse) {
                    HttpResponse r = (HttpResponse)message;
                    copy = new DefaultHttpResponse(r.getProtocolVersion(), r.getStatus());
                } else {
                    throw new CodecException("Object of class " + message.getClass().getName() + " is not a HttpRequest or HttpResponse");
                }
                copy.headers().set(message.headers());
                copy.setDecoderResult(message.getDecoderResult());
                out.add(copy);
            } else {
                out.add(message);
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent c = (HttpContent)msg;
            if (this.decoder == null) {
                out.add(c.retain());
            } else {
                this.decodeContent(c, out);
            }
        }
    }

    private void decodeContent(HttpContent c, List<Object> out) {
        ByteBuf content = c.content();
        this.decode(content, out);
        if (c instanceof LastHttpContent) {
            this.finishDecode(out);
            LastHttpContent last = (LastHttpContent)c;
            HttpHeaders headers = last.trailingHeaders();
            if (headers.isEmpty()) {
                out.add(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                out.add(new ComposedLastHttpContent(headers));
            }
        }
    }

    protected abstract EmbeddedChannel newContentDecoder(String var1) throws Exception;

    protected String getTargetContentEncoding(String contentEncoding) throws Exception {
        return "identity";
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.cleanup();
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.cleanup();
        super.channelInactive(ctx);
    }

    private void cleanup() {
        if (this.decoder != null) {
            if (this.decoder.finish()) {
                ByteBuf buf;
                while ((buf = (ByteBuf)this.decoder.readInbound()) != null) {
                    buf.release();
                }
            }
            this.decoder = null;
        }
    }

    private void decode(ByteBuf in, List<Object> out) {
        this.decoder.writeInbound(in.retain());
        this.fetchDecoderOutput(out);
    }

    private void finishDecode(List<Object> out) {
        if (this.decoder.finish()) {
            this.fetchDecoderOutput(out);
        }
        this.decoder = null;
    }

    private void fetchDecoderOutput(List<Object> out) {
        ByteBuf buf;
        while ((buf = (ByteBuf)this.decoder.readInbound()) != null) {
            if (!buf.isReadable()) {
                buf.release();
                continue;
            }
            out.add(new DefaultHttpContent(buf));
        }
    }
}

