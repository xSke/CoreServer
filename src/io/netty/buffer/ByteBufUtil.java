/*
 * Decompiled with CFR 0_129.
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.util.CharsetUtil;
import io.netty.util.Recycler;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Locale;

public final class ByteBufUtil {
    private static final InternalLogger logger;
    private static final char[] HEXDUMP_TABLE;
    private static final String NEWLINE;
    private static final String[] BYTE2HEX;
    private static final String[] HEXPADDING;
    private static final String[] BYTEPADDING;
    private static final char[] BYTE2CHAR;
    private static final String[] HEXDUMP_ROWPREFIXES;
    static final ByteBufAllocator DEFAULT_ALLOCATOR;
    private static final int THREAD_LOCAL_BUFFER_SIZE;

    public static String hexDump(ByteBuf buffer) {
        return ByteBufUtil.hexDump(buffer, buffer.readerIndex(), buffer.readableBytes());
    }

    public static String hexDump(ByteBuf buffer, int fromIndex, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        if (length == 0) {
            return "";
        }
        int endIndex = fromIndex + length;
        char[] buf = new char[length << 1];
        int srcIdx = fromIndex;
        int dstIdx = 0;
        while (srcIdx < endIndex) {
            System.arraycopy(HEXDUMP_TABLE, buffer.getUnsignedByte(srcIdx) << 1, buf, dstIdx, 2);
            ++srcIdx;
            dstIdx += 2;
        }
        return new String(buf);
    }

    public static int hashCode(ByteBuf buffer) {
        int i;
        int aLen = buffer.readableBytes();
        int intCount = aLen >>> 2;
        int byteCount = aLen & 3;
        int hashCode = 1;
        int arrayIndex = buffer.readerIndex();
        if (buffer.order() == ByteOrder.BIG_ENDIAN) {
            for (i = intCount; i > 0; --i) {
                hashCode = 31 * hashCode + buffer.getInt(arrayIndex);
                arrayIndex += 4;
            }
        } else {
            for (i = intCount; i > 0; --i) {
                hashCode = 31 * hashCode + ByteBufUtil.swapInt(buffer.getInt(arrayIndex));
                arrayIndex += 4;
            }
        }
        for (i = byteCount; i > 0; --i) {
            hashCode = 31 * hashCode + buffer.getByte(arrayIndex++);
        }
        if (hashCode == 0) {
            hashCode = 1;
        }
        return hashCode;
    }

    public static boolean equals(ByteBuf bufferA, ByteBuf bufferB) {
        int i;
        int aLen = bufferA.readableBytes();
        if (aLen != bufferB.readableBytes()) {
            return false;
        }
        int longCount = aLen >>> 3;
        int byteCount = aLen & 7;
        int aIndex = bufferA.readerIndex();
        int bIndex = bufferB.readerIndex();
        if (bufferA.order() == bufferB.order()) {
            for (i = longCount; i > 0; --i) {
                if (bufferA.getLong(aIndex) != bufferB.getLong(bIndex)) {
                    return false;
                }
                aIndex += 8;
                bIndex += 8;
            }
        } else {
            for (i = longCount; i > 0; --i) {
                if (bufferA.getLong(aIndex) != ByteBufUtil.swapLong(bufferB.getLong(bIndex))) {
                    return false;
                }
                aIndex += 8;
                bIndex += 8;
            }
        }
        for (i = byteCount; i > 0; --i) {
            if (bufferA.getByte(aIndex) != bufferB.getByte(bIndex)) {
                return false;
            }
            ++aIndex;
            ++bIndex;
        }
        return true;
    }

    public static int compare(ByteBuf bufferA, ByteBuf bufferB) {
        int i;
        long vb;
        long va;
        int aLen = bufferA.readableBytes();
        int bLen = bufferB.readableBytes();
        int minLength = Math.min(aLen, bLen);
        int uintCount = minLength >>> 2;
        int byteCount = minLength & 3;
        int aIndex = bufferA.readerIndex();
        int bIndex = bufferB.readerIndex();
        if (bufferA.order() == bufferB.order()) {
            for (i = uintCount; i > 0; --i) {
                va = bufferA.getUnsignedInt(aIndex);
                if (va > (vb = bufferB.getUnsignedInt(bIndex))) {
                    return 1;
                }
                if (va < vb) {
                    return -1;
                }
                aIndex += 4;
                bIndex += 4;
            }
        } else {
            for (i = uintCount; i > 0; --i) {
                va = bufferA.getUnsignedInt(aIndex);
                if (va > (vb = (long)ByteBufUtil.swapInt(bufferB.getInt(bIndex)) & 0xFFFFFFFFL)) {
                    return 1;
                }
                if (va < vb) {
                    return -1;
                }
                aIndex += 4;
                bIndex += 4;
            }
        }
        for (i = byteCount; i > 0; --i) {
            short vb2;
            short va2 = bufferA.getUnsignedByte(aIndex);
            if (va2 > (vb2 = bufferB.getUnsignedByte(bIndex))) {
                return 1;
            }
            if (va2 < vb2) {
                return -1;
            }
            ++aIndex;
            ++bIndex;
        }
        return aLen - bLen;
    }

    public static int indexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value) {
        if (fromIndex <= toIndex) {
            return ByteBufUtil.firstIndexOf(buffer, fromIndex, toIndex, value);
        }
        return ByteBufUtil.lastIndexOf(buffer, fromIndex, toIndex, value);
    }

    public static short swapShort(short value) {
        return Short.reverseBytes(value);
    }

    public static int swapMedium(int value) {
        int swapped = value << 16 & 16711680 | value & 65280 | value >>> 16 & 255;
        if ((swapped & 8388608) != 0) {
            swapped |= -16777216;
        }
        return swapped;
    }

    public static int swapInt(int value) {
        return Integer.reverseBytes(value);
    }

    public static long swapLong(long value) {
        return Long.reverseBytes(value);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ByteBuf readBytes(ByteBufAllocator alloc, ByteBuf buffer, int length) {
        boolean release = true;
        ByteBuf dst = alloc.buffer(length);
        try {
            buffer.readBytes(dst);
            release = false;
            ByteBuf byteBuf = dst;
            return byteBuf;
        }
        finally {
            if (release) {
                dst.release();
            }
        }
    }

    private static int firstIndexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value) {
        if ((fromIndex = Math.max(fromIndex, 0)) >= toIndex || buffer.capacity() == 0) {
            return -1;
        }
        return buffer.forEachByte(fromIndex, toIndex - fromIndex, new IndexOfProcessor(value));
    }

    private static int lastIndexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value) {
        if ((fromIndex = Math.min(fromIndex, buffer.capacity())) < 0 || buffer.capacity() == 0) {
            return -1;
        }
        return buffer.forEachByteDesc(toIndex, fromIndex - toIndex, new IndexOfProcessor(value));
    }

    public static int writeUtf8(ByteBuf buf, CharSequence seq) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        if (seq == null) {
            throw new NullPointerException("seq");
        }
        int len = seq.length();
        int maxSize = len * 3;
        buf.ensureWritable(maxSize);
        if (buf instanceof AbstractByteBuf) {
            int oldWriterIndex;
            AbstractByteBuf buffer = (AbstractByteBuf)buf;
            int writerIndex = oldWriterIndex = buffer.writerIndex;
            for (int i = 0; i < len; ++i) {
                char c = seq.charAt(i);
                if (c < '') {
                    buffer._setByte(writerIndex++, (byte)c);
                    continue;
                }
                if (c < '\u0800') {
                    buffer._setByte(writerIndex++, (byte)(192 | c >> 6));
                    buffer._setByte(writerIndex++, (byte)(128 | c & 63));
                    continue;
                }
                buffer._setByte(writerIndex++, (byte)(224 | c >> 12));
                buffer._setByte(writerIndex++, (byte)(128 | c >> 6 & 63));
                buffer._setByte(writerIndex++, (byte)(128 | c & 63));
            }
            buffer.writerIndex = writerIndex;
            return writerIndex - oldWriterIndex;
        }
        byte[] bytes = seq.toString().getBytes(CharsetUtil.UTF_8);
        buf.writeBytes(bytes);
        return bytes.length;
    }

    public static int writeAscii(ByteBuf buf, CharSequence seq) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        if (seq == null) {
            throw new NullPointerException("seq");
        }
        int len = seq.length();
        buf.ensureWritable(len);
        if (buf instanceof AbstractByteBuf) {
            AbstractByteBuf buffer = (AbstractByteBuf)buf;
            int writerIndex = buffer.writerIndex;
            for (int i = 0; i < len; ++i) {
                buffer._setByte(writerIndex++, (byte)seq.charAt(i));
            }
            buffer.writerIndex = writerIndex;
        } else {
            buf.writeBytes(seq.toString().getBytes(CharsetUtil.US_ASCII));
        }
        return len;
    }

    public static ByteBuf encodeString(ByteBufAllocator alloc, CharBuffer src, Charset charset) {
        return ByteBufUtil.encodeString0(alloc, false, src, charset);
    }

    static ByteBuf encodeString0(ByteBufAllocator alloc, boolean enforceHeap, CharBuffer src, Charset charset) {
        CharsetEncoder encoder = CharsetUtil.getEncoder(charset);
        int length = (int)((double)src.remaining() * (double)encoder.maxBytesPerChar());
        boolean release = true;
        ByteBuf dst = enforceHeap ? alloc.heapBuffer(length) : alloc.buffer(length);
        try {
            ByteBuffer dstBuf = dst.internalNioBuffer(0, length);
            int pos = dstBuf.position();
            CoderResult cr = encoder.encode(src, dstBuf, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            if (!(cr = encoder.flush(dstBuf)).isUnderflow()) {
                cr.throwException();
            }
            dst.writerIndex(dst.writerIndex() + dstBuf.position() - pos);
            release = false;
            ByteBuf byteBuf = dst;
            return byteBuf;
        }
        catch (CharacterCodingException x) {
            throw new IllegalStateException(x);
        }
        finally {
            if (release) {
                dst.release();
            }
        }
    }

    static String decodeString(ByteBuffer src, Charset charset) {
        CharsetDecoder decoder = CharsetUtil.getDecoder(charset);
        CharBuffer dst = CharBuffer.allocate((int)((double)src.remaining() * (double)decoder.maxCharsPerByte()));
        try {
            CoderResult cr = decoder.decode(src, dst, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            if (!(cr = decoder.flush(dst)).isUnderflow()) {
                cr.throwException();
            }
        }
        catch (CharacterCodingException x) {
            throw new IllegalStateException(x);
        }
        return dst.flip().toString();
    }

    public static String prettyHexDump(ByteBuf buffer) {
        return ByteBufUtil.prettyHexDump(buffer, buffer.readerIndex(), buffer.readableBytes());
    }

    public static String prettyHexDump(ByteBuf buffer, int offset, int length) {
        if (length == 0) {
            return "";
        }
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80);
        ByteBufUtil.appendPrettyHexDump(buf, buffer, offset, length);
        return buf.toString();
    }

    public static void appendPrettyHexDump(StringBuilder dump, ByteBuf buf) {
        ByteBufUtil.appendPrettyHexDump(dump, buf, buf.readerIndex(), buf.readableBytes());
    }

    public static void appendPrettyHexDump(StringBuilder dump, ByteBuf buf, int offset, int length) {
        if (offset < 0 || length > ObjectUtil.checkNotNull(buf, "buf").capacity() - offset) {
            throw new IndexOutOfBoundsException("expected: 0 <= offset(" + offset + ") <= offset + length(" + length + ") <= " + "buf.capacity(" + buf.capacity() + ')');
        }
        if (length == 0) {
            return;
        }
        dump.append("         +-------------------------------------------------+" + NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" + NEWLINE + "+--------+-------------------------------------------------+----------------+");
        int startIndex = offset;
        int fullRows = length >>> 4;
        int remainder = length & 15;
        for (int row = 0; row < fullRows; ++row) {
            int j;
            int rowStartIndex = (row << 4) + startIndex;
            ByteBufUtil.appendHexDumpRowPrefix(dump, row, rowStartIndex);
            int rowEndIndex = rowStartIndex + 16;
            for (j = rowStartIndex; j < rowEndIndex; ++j) {
                dump.append(BYTE2HEX[buf.getUnsignedByte(j)]);
            }
            dump.append(" |");
            for (j = rowStartIndex; j < rowEndIndex; ++j) {
                dump.append(BYTE2CHAR[buf.getUnsignedByte(j)]);
            }
            dump.append('|');
        }
        if (remainder != 0) {
            int j;
            int rowStartIndex = (fullRows << 4) + startIndex;
            ByteBufUtil.appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);
            int rowEndIndex = rowStartIndex + remainder;
            for (j = rowStartIndex; j < rowEndIndex; ++j) {
                dump.append(BYTE2HEX[buf.getUnsignedByte(j)]);
            }
            dump.append(HEXPADDING[remainder]);
            dump.append(" |");
            for (j = rowStartIndex; j < rowEndIndex; ++j) {
                dump.append(BYTE2CHAR[buf.getUnsignedByte(j)]);
            }
            dump.append(BYTEPADDING[remainder]);
            dump.append('|');
        }
        dump.append(NEWLINE + "+--------+-------------------------------------------------+----------------+");
    }

    private static void appendHexDumpRowPrefix(StringBuilder dump, int row, int rowStartIndex) {
        if (row < HEXDUMP_ROWPREFIXES.length) {
            dump.append(HEXDUMP_ROWPREFIXES[row]);
        } else {
            dump.append(NEWLINE);
            dump.append(Long.toHexString((long)rowStartIndex & 0xFFFFFFFFL | 0x100000000L));
            dump.setCharAt(dump.length() - 9, '|');
            dump.append('|');
        }
    }

    public static ByteBuf threadLocalDirectBuffer() {
        if (THREAD_LOCAL_BUFFER_SIZE <= 0) {
            return null;
        }
        if (PlatformDependent.hasUnsafe()) {
            return ThreadLocalUnsafeDirectByteBuf.newInstance();
        }
        return ThreadLocalDirectByteBuf.newInstance();
    }

    private ByteBufUtil() {
    }

    static {
        AbstractByteBufAllocator alloc;
        int i;
        StringBuilder buf;
        int padding;
        int j;
        logger = InternalLoggerFactory.getInstance(ByteBufUtil.class);
        HEXDUMP_TABLE = new char[1024];
        NEWLINE = StringUtil.NEWLINE;
        BYTE2HEX = new String[256];
        HEXPADDING = new String[16];
        BYTEPADDING = new String[16];
        BYTE2CHAR = new char[256];
        HEXDUMP_ROWPREFIXES = new String[4096];
        char[] DIGITS = "0123456789abcdef".toCharArray();
        for (i = 0; i < 256; ++i) {
            ByteBufUtil.HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 15];
            ByteBufUtil.HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 15];
        }
        for (i = 0; i < BYTE2HEX.length; ++i) {
            ByteBufUtil.BYTE2HEX[i] = "" + ' ' + StringUtil.byteToHexStringPadded(i);
        }
        for (i = 0; i < HEXPADDING.length; ++i) {
            padding = HEXPADDING.length - i;
            buf = new StringBuilder(padding * 3);
            for (j = 0; j < padding; ++j) {
                buf.append("   ");
            }
            ByteBufUtil.HEXPADDING[i] = buf.toString();
        }
        for (i = 0; i < BYTEPADDING.length; ++i) {
            padding = BYTEPADDING.length - i;
            buf = new StringBuilder(padding);
            for (j = 0; j < padding; ++j) {
                buf.append(' ');
            }
            ByteBufUtil.BYTEPADDING[i] = buf.toString();
        }
        for (i = 0; i < BYTE2CHAR.length; ++i) {
            ByteBufUtil.BYTE2CHAR[i] = i <= 31 || i >= 127 ? 46 : (char)i;
        }
        for (i = 0; i < HEXDUMP_ROWPREFIXES.length; ++i) {
            StringBuilder buf2 = new StringBuilder(12);
            buf2.append(NEWLINE);
            buf2.append(Long.toHexString((long)(i << 4) & 0xFFFFFFFFL | 0x100000000L));
            buf2.setCharAt(buf2.length() - 9, '|');
            buf2.append('|');
            ByteBufUtil.HEXDUMP_ROWPREFIXES[i] = buf2.toString();
        }
        String allocType = SystemPropertyUtil.get("io.netty.allocator.type", "unpooled").toLowerCase(Locale.US).trim();
        if ("unpooled".equals(allocType)) {
            alloc = UnpooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", (Object)allocType);
        } else if ("pooled".equals(allocType)) {
            alloc = PooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", (Object)allocType);
        } else {
            alloc = UnpooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: unpooled (unknown: {})", (Object)allocType);
        }
        DEFAULT_ALLOCATOR = alloc;
        THREAD_LOCAL_BUFFER_SIZE = SystemPropertyUtil.getInt("io.netty.threadLocalDirectBufferSize", 65536);
        logger.debug("-Dio.netty.threadLocalDirectBufferSize: {}", (Object)THREAD_LOCAL_BUFFER_SIZE);
    }

    private static class IndexOfProcessor
    implements ByteBufProcessor {
        private final byte byteToFind;

        public IndexOfProcessor(byte byteToFind) {
            this.byteToFind = byteToFind;
        }

        @Override
        public boolean process(byte value) {
            return value != this.byteToFind;
        }
    }

    static final class ThreadLocalDirectByteBuf
    extends UnpooledDirectByteBuf {
        private static final Recycler<ThreadLocalDirectByteBuf> RECYCLER = new Recycler<ThreadLocalDirectByteBuf>(){

            @Override
            protected ThreadLocalDirectByteBuf newObject(Recycler.Handle handle) {
                return new ThreadLocalDirectByteBuf(handle);
            }
        };
        private final Recycler.Handle handle;

        static ThreadLocalDirectByteBuf newInstance() {
            ThreadLocalDirectByteBuf buf = RECYCLER.get();
            buf.setRefCnt(1);
            return buf;
        }

        private ThreadLocalDirectByteBuf(Recycler.Handle handle) {
            super((ByteBufAllocator)UnpooledByteBufAllocator.DEFAULT, 256, Integer.MAX_VALUE);
            this.handle = handle;
        }

        @Override
        protected void deallocate() {
            if (this.capacity() > THREAD_LOCAL_BUFFER_SIZE) {
                super.deallocate();
            } else {
                this.clear();
                RECYCLER.recycle(this, this.handle);
            }
        }

    }

    static final class ThreadLocalUnsafeDirectByteBuf
    extends UnpooledUnsafeDirectByteBuf {
        private static final Recycler<ThreadLocalUnsafeDirectByteBuf> RECYCLER = new Recycler<ThreadLocalUnsafeDirectByteBuf>(){

            @Override
            protected ThreadLocalUnsafeDirectByteBuf newObject(Recycler.Handle handle) {
                return new ThreadLocalUnsafeDirectByteBuf(handle);
            }
        };
        private final Recycler.Handle handle;

        static ThreadLocalUnsafeDirectByteBuf newInstance() {
            ThreadLocalUnsafeDirectByteBuf buf = RECYCLER.get();
            buf.setRefCnt(1);
            return buf;
        }

        private ThreadLocalUnsafeDirectByteBuf(Recycler.Handle handle) {
            super((ByteBufAllocator)UnpooledByteBufAllocator.DEFAULT, 256, Integer.MAX_VALUE);
            this.handle = handle;
        }

        @Override
        protected void deallocate() {
            if (this.capacity() > THREAD_LOCAL_BUFFER_SIZE) {
                super.deallocate();
            } else {
                this.clear();
                RECYCLER.recycle(this, this.handle);
            }
        }

    }

}

