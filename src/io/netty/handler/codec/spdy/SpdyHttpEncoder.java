/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.spdy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.http.FullHttpMessage;
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
import io.netty.handler.codec.spdy.DefaultSpdyDataFrame;
import io.netty.handler.codec.spdy.DefaultSpdyHeadersFrame;
import io.netty.handler.codec.spdy.DefaultSpdySynReplyFrame;
import io.netty.handler.codec.spdy.DefaultSpdySynStreamFrame;
import io.netty.handler.codec.spdy.SpdyCodecUtil;
import io.netty.handler.codec.spdy.SpdyDataFrame;
import io.netty.handler.codec.spdy.SpdyHeaders;
import io.netty.handler.codec.spdy.SpdyHeadersFrame;
import io.netty.handler.codec.spdy.SpdyHttpHeaders;
import io.netty.handler.codec.spdy.SpdySynStreamFrame;
import io.netty.handler.codec.spdy.SpdyVersion;
import java.util.List;
import java.util.Map;

public class SpdyHttpEncoder
extends MessageToMessageEncoder<HttpObject> {
    private int currentStreamId;

    public SpdyHttpEncoder(SpdyVersion version) {
        if (version == null) {
            throw new NullPointerException("version");
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        boolean valid = false;
        boolean last = false;
        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest)msg;
            SpdySynStreamFrame spdySynStreamFrame = this.createSynStreamFrame(httpRequest);
            out.add(spdySynStreamFrame);
            last = spdySynStreamFrame.isLast() || spdySynStreamFrame.isUnidirectional();
            valid = true;
        }
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse)msg;
            SpdyHeadersFrame spdyHeadersFrame = this.createHeadersFrame(httpResponse);
            out.add(spdyHeadersFrame);
            last = spdyHeadersFrame.isLast();
            valid = true;
        }
        if (msg instanceof HttpContent && !last) {
            HttpContent chunk = (HttpContent)msg;
            chunk.content().retain();
            DefaultSpdyDataFrame spdyDataFrame = new DefaultSpdyDataFrame(this.currentStreamId, chunk.content());
            if (chunk instanceof LastHttpContent) {
                LastHttpContent trailer = (LastHttpContent)chunk;
                HttpHeaders trailers = trailer.trailingHeaders();
                if (trailers.isEmpty()) {
                    spdyDataFrame.setLast(true);
                    out.add(spdyDataFrame);
                } else {
                    DefaultSpdyHeadersFrame spdyHeadersFrame = new DefaultSpdyHeadersFrame(this.currentStreamId);
                    spdyHeadersFrame.setLast(true);
                    for (Map.Entry entry : trailers) {
                        spdyHeadersFrame.headers().add((String)entry.getKey(), entry.getValue());
                    }
                    out.add(spdyDataFrame);
                    out.add(spdyHeadersFrame);
                }
            } else {
                out.add(spdyDataFrame);
            }
            valid = true;
        }
        if (!valid) {
            throw new UnsupportedMessageTypeException(msg, new Class[0]);
        }
    }

    private SpdySynStreamFrame createSynStreamFrame(HttpRequest httpRequest) throws Exception {
        HttpHeaders httpHeaders = httpRequest.headers();
        int streamId = SpdyHttpHeaders.getStreamId(httpRequest);
        int associatedToStreamId = SpdyHttpHeaders.getAssociatedToStreamId(httpRequest);
        byte priority = SpdyHttpHeaders.getPriority(httpRequest);
        String scheme = httpHeaders.get("X-SPDY-Scheme");
        httpHeaders.remove("X-SPDY-Stream-ID");
        httpHeaders.remove("X-SPDY-Associated-To-Stream-ID");
        httpHeaders.remove("X-SPDY-Priority");
        httpHeaders.remove("X-SPDY-Scheme");
        httpHeaders.remove("Connection");
        httpHeaders.remove("Keep-Alive");
        httpHeaders.remove("Proxy-Connection");
        httpHeaders.remove("Transfer-Encoding");
        DefaultSpdySynStreamFrame spdySynStreamFrame = new DefaultSpdySynStreamFrame(streamId, associatedToStreamId, priority);
        SpdyHeaders frameHeaders = spdySynStreamFrame.headers();
        frameHeaders.set(":method", httpRequest.getMethod());
        frameHeaders.set(":path", httpRequest.getUri());
        frameHeaders.set(":version", httpRequest.getProtocolVersion());
        String host = httpHeaders.get("Host");
        httpHeaders.remove("Host");
        frameHeaders.set(":host", host);
        if (scheme == null) {
            scheme = "https";
        }
        frameHeaders.set(":scheme", scheme);
        for (Map.Entry entry : httpHeaders) {
            frameHeaders.add((String)entry.getKey(), entry.getValue());
        }
        this.currentStreamId = spdySynStreamFrame.streamId();
        if (associatedToStreamId == 0) {
            spdySynStreamFrame.setLast(SpdyHttpEncoder.isLast(httpRequest));
        } else {
            spdySynStreamFrame.setUnidirectional(true);
        }
        return spdySynStreamFrame;
    }

    private SpdyHeadersFrame createHeadersFrame(HttpResponse httpResponse) throws Exception {
        HttpHeaders httpHeaders = httpResponse.headers();
        int streamId = SpdyHttpHeaders.getStreamId(httpResponse);
        httpHeaders.remove("X-SPDY-Stream-ID");
        httpHeaders.remove("Connection");
        httpHeaders.remove("Keep-Alive");
        httpHeaders.remove("Proxy-Connection");
        httpHeaders.remove("Transfer-Encoding");
        DefaultSpdyHeadersFrame spdyHeadersFrame = SpdyCodecUtil.isServerId(streamId) ? new DefaultSpdyHeadersFrame(streamId) : new DefaultSpdySynReplyFrame(streamId);
        SpdyHeaders frameHeaders = spdyHeadersFrame.headers();
        frameHeaders.set(":status", httpResponse.getStatus().code());
        frameHeaders.set(":version", httpResponse.getProtocolVersion());
        for (Map.Entry entry : httpHeaders) {
            spdyHeadersFrame.headers().add((String)entry.getKey(), entry.getValue());
        }
        this.currentStreamId = streamId;
        spdyHeadersFrame.setLast(SpdyHttpEncoder.isLast(httpResponse));
        return spdyHeadersFrame;
    }

    private static boolean isLast(HttpMessage httpMessage) {
        FullHttpMessage fullMessage;
        if (httpMessage instanceof FullHttpMessage && (fullMessage = (FullHttpMessage)httpMessage).trailingHeaders().isEmpty() && !fullMessage.content().isReadable()) {
            return true;
        }
        return false;
    }
}

