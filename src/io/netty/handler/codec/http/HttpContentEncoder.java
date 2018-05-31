/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.ComposedLastHttpContent;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public abstract class HttpContentEncoder
extends MessageToMessageCodec<HttpRequest, HttpObject> {
    private static final CharSequence ZERO_LENGTH_HEAD = "HEAD";
    private static final CharSequence ZERO_LENGTH_CONNECT = "CONNECT";
    private final Queue<CharSequence> acceptEncodingQueue = new ArrayDeque<CharSequence>();
    private CharSequence acceptEncoding;
    private EmbeddedChannel encoder;
    private State state = State.AWAIT_HEADERS;

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return msg instanceof HttpContent || msg instanceof HttpResponse;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) throws Exception {
        HttpMethod meth;
        CharSequence acceptedEncoding = msg.headers().get("Accept-Encoding");
        if (acceptedEncoding == null) {
            acceptedEncoding = "identity";
        }
        if ((meth = msg.getMethod()) == HttpMethod.HEAD) {
            acceptedEncoding = ZERO_LENGTH_HEAD;
        } else if (meth == HttpMethod.CONNECT) {
            acceptedEncoding = ZERO_LENGTH_CONNECT;
        }
        this.acceptEncodingQueue.add(acceptedEncoding);
        out.add(ReferenceCountUtil.retain(msg));
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        isFull = msg instanceof HttpResponse != false && msg instanceof LastHttpContent != false;
        switch (1.$SwitchMap$io$netty$handler$codec$http$HttpContentEncoder$State[this.state.ordinal()]) {
            case 1: {
                HttpContentEncoder.ensureHeaders(msg);
                if (!HttpContentEncoder.$assertionsDisabled && this.encoder != null) {
                    throw new AssertionError();
                }
                res = (HttpResponse)msg;
                this.acceptEncoding = this.acceptEncodingQueue.poll();
                if (this.acceptEncoding == null) {
                    throw new IllegalStateException("cannot send more responses than requests");
                }
                if (HttpContentEncoder.isPassthru(res, this.acceptEncoding)) {
                    if (isFull) {
                        out.add(ReferenceCountUtil.retain(res));
                        return;
                    }
                    out.add(res);
                    this.state = State.PASS_THROUGH;
                    return;
                }
                if (isFull && !((ByteBufHolder)res).content().isReadable()) {
                    out.add(ReferenceCountUtil.retain(res));
                    return;
                }
                result = this.beginEncode(res, this.acceptEncoding.toString());
                if (result == null) {
                    if (isFull) {
                        out.add(ReferenceCountUtil.retain(res));
                        return;
                    }
                    out.add(res);
                    this.state = State.PASS_THROUGH;
                    return;
                }
                this.encoder = result.contentEncoder();
                res.headers().set("Content-Encoding", (Object)result.targetContentEncoding());
                res.headers().remove("Content-Length");
                res.headers().set("Transfer-Encoding", (Object)"chunked");
                if (!isFull) ** GOTO lbl38
                newRes = new DefaultHttpResponse(res.getProtocolVersion(), res.getStatus());
                newRes.headers().set(res.headers());
                out.add(newRes);
                ** GOTO lbl42
lbl38: // 1 sources:
                out.add(res);
                this.state = State.AWAIT_CONTENT;
                if (!(msg instanceof HttpContent)) {
                    return;
                }
            }
lbl42: // 4 sources:
            case 2: {
                HttpContentEncoder.ensureContent(msg);
                if (this.encodeContent((HttpContent)msg, out) == false) return;
                this.state = State.AWAIT_HEADERS;
                return;
            }
            case 3: {
                HttpContentEncoder.ensureContent(msg);
                out.add(ReferenceCountUtil.retain(msg));
                if (msg instanceof LastHttpContent == false) return;
                this.state = State.AWAIT_HEADERS;
            }
        }
    }

    private static boolean isPassthru(HttpResponse res, CharSequence httpMethod) {
        int code = res.getStatus().code();
        boolean expectEmptyBody = httpMethod == ZERO_LENGTH_HEAD || httpMethod == ZERO_LENGTH_CONNECT && code == 200;
        return code < 200 || code == 204 || code == 304 || expectEmptyBody;
    }

    private static void ensureHeaders(HttpObject msg) {
        if (!(msg instanceof HttpResponse)) {
            throw new IllegalStateException("unexpected message type: " + msg.getClass().getName() + " (expected: " + HttpResponse.class.getSimpleName() + ')');
        }
    }

    private static void ensureContent(HttpObject msg) {
        if (!(msg instanceof HttpContent)) {
            throw new IllegalStateException("unexpected message type: " + msg.getClass().getName() + " (expected: " + HttpContent.class.getSimpleName() + ')');
        }
    }

    private boolean encodeContent(HttpContent c, List<Object> out) {
        ByteBuf content = c.content();
        this.encode(content, out);
        if (c instanceof LastHttpContent) {
            this.finishEncode(out);
            LastHttpContent last = (LastHttpContent)c;
            HttpHeaders headers = last.trailingHeaders();
            if (headers.isEmpty()) {
                out.add(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                out.add(new ComposedLastHttpContent(headers));
            }
            return true;
        }
        return false;
    }

    protected abstract Result beginEncode(HttpResponse var1, String var2) throws Exception;

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
        if (this.encoder != null) {
            if (this.encoder.finish()) {
                ByteBuf buf;
                while ((buf = (ByteBuf)this.encoder.readOutbound()) != null) {
                    buf.release();
                }
            }
            this.encoder = null;
        }
    }

    private void encode(ByteBuf in, List<Object> out) {
        this.encoder.writeOutbound(in.retain());
        this.fetchEncoderOutput(out);
    }

    private void finishEncode(List<Object> out) {
        if (this.encoder.finish()) {
            this.fetchEncoderOutput(out);
        }
        this.encoder = null;
    }

    private void fetchEncoderOutput(List<Object> out) {
        ByteBuf buf;
        while ((buf = (ByteBuf)this.encoder.readOutbound()) != null) {
            if (!buf.isReadable()) {
                buf.release();
                continue;
            }
            out.add(new DefaultHttpContent(buf));
        }
    }

    static class 1 {
        static final /* synthetic */ int[] $SwitchMap$io$netty$handler$codec$http$HttpContentEncoder$State;

        static {
            $SwitchMap$io$netty$handler$codec$http$HttpContentEncoder$State = new int[State.values().length];
            try {
                1.$SwitchMap$io$netty$handler$codec$http$HttpContentEncoder$State[State.AWAIT_HEADERS.ordinal()] = 1;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                1.$SwitchMap$io$netty$handler$codec$http$HttpContentEncoder$State[State.AWAIT_CONTENT.ordinal()] = 2;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                1.$SwitchMap$io$netty$handler$codec$http$HttpContentEncoder$State[State.PASS_THROUGH.ordinal()] = 3;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
        }
    }

    public static final class Result {
        private final String targetContentEncoding;
        private final EmbeddedChannel contentEncoder;

        public Result(String targetContentEncoding, EmbeddedChannel contentEncoder) {
            if (targetContentEncoding == null) {
                throw new NullPointerException("targetContentEncoding");
            }
            if (contentEncoder == null) {
                throw new NullPointerException("contentEncoder");
            }
            this.targetContentEncoding = targetContentEncoding;
            this.contentEncoder = contentEncoder;
        }

        public String targetContentEncoding() {
            return this.targetContentEncoding;
        }

        public EmbeddedChannel contentEncoder() {
            return this.contentEncoder;
        }
    }

    private static enum State {
        PASS_THROUGH,
        AWAIT_HEADERS,
        AWAIT_CONTENT;
        

        private State() {
        }
    }

}

