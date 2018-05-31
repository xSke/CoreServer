/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.io;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteProcessor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

@Beta
public final class ByteStreams {
    private static final int BUF_SIZE = 4096;
    private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream(){

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b) {
            Preconditions.checkNotNull(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            Preconditions.checkNotNull(b);
        }

        public String toString() {
            return "ByteStreams.nullOutputStream()";
        }
    };

    private ByteStreams() {
    }

    public static long copy(InputStream from, OutputStream to) throws IOException {
        int r;
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        byte[] buf = new byte[4096];
        long total = 0L;
        while ((r = from.read(buf)) != -1) {
            to.write(buf, 0, r);
            total += (long)r;
        }
        return total;
    }

    public static long copy(ReadableByteChannel from, WritableByteChannel to) throws IOException {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        ByteBuffer buf = ByteBuffer.allocate(4096);
        long total = 0L;
        while (from.read(buf) != -1) {
            buf.flip();
            while (buf.hasRemaining()) {
                total += (long)to.write(buf);
            }
            buf.clear();
        }
        return total;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(in, out);
        return out.toByteArray();
    }

    static byte[] toByteArray(InputStream in, int expectedSize) throws IOException {
        int read;
        byte[] bytes = new byte[expectedSize];
        for (int remaining = expectedSize; remaining > 0; remaining -= read) {
            int off = expectedSize - remaining;
            read = in.read(bytes, off, remaining);
            if (read != -1) continue;
            return Arrays.copyOf(bytes, off);
        }
        int b = in.read();
        if (b == -1) {
            return bytes;
        }
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write(b);
        ByteStreams.copy(in, out);
        byte[] result = new byte[bytes.length + out.size()];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        out.writeTo(result, bytes.length);
        return result;
    }

    public static ByteArrayDataInput newDataInput(byte[] bytes) {
        return ByteStreams.newDataInput(new ByteArrayInputStream(bytes));
    }

    public static ByteArrayDataInput newDataInput(byte[] bytes, int start) {
        Preconditions.checkPositionIndex(start, bytes.length);
        return ByteStreams.newDataInput(new ByteArrayInputStream(bytes, start, bytes.length - start));
    }

    public static ByteArrayDataInput newDataInput(ByteArrayInputStream byteArrayInputStream) {
        return new ByteArrayDataInputStream(Preconditions.checkNotNull(byteArrayInputStream));
    }

    public static ByteArrayDataOutput newDataOutput() {
        return ByteStreams.newDataOutput(new ByteArrayOutputStream());
    }

    public static ByteArrayDataOutput newDataOutput(int size) {
        Preconditions.checkArgument(size >= 0, "Invalid size: %s", size);
        return ByteStreams.newDataOutput(new ByteArrayOutputStream(size));
    }

    public static ByteArrayDataOutput newDataOutput(ByteArrayOutputStream byteArrayOutputSteam) {
        return new ByteArrayDataOutputStream(Preconditions.checkNotNull(byteArrayOutputSteam));
    }

    public static OutputStream nullOutputStream() {
        return NULL_OUTPUT_STREAM;
    }

    public static InputStream limit(InputStream in, long limit) {
        return new LimitedInputStream(in, limit);
    }

    public static void readFully(InputStream in, byte[] b) throws IOException {
        ByteStreams.readFully(in, b, 0, b.length);
    }

