/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.io;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Closer;
import com.google.common.io.FileWriteMode;
import com.google.common.io.LineProcessor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Beta
public final class Files {
    private static final int TEMP_DIR_ATTEMPTS = 10000;
    private static final TreeTraverser<File> FILE_TREE_TRAVERSER = new TreeTraverser<File>(){

        @Override
        public Iterable<File> children(File file) {
            File[] files;
            if (file.isDirectory() && (files = file.listFiles()) != null) {
                return Collections.unmodifiableList(Arrays.asList(files));
            }
            return Collections.emptyList();
        }

        public String toString() {
            return "Files.fileTreeTraverser()";
        }
    };

    private Files() {
    }

    public static BufferedReader newReader(File file, Charset charset) throws FileNotFoundException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(charset);
        return new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(file), charset));
    }

    public static BufferedWriter newWriter(File file, Charset charset) throws FileNotFoundException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(charset);
        return new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(file), charset));
    }

    public static ByteSource asByteSource(File file) {
        return new FileByteSource(file);
    }

    static byte[] readFile(InputStream in, long expectedSize) throws IOException {
        if (expectedSize > Integer.MAX_VALUE) {
            long l = expectedSize;
            throw new OutOfMemoryError(new StringBuilder(68).append("file is too large to fit in a byte array: ").append(l).append(" bytes").toString());
        }
        return expectedSize == 0L ? ByteStreams.toByteArray(in) : ByteStreams.toByteArray(in, (int)expectedSize);
    }

    public static /* varargs */ ByteSink asByteSink(File file, FileWriteMode ... modes) {
        return new FileByteSink(file, modes);
    }

    public static CharSource asCharSource(File file, Charset charset) {
        return Files.asByteSource(file).asCharSource(charset);
    }

    public static /* varargs */ CharSink asCharSink(File file, Charset charset, FileWriteMode ... modes) {
        return Files.asByteSink(file, modes).asCharSink(charset);
    }

    private static FileWriteMode[] modes(boolean append) {
        FileWriteMode[] arrfileWriteMode;
        if (append) {
            FileWriteMode[] arrfileWriteMode2 = new FileWriteMode[1];
            arrfileWriteMode = arrfileWriteMode2;
            arrfileWriteMode2[0] = FileWriteMode.APPEND;
        } else {
            arrfileWriteMode = new FileWriteMode[]{};
        }
        return arrfileWriteMode;
    }

    public static byte[] toByteArray(File file) throws IOException {
        return Files.asByteSource(file).read();
    }

    public static String toString(File file, Charset charset) throws IOException {
        return Files.asCharSource(file, charset).read();
    }

    public static void write(byte[] from, File to) throws IOException {
        Files.asByteSink(to, new FileWriteMode[0]).write(from);
    }

    public static void copy(File from, OutputStream to) throws IOException {
        Files.asByteSource(from).copyTo(to);
    }

    public static void copy(File from, File to) throws IOException {
        Preconditions.checkArgument(!from.equals(to), "Source %s and destination %s must be different", from, to);
        Files.asByteSource(from).copyTo(Files.asByteSink(to, new FileWriteMode[0]));
    }

    public static void write(CharSequence from, File to, Charset charset) throws IOException {
        Files.asCharSink(to, charset, new FileWriteMode[0]).write(from);
    }

    public static void append(CharSequence from, File to, Charset charset) throws IOException {
        Files.write(from, to, charset, true);
    }

    private static void write(CharSequence from, File to, Charset charset, boolean append) throws IOException {
        Files.asCharSink(to, charset, Files.modes(append)).write(from);
    }

    public static void copy(File from, Charset charset, Appendable to) throws IOException {
        Files.asCharSource(from, charset).copyTo(to);
    }

    public static boolean equal(File file1, File file2) throws IOException {
        Preconditions.checkNotNull(file1);
        Preconditions.checkNotNull(file2);
        if (file1 == file2 || file1.equals(file2)) {
            return true;
        }
        long len1 = file1.length();
        long len2 = file2.length();
        if (len1 != 0L && len2 != 0L && len1 != len2) {
            return false;
        }
        return Files.asByteSource(file1).contentEquals(Files.asByteSource(file2));
    }

    public static File createTempDir() {
        int n;
        String string;
        Object tempDir;
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        long l = System.currentTimeMillis();
        String baseName = new StringBuilder(21).append(l).append("-").toString();
        int counter22 = 0;
        while (counter22 < 10000) {
            string = String.valueOf(String.valueOf(baseName));
            n = counter22++;
            tempDir = new File(baseDir, new StringBuilder(11 + string.length()).append(string).append(n).toString());
            if (!tempDir.mkdir()) continue;
            return tempDir;
        }
        String counter22 = String.valueOf(String.valueOf("Failed to create directory within 10000 attempts (tried "));
        tempDir = String.valueOf(String.valueOf(baseName));
        string = String.valueOf(String.valueOf(baseName));
        n = 9999;
        throw new IllegalStateException(new StringBuilder(17 + counter22.length() + tempDir.length() + string.length()).append(counter22).append((String)tempDir).append("0 to ").append(string).append(n).append(")").toString());
    }

    public static void touch(File file) throws IOException {
        Preconditions.checkNotNull(file);
        if (!file.createNewFile() && !file.setLastModified(System.currentTimeMillis())) {
            String string = String.valueOf(String.valueOf(file));
            throw new IOException(new StringBuilder(38 + string.length()).append("Unable to update modification time of ").append(string).toString());
        }
    }

    public static void createParentDirs(File file) throws IOException {
        Preconditions.checkNotNull(file);
        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
            return;
        }
        parent.mkdirs();
        if (!parent.isDirectory()) {
            String string = String.valueOf(String.valueOf(file));
            throw new IOException(new StringBuilder(39 + string.length()).append("Unable to create parent directories of ").append(string).toString());
        }
    }

    public static void move(File from, File to) throws IOException {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkArgument(!from.equals(to), "Source %s and destination %s must be different", from, to);
        if (!from.renameTo(to)) {
            Files.copy(from, to);
            if (!from.delete()) {
                if (!to.delete()) {
                    String string = String.valueOf(String.valueOf(to));
                    throw new IOException(new StringBuilder(17 + string.length()).append("Unable to delete ").append(string).toString());
                }
                String string = String.valueOf(String.valueOf(from));
                throw new IOException(new StringBuilder(17 + string.length()).append("Unable to delete ").append(string).toString());
            }
        }
    }

    public static String readFirstLine(File file, Charset charset) throws IOException {
        return Files.asCharSource(file, charset).readFirstLine();
    }

    public static List<String> readLines(File file, Charset charset) throws IOException {
        return (List)Files.readLines(file, charset, new LineProcessor<List<String>>(){
            final List<String> result = Lists.newArrayList();

            @Override
            public boolean processLine(String line) {
                this.result.add(line);
                return true;
            }

            @Override
            public List<String> getResult() {
                return this.result;
            }
        });
    }

    public static <T> T readLines(File file, Charset charset, LineProcessor<T> callback) throws IOException {
        return Files.asCharSource(file, charset).readLines(callback);
    }

    public static <T> T readBytes(File file, ByteProcessor<T> processor) throws IOException {
        return Files.asByteSource(file).read(processor);
    }

    public static HashCode hash(File file, HashFunction hashFunction) throws IOException {
        return Files.asByteSource(file).hash(hashFunction);
    }

    public static MappedByteBuffer map(File file) throws IOException {
        Preconditions.checkNotNull(file);
        return Files.map(file, FileChannel.MapMode.READ_ONLY);
    }

    public static MappedByteBuffer map(File file, FileChannel.MapMode mode) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(mode);
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }
        return Files.map(file, mode, file.length());
    }

    public static MappedByteBuffer map(File file, FileChannel.MapMode mode, long size) throws FileNotFoundException, IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(mode);
        Closer closer = Closer.create();
        try {
            RandomAccessFile raf = closer.register(new RandomAccessFile(file, mode == FileChannel.MapMode.READ_ONLY ? "r" : "rw"));
            MappedByteBuffer mappedByteBuffer = Files.map(raf, mode, size);
            return mappedByteBuffer;
        }
        catch (Throwable e) {
            throw closer.rethrow(e);
        }
        finally {
            closer.close();
        }
    }

    private static MappedByteBuffer map(RandomAccessFile raf, FileChannel.MapMode mode, long size) throws IOException {
        Closer closer = Closer.create();
        try {
            FileChannel channel = closer.register(raf.getChannel());
            MappedByteBuffer mappedByteBuffer = channel.map(mode, 0L, size);
            return mappedByteBuffer;
        }
        catch (Throwable e) {
            throw closer.rethrow(e);
        }
        finally {
            closer.close();
        }
    }

    public static String simplifyPath(String pathname) {
        Preconditions.checkNotNull(pathname);
        if (pathname.length() == 0) {
            return ".";
        }
        Iterable<String> components = Splitter.on('/').omitEmptyStrings().split(pathname);
        ArrayList<String> path = new ArrayList<String>();
        for (String component : components) {
            if (component.equals(".")) continue;
            if (component.equals("..")) {
                if (path.size() > 0 && !((String)path.get(path.size() - 1)).equals("..")) {
                    path.remove(path.size() - 1);
                    continue;
                }
                path.add("..");
                continue;
            }
            path.add(component);
        }
        String result = Joiner.on('/').join(path);
        if (pathname.charAt(0) == '/') {
            String string = String.valueOf(result);
            String string2 = result = string.length() != 0 ? "/".concat(string) : new String("/");
        }
        while (result.startsWith("/../")) {
            result = result.substring(3);
        }
        if (result.equals("/..")) {
            result = "/";
        } else if ("".equals(result)) {
            result = ".";
        }
        return result;
    }

    public static String getFileExtension(String fullName) {
        Preconditions.checkNotNull(fullName);
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf(46);
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
    }

    public static String getNameWithoutExtension(String file) {
        Preconditions.checkNotNull(file);
        String fileName = new File(file).getName();
        int dotIndex = fileName.lastIndexOf(46);
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }

    public static TreeTraverser<File> fileTreeTraverser() {
        return FILE_TREE_TRAVERSER;
    }

    public static Predicate<File> isDirectory() {
        return FilePredicate.IS_DIRECTORY;
    }

    public static Predicate<File> isFile() {
        return FilePredicate.IS_FILE;
    }

    private static enum FilePredicate implements Predicate<File>
    {
        IS_DIRECTORY{

            @Override
            public boolean apply(File file) {
                return file.isDirectory();
            }

            public String toString() {
                return "Files.isDirectory()";
            }
        }
        ,
        IS_FILE{

            @Override
            public boolean apply(File file) {
                return file.isFile();
            }

            public String toString() {
                return "Files.isFile()";
            }
        };
        

        private FilePredicate() {
        }

    }

    private static final class FileByteSink
    extends ByteSink {
        private final File file;
        private final ImmutableSet<FileWriteMode> modes;

        private /* varargs */ FileByteSink(File file, FileWriteMode ... modes) {
            this.file = Preconditions.checkNotNull(file);
            this.modes = ImmutableSet.copyOf(modes);
        }

        @Override
        public FileOutputStream openStream() throws IOException {
            return new FileOutputStream(this.file, this.modes.contains((Object)FileWriteMode.APPEND));
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.file));
            String string2 = String.valueOf(String.valueOf(this.modes));
            return new StringBuilder(20 + string.length() + string2.length()).append("Files.asByteSink(").append(string).append(", ").append(string2).append(")").toString();
        }
    }

    private static final class FileByteSource
    extends ByteSource {
        private final File file;

        private FileByteSource(File file) {
            this.file = Preconditions.checkNotNull(file);
        }

        @Override
        public FileInputStream openStream() throws IOException {
            return new FileInputStream(this.file);
        }

        @Override
        public long size() throws IOException {
            if (!this.file.isFile()) {
                throw new FileNotFoundException(this.file.toString());
            }
            return this.file.length();
        }

        @Override
        public byte[] read() throws IOException {
            Closer closer = Closer.create();
            try {
                FileInputStream in = closer.register(this.openStream());
                byte[] arrby = Files.readFile(in, in.getChannel().size());
                return arrby;
            }
            catch (Throwable e) {
                throw closer.rethrow(e);
            }
            finally {
                closer.close();
            }
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.file));
            return new StringBuilder(20 + string.length()).append("Files.asByteSource(").append(string).append(")").toString();
        }
    }

}

