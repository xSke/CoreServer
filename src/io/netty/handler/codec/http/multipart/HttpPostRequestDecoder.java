/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http.multipart;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostBodyUtil;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostStandardRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import io.netty.util.internal.StringUtil;
import java.nio.charset.Charset;
import java.util.List;

public class HttpPostRequestDecoder
implements InterfaceHttpPostRequestDecoder {
    static final int DEFAULT_DISCARD_THRESHOLD = 10485760;
    private final InterfaceHttpPostRequestDecoder decoder;

    public HttpPostRequestDecoder(HttpRequest request) throws ErrorDataDecoderException, IncompatibleDataDecoderException {
        this(new DefaultHttpDataFactory(16384L), request, HttpConstants.DEFAULT_CHARSET);
    }

    public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request) throws ErrorDataDecoderException, IncompatibleDataDecoderException {
        this(factory, request, HttpConstants.DEFAULT_CHARSET);
    }

    public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset) throws ErrorDataDecoderException, IncompatibleDataDecoderException {
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        if (request == null) {
            throw new NullPointerException("request");
        }
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.decoder = HttpPostRequestDecoder.isMultipart(request) ? new HttpPostMultipartRequestDecoder(factory, request, charset) : new HttpPostStandardRequestDecoder(factory, request, charset);
    }

    public static boolean isMultipart(HttpRequest request) {
        if (request.headers().contains("Content-Type")) {
            return HttpPostRequestDecoder.getMultipartDataBoundary(request.headers().get("Content-Type")) != null;
        }
        return false;
    }

    protected static String[] getMultipartDataBoundary(String contentType) {
        String[] headerContentType = HttpPostRequestDecoder.splitHeaderContentType(contentType);
        if (headerContentType[0].toLowerCase().startsWith("multipart/form-data")) {
            String charset;
            String bound;
            int mrank;
            int index;
            int crank;
            if (headerContentType[1].toLowerCase().startsWith("boundary")) {
                mrank = 1;
                crank = 2;
            } else if (headerContentType[2].toLowerCase().startsWith("boundary")) {
                mrank = 2;
                crank = 1;
            } else {
                return null;
            }
            String boundary = StringUtil.substringAfter(headerContentType[mrank], '=');
            if (boundary == null) {
                throw new ErrorDataDecoderException("Needs a boundary value");
            }
            if (boundary.charAt(0) == '\"' && (bound = boundary.trim()).charAt(index = bound.length() - 1) == '\"') {
                boundary = bound.substring(1, index);
            }
            if (headerContentType[crank].toLowerCase().startsWith("charset") && (charset = StringUtil.substringAfter(headerContentType[crank], '=')) != null) {
                return new String[]{"--" + boundary, charset};
            }
            return new String[]{"--" + boundary};
        }
        return null;
    }

    @Override
    public boolean isMultipart() {
        return this.decoder.isMultipart();
    }

    @Override
    public void setDiscardThreshold(int discardThreshold) {
        this.decoder.setDiscardThreshold(discardThreshold);
    }

    @Override
    public int getDiscardThreshold() {
        return this.decoder.getDiscardThreshold();
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas() {
        return this.decoder.getBodyHttpDatas();
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas(String name) {
        return this.decoder.getBodyHttpDatas(name);
    }

    @Override
    public InterfaceHttpData getBodyHttpData(String name) {
        return this.decoder.getBodyHttpData(name);
    }

    @Override
    public InterfaceHttpPostRequestDecoder offer(HttpContent content) {
        return this.decoder.offer(content);
    }

    @Override
    public boolean hasNext() {
        return this.decoder.hasNext();
    }

    @Override
    public InterfaceHttpData next() {
        return this.decoder.next();
    }

    @Override
    public void destroy() {
        this.decoder.destroy();
    }

    @Override
    public void cleanFiles() {
        this.decoder.cleanFiles();
    }

    @Override
    public void removeHttpDataFromClean(InterfaceHttpData data) {
        this.decoder.removeHttpDataFromClean(data);
    }

    protected void addHttpData(InterfaceHttpData data) {
        if (this.decoder instanceof HttpPostMultipartRequestDecoder) {
            ((HttpPostMultipartRequestDecoder)this.decoder).addHttpData(data);
        } else {
            ((HttpPostStandardRequestDecoder)this.decoder).addHttpData(data);
        }
    }

    protected InterfaceHttpData getFileUpload(String delimiter) {
        if (this.decoder instanceof HttpPostMultipartRequestDecoder) {
            ((HttpPostMultipartRequestDecoder)this.decoder).getFileUpload(delimiter);
        }
        return null;
    }

    private static String[] splitHeaderContentType(String sb) {
        int bEnd;
        int aStart = HttpPostBodyUtil.findNonWhitespace(sb, 0);
        int aEnd = sb.indexOf(59);
        if (aEnd == -1) {
            return new String[]{sb, "", ""};
        }
        int bStart = HttpPostBodyUtil.findNonWhitespace(sb, aEnd + 1);
        if (sb.charAt(aEnd - 1) == ' ') {
            --aEnd;
        }
        if ((bEnd = sb.indexOf(59, bStart)) == -1) {
            bEnd = HttpPostBodyUtil.findEndOfString(sb);
            return new String[]{sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), ""};
        }
        int cStart = HttpPostBodyUtil.findNonWhitespace(sb, bEnd + 1);
        if (sb.charAt(bEnd - 1) == ' ') {
            --bEnd;
        }
        int cEnd = HttpPostBodyUtil.findEndOfString(sb);
        return new String[]{sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), sb.substring(cStart, cEnd)};
    }

    public static class IncompatibleDataDecoderException
    extends DecoderException {
        private static final long serialVersionUID = -953268047926250267L;

        public IncompatibleDataDecoderException() {
        }

        public IncompatibleDataDecoderException(String msg) {
            super(msg);
        }

        public IncompatibleDataDecoderException(Throwable cause) {
            super(cause);
        }

        public IncompatibleDataDecoderException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    public static class ErrorDataDecoderException
    extends DecoderException {
        private static final long serialVersionUID = 5020247425493164465L;

        public ErrorDataDecoderException() {
        }

        public ErrorDataDecoderException(String msg) {
            super(msg);
        }

        public ErrorDataDecoderException(Throwable cause) {
            super(cause);
        }

        public ErrorDataDecoderException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    public static class EndOfDataDecoderException
    extends DecoderException {
        private static final long serialVersionUID = 1336267941020800769L;
    }

    public static class NotEnoughDataDecoderException
    extends DecoderException {
        private static final long serialVersionUID = -7846841864603865638L;

        public NotEnoughDataDecoderException() {
        }

        public NotEnoughDataDecoderException(String msg) {
            super(msg);
        }

        public NotEnoughDataDecoderException(Throwable cause) {
            super(cause);
        }

        public NotEnoughDataDecoderException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    protected static enum MultiPartStatus {
        NOTSTARTED,
        PREAMBLE,
        HEADERDELIMITER,
        DISPOSITION,
        FIELD,
        FILEUPLOAD,
        MIXEDPREAMBLE,
        MIXEDDELIMITER,
        MIXEDDISPOSITION,
        MIXEDFILEUPLOAD,
        MIXEDCLOSEDELIMITER,
        CLOSEDELIMITER,
        PREEPILOGUE,
        EPILOGUE;
        

        private MultiPartStatus() {
        }
    }

}

