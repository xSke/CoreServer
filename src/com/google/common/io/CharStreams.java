/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.io;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.io.AppendableWriter;
import com.google.common.io.LineProcessor;
import com.google.common.io.LineReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

@Beta
public final class CharStreams {
    private static final int BUF_SIZE = 2048;

    private CharStreams() {
    }

    public static long copy(Readable from, Appendable to) throws IOException {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        CharBuffer buf = CharBuffer.allocate(2048);
        long total = 0L;
        while (from.read(buf) != -1) {
            buf.flip();
            to.append(buf);
            total += (long)buf.remaining();
            buf.clear();
        }
        return total;
    }

    public static String toString(Readable r) throws IOException {
        return CharStreams.toStringBuilder(r).toString();
    }

    private static StringBuilder toStringBuilder(Readable r) throws IOException {
        StringBuilder sb = new StringBuilder();
        CharStreams.copy(r, sb);
        return sb;
    }

    public static List<String> readLines(Readable r) throws IOException {
        String line;
        ArrayList<String> result = new ArrayList<String>();
        LineReader lineReader = new LineReader(r);
        while ((line = lineReader.readLine()) != null) {
            result.add(line);
        }
        return result;
    }

    public static <T> T readLines(Readable readable, LineProcessor<T> processor) throws IOException {
        String line;
        Preconditions.checkNotNull(readable);
        Preconditions.checkNotNull(processor);
        LineReader lineReader = new LineReader(readable);
        while ((line = lineReader.readLine()) != null && processor.processLine(line)) {
        }
        return processor.getResult();
    }

    public static void skipFully(Reader reader, long n) throws IOException {
        Preconditions.checkNotNull(reader);
        while (n > 0L) {
            long amt = reader.skip(n);
            if (amt == 0L) {
                if (reader.read() == -1) {
                    throw new EOFException();
                }
                --n;
                continue;
            }
            n -= amt;
        }
    }

    public static Writer nullWriter() {
        return INSTANCE;
    }

    public static Writer asWriter(Appendable target) {
        if (target instanceof Writer) {
            return (Writer)target;
        }
        return new AppendableWriter(target);
    }

    static Reader asReader(final Readable readable) {
        Preconditions.checkNotNull(readable);
        if (readable instanceof Reader) {
            return (Reader)readable;
        }
        return new Reader(){

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return this.read(CharBuffer.wrap(cbuf, off, len));
            }

            @Override
            public int read(CharBuffer target) throws IOException {
                return readable.read(target);
            }

            @Override
            public void close() throws IOException {
                if (readable instanceof Closeable) {
                    ((Closeable)((Object)readable)).close();
                }
            }
        };
    }

    private static final class NullWriter
    extends Writer {
        private static final NullWriter INSTANCE = new NullWriter();

        private NullWriter() {
        }

        @Override
        public void write(int c) {
        }

        @Override
        public void write(char[] cbuf) {
            Preconditions.checkNotNull(cbuf);
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            Preconditions.checkPositionIndexes(off, off + len, cbuf.length);
        }

        @Override
        public void write(String str) {
            Preconditions.checkNotNull(str);
        }

        @Override
        public void write(String str, int off, int len) {
            Preconditions.checkPositionIndexes(off, off + len, str.length());
        }

        @Override
        public Writer append(CharSequence csq) {
            Preconditions.checkNotNull(csq);
            return this;
        }

        @Override
        public Writer append(CharSequence csq, int start, int end) {
            Preconditions.checkPositionIndexes(start, end, csq.length());
            return this;
        }

        @Override
        public Writer append(char c) {
            return this;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        public String toString() {
            return "CharStreams.nullWriter()";
        }
    }

}