    public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        int read = ByteStreams.read(in, b, off, len);
        if (read != len) {
            int n = read;
            int n2 = len;
            throw new EOFException(new StringBuilder(81).append("reached end of stream after reading ").append(n).append(" bytes; ").append(n2).append(" bytes expected").toString());
        }
    }

    public static void skipFully(InputStream in, long n) throws IOException {
        long toSkip = n;
        while (n > 0L) {
            long amt = in.skip(n);
            if (amt == 0L) {
                if (in.read() == -1) {
                    long skipped;
                    long l = skipped = toSkip - n;
                    long l2 = toSkip;
                    throw new EOFException(new StringBuilder(100).append("reached end of stream after skipping ").append(l).append(" bytes; ").append(l2).append(" bytes expected").toString());
                }
                --n;
                continue;
            }
            n -= amt;
        }
    }

    public static <T> T readBytes(InputStream input, ByteProcessor<T> processor) throws IOException {
        int read;
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(processor);
        byte[] buf = new byte[4096];
        while ((read = input.read(buf)) != -1 && processor.processBytes(buf, 0, read)) {
        }
        return processor.getResult();
    }

    public static int read(InputStream in, byte[] b, int off, int len) throws IOException {
        int total;
        int result;
        Preconditions.checkNotNull(in);
        Preconditions.checkNotNull(b);
        if (len < 0) {
            throw new IndexOutOfBoundsException("len is negative");
        }
        for (total = 0; total < len && (result = in.read(b, off + total, len - total)) != -1; total += result) {
        }
        return total;
    }

    private static final class LimitedInputStream
    extends FilterInputStream {
        private long left;
        private long mark = -1L;

        LimitedInputStream(InputStream in, long limit) {
            super(in);
            Preconditions.checkNotNull(in);
            Preconditions.checkArgument(limit >= 0L, "limit must be non-negative");
            this.left = limit;
        }

        @Override
        public int available() throws IOException {
            return (int)Math.min((long)this.in.available(), this.left);
        }

        @Override
        public synchronized void mark(int readLimit) {
            this.in.mark(readLimit);
            this.mark = this.left;
        }

        @Override
        public int read() throws IOException {
            if (this.left == 0L) {
                return -1;
            }
            int result = this.in.read();
            if (result != -1) {
                --this.left;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (this.left == 0L) {
                return -1;
            }
            int result = this.in.read(b, off, len = (int)Math.min((long)len, this.left));
            if (result != -1) {
                this.left -= (long)result;
            }
            return result;
        }

        @Override
        public synchronized void reset() throws IOException {
            if (!this.in.markSupported()) {
                throw new IOException("Mark not supported");
            }
            if (this.mark == -1L) {
                throw new IOException("Mark not set");
            }
            this.in.reset();
            this.left = this.mark;
        }

        @Override
        public long skip(long n) throws IOException {
            n = Math.min(n, this.left);
            long skipped = this.in.skip(n);
            this.left -= skipped;
            return skipped;
        }
    }

    private static class ByteArrayDataOutputStream
    implements ByteArrayDataOutput {
        final DataOutput output;
        final ByteArrayOutputStream byteArrayOutputSteam;

        ByteArrayDataOutputStream(ByteArrayOutputStream byteArrayOutputSteam) {
            this.byteArrayOutputSteam = byteArrayOutputSteam;
            this.output = new DataOutputStream(byteArrayOutputSteam);
        }

        @Override
        public void write(int b) {
            try {
                this.output.write(b);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void write(byte[] b) {
            try {
                this.output.write(b);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) {
            try {
                this.output.write(b, off, len);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeBoolean(boolean v) {
            try {
                this.output.writeBoolean(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeByte(int v) {
            try {
                this.output.writeByte(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeBytes(String s) {
            try {
                this.output.writeBytes(s);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeChar(int v) {
            try {
                this.output.writeChar(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeChars(String s) {
            try {
                this.output.writeChars(s);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeDouble(double v) {
            try {
                this.output.writeDouble(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeFloat(float v) {
            try {
                this.output.writeFloat(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeInt(int v) {
            try {
                this.output.writeInt(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeLong(long v) {
            try {
                this.output.writeLong(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeShort(int v) {
            try {
                this.output.writeShort(v);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public void writeUTF(String s) {
            try {
                this.output.writeUTF(s);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public byte[] toByteArray() {
            return this.byteArrayOutputSteam.toByteArray();
        }
    }

    private static class ByteArrayDataInputStream
    implements ByteArrayDataInput {
        final DataInput input;

        ByteArrayDataInputStream(ByteArrayInputStream byteArrayInputStream) {
            this.input = new DataInputStream(byteArrayInputStream);
        }

        @Override
        public void readFully(byte[] b) {
            try {
                this.input.readFully(b);
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void readFully(byte[] b, int off, int len) {
            try {
                this.input.readFully(b, off, len);
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public int skipBytes(int n) {
            try {
                return this.input.skipBytes(n);
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public boolean readBoolean() {
            try {
                return this.input.readBoolean();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public byte readByte() {
            try {
                return this.input.readByte();
            }
            catch (EOFException e) {
                throw new IllegalStateException(e);
            }
            catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        @Override
        public int readUnsignedByte() {
            try {
                return this.input.readUnsignedByte();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public short readShort() {
            try {
                return this.input.readShort();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public int readUnsignedShort() {
            try {
                return this.input.readUnsignedShort();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public char readChar() {
            try {
                return this.input.readChar();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public int readInt() {
            try {
                return this.input.readInt();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public long readLong() {
            try {
                return this.input.readLong();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public float readFloat() {
            try {
                return this.input.readFloat();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public double readDouble() {
            try {
                return this.input.readDouble();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String readLine() {
            try {
                return this.input.readLine();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String readUTF() {
            try {
                return this.input.readUTF();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static final class FastByteArrayOutputStream
    extends ByteArrayOutputStream {
        private FastByteArrayOutputStream() {
        }

        void writeTo(byte[] b, int off) {
            System.arraycopy(this.buf, 0, b, off, this.count);
        }
    }

}

