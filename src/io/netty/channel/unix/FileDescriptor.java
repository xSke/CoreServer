/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.unix;

import io.netty.channel.epoll.Native;
import io.netty.util.internal.ObjectUtil;
import java.io.File;
import java.io.IOException;

public class FileDescriptor {
    private final int fd;
    private volatile boolean open = true;

    public FileDescriptor(int fd) {
        if (fd < 0) {
            throw new IllegalArgumentException("fd must be >= 0");
        }
        this.fd = fd;
    }

    public int intValue() {
        return this.fd;
    }

    public void close() throws IOException {
        this.open = false;
        FileDescriptor.close(this.fd);
    }

    public boolean isOpen() {
        return this.open;
    }

    public String toString() {
        return "FileDescriptor{fd=" + this.fd + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileDescriptor)) {
            return false;
        }
        return this.fd == ((FileDescriptor)o).fd;
    }

    public int hashCode() {
        return this.fd;
    }

    private static native int close(int var0);

    public static FileDescriptor from(String path) throws IOException {
        ObjectUtil.checkNotNull(path, "path");
        int res = FileDescriptor.open(path);
        if (res < 0) {
            throw Native.newIOException("open", res);
        }
        return new FileDescriptor(res);
    }

    public static FileDescriptor from(File file) throws IOException {
        return FileDescriptor.from(ObjectUtil.checkNotNull(file, "file").getPath());
    }

    private static native int open(String var0);
}

