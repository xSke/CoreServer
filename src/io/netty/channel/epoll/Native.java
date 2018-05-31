/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.channel.ChannelException;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventArray;
import io.netty.channel.epoll.EpollTcpInfo;
import io.netty.channel.epoll.NativeDatagramPacketArray;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.NativeLibraryLoader;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Locale;

public final class Native {
    public static final int EPOLLIN;
    public static final int EPOLLOUT;
    public static final int EPOLLRDHUP;
    public static final int EPOLLET;
    public static final int EPOLLERR;
    public static final int IOV_MAX;
    public static final int UIO_MAX_IOV;
    public static final boolean IS_SUPPORTING_SENDMMSG;
    public static final long SSIZE_MAX;
    private static final byte[] IPV4_MAPPED_IPV6_PREFIX;
    private static final int ERRNO_EBADF_NEGATIVE;
    private static final int ERRNO_EPIPE_NEGATIVE;
    private static final int ERRNO_ECONNRESET_NEGATIVE;
    private static final int ERRNO_EAGAIN_NEGATIVE;
    private static final int ERRNO_EWOULDBLOCK_NEGATIVE;
    private static final int ERRNO_EINPROGRESS_NEGATIVE;
    private static final String[] ERRORS;
    private static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION;
    private static final IOException CONNECTION_RESET_EXCEPTION_WRITE;
    private static final IOException CONNECTION_RESET_EXCEPTION_WRITEV;
    private static final IOException CONNECTION_RESET_EXCEPTION_READ;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDFILE;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDTO;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDMSG;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDMMSG;
    private static final IOException CONNECTION_RESET_EXCEPTION_SPLICE;

    private static IOException newConnectionResetException(String method, int errnoNegative) {
        IOException exception = Native.newIOException(method, errnoNegative);
        exception.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        return exception;
    }

    public static IOException newIOException(String method, int err) {
        return new IOException(method + "() failed: " + ERRORS[- err]);
    }

    private static int ioResult(String method, int err, IOException resetCause) throws IOException {
        if (err == ERRNO_EAGAIN_NEGATIVE || err == ERRNO_EWOULDBLOCK_NEGATIVE) {
            return 0;
        }
        if (err == ERRNO_EPIPE_NEGATIVE || err == ERRNO_ECONNRESET_NEGATIVE) {
            throw resetCause;
        }
        if (err == ERRNO_EBADF_NEGATIVE) {
            throw CLOSED_CHANNEL_EXCEPTION;
        }
        throw Native.newIOException(method, err);
    }

    public static native int eventFd();

    public static native void eventFdWrite(int var0, long var1);

    public static native void eventFdRead(int var0);

    public static native int epollCreate();

    public static int epollWait(int efd, EpollEventArray events, int timeout) throws IOException {
        int ready = Native.epollWait0(efd, events.memoryAddress(), events.length(), timeout);
        if (ready < 0) {
            throw Native.newIOException("epoll_wait", ready);
        }
        return ready;
    }

    private static native int epollWait0(int var0, long var1, int var3, int var4);

    public static void epollCtlAdd(int efd, int fd, int flags) throws IOException {
        int res = Native.epollCtlAdd0(efd, fd, flags);
        if (res < 0) {
            throw Native.newIOException("epoll_ctl", res);
        }
    }

    private static native int epollCtlAdd0(int var0, int var1, int var2);

    public static void epollCtlMod(int efd, int fd, int flags) throws IOException {
        int res = Native.epollCtlMod0(efd, fd, flags);
        if (res < 0) {
            throw Native.newIOException("epoll_ctl", res);
        }
    }

    private static native int epollCtlMod0(int var0, int var1, int var2);

    public static void epollCtlDel(int efd, int fd) throws IOException {
        int res = Native.epollCtlDel0(efd, fd);
        if (res < 0) {
            throw Native.newIOException("epoll_ctl", res);
        }
    }

    private static native int epollCtlDel0(int var0, int var1);

    private static native int errnoEBADF();

    private static native int errnoEPIPE();

    private static native int errnoECONNRESET();

    private static native int errnoEAGAIN();

    private static native int errnoEWOULDBLOCK();

    private static native int errnoEINPROGRESS();

    private static native String strError(int var0);

    public static void close(int fd) throws IOException {
        int res = Native.close0(fd);
        if (res < 0) {
            throw Native.newIOException("close", res);
        }
    }

    private static native int close0(int var0);

    public static int splice(int fd, int offIn, int fdOut, int offOut, int len) throws IOException {
        int res = Native.splice0(fd, offIn, fdOut, offOut, len);
        if (res >= 0) {
            return res;
        }
        return Native.ioResult("splice", res, CONNECTION_RESET_EXCEPTION_SPLICE);
    }

    private static native int splice0(int var0, int var1, int var2, int var3, int var4);

    public static long pipe() throws IOException {
        long res = Native.pipe0();
        if (res >= 0L) {
            return res;
        }
        throw Native.newIOException("pipe", (int)res);
    }

    private static native long pipe0();

    public static int write(int fd, ByteBuffer buf, int pos, int limit) throws IOException {
        int res = Native.write0(fd, buf, pos, limit);
        if (res >= 0) {
            return res;
        }
        return Native.ioResult("write", res, CONNECTION_RESET_EXCEPTION_WRITE);
    }

    private static native int write0(int var0, ByteBuffer var1, int var2, int var3);

    public static int writeAddress(int fd, long address, int pos, int limit) throws IOException {
        int res = Native.writeAddress0(fd, address, pos, limit);
        if (res >= 0) {
            return res;
        }
        return Native.ioResult("writeAddress", res, CONNECTION_RESET_EXCEPTION_WRITE);
    }

    private static native int writeAddress0(int var0, long var1, int var3, int var4);

    public static long writev(int fd, ByteBuffer[] buffers, int offset, int length) throws IOException {
        long res = Native.writev0(fd, buffers, offset, length);
        if (res >= 0L) {
            return res;
        }
        return Native.ioResult("writev", (int)res, CONNECTION_RESET_EXCEPTION_WRITEV);
    }

    private static native long writev0(int var0, ByteBuffer[] var1, int var2, int var3);

    public static long writevAddresses(int fd, long memoryAddress, int length) throws IOException {
        long res = Native.writevAddresses0(fd, memoryAddress, length);
        if (res >= 0L) {
            return res;
        }
        return Native.ioResult("writevAddresses", (int)res, CONNECTION_RESET_EXCEPTION_WRITEV);
    }

    private static native long writevAddresses0(int var0, long var1, int var3);

    public static int read(int fd, ByteBuffer buf, int pos, int limit) throws IOException {
        int res = Native.read0(fd, buf, pos, limit);
        if (res > 0) {
            return res;
        }
        if (res == 0) {
            return -1;
        }
        return Native.ioResult("read", res, CONNECTION_RESET_EXCEPTION_READ);
    }

    private static native int read0(int var0, ByteBuffer var1, int var2, int var3);

    public static int readAddress(int fd, long address, int pos, int limit) throws IOException {
        int res = Native.readAddress0(fd, address, pos, limit);
        if (res > 0) {
            return res;
        }
        if (res == 0) {
            return -1;
        }
        return Native.ioResult("readAddress", res, CONNECTION_RESET_EXCEPTION_READ);
    }

    private static native int readAddress0(int var0, long var1, int var3, int var4);

    public static long sendfile(int dest, DefaultFileRegion src, long baseOffset, long offset, long length) throws IOException {
        src.open();
        long res = Native.sendfile0(dest, src, baseOffset, offset, length);
        if (res >= 0L) {
            return res;
        }
        return Native.ioResult("sendfile", (int)res, CONNECTION_RESET_EXCEPTION_SENDFILE);
    }

    private static native long sendfile0(int var0, DefaultFileRegion var1, long var2, long var4, long var6) throws IOException;

    public static int sendTo(int fd, ByteBuffer buf, int pos, int limit, InetAddress addr, int port) throws IOException {
        byte[] address;
        int scopeId;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        } else {
            scopeId = 0;
            address = Native.ipv4MappedIpv6Address(addr.getAddress());
        }
        int res = Native.sendTo0(fd, buf, pos, limit, address, scopeId, port);
        if (res >= 0) {
            return res;
        }
        return Native.ioResult("sendTo", res, CONNECTION_RESET_EXCEPTION_SENDTO);
    }

    private static native int sendTo0(int var0, ByteBuffer var1, int var2, int var3, byte[] var4, int var5, int var6);

    public static int sendToAddress(int fd, long memoryAddress, int pos, int limit, InetAddress addr, int port) throws IOException {
        int scopeId;
        byte[] address;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        } else {
            scopeId = 0;
            address = Native.ipv4MappedIpv6Address(addr.getAddress());
        }
        int res = Native.sendToAddress0(fd, memoryAddress, pos, limit, address, scopeId, port);
        if (res >= 0) {
            return res;
        }
        return Native.ioResult("sendToAddress", res, CONNECTION_RESET_EXCEPTION_SENDTO);
    }

    private static native int sendToAddress0(int var0, long var1, int var3, int var4, byte[] var5, int var6, int var7);

    public static int sendToAddresses(int fd, long memoryAddress, int length, InetAddress addr, int port) throws IOException {
        byte[] address;
        int scopeId;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        } else {
            scopeId = 0;
            address = Native.ipv4MappedIpv6Address(addr.getAddress());
        }
        int res = Native.sendToAddresses(fd, memoryAddress, length, address, scopeId, port);
        if (res >= 0) {
            return res;
        }
        return Native.ioResult("sendToAddresses", res, CONNECTION_RESET_EXCEPTION_SENDMSG);
    }

    private static native int sendToAddresses(int var0, long var1, int var3, byte[] var4, int var5, int var6);

    public static native EpollDatagramChannel.DatagramSocketAddress recvFrom(int var0, ByteBuffer var1, int var2, int var3) throws IOException;

    public static native EpollDatagramChannel.DatagramSocketAddress recvFromAddress(int var0, long var1, int var3, int var4) throws IOException;

    public static int sendmmsg(int fd, NativeDatagramPacketArray.NativeDatagramPacket[] msgs, int offset, int len) throws IOException {
        int res = Native.sendmmsg0(fd, msgs, offset, len);
        if (res >= 0) {
            return res;
        }
        return Native.ioResult("sendmmsg", res, CONNECTION_RESET_EXCEPTION_SENDMMSG);
    }

    private static native int sendmmsg0(int var0, NativeDatagramPacketArray.NativeDatagramPacket[] var1, int var2, int var3);

    private static native boolean isSupportingSendmmsg();

    public static int socketStreamFd() {
        int res = Native.socketStream();
        if (res < 0) {
            throw new ChannelException(Native.newIOException("socketStreamFd", res));
        }
        return res;
    }

    public static int socketDgramFd() {
        int res = Native.socketDgram();
        if (res < 0) {
            throw new ChannelException(Native.newIOException("socketDgramFd", res));
        }
        return res;
    }

    public static int socketDomainFd() {
        int res = Native.socketDomain();
        if (res < 0) {
            throw new ChannelException(Native.newIOException("socketDomain", res));
        }
        return res;
    }

    private static native int socketStream();

    private static native int socketDgram();

    private static native int socketDomain();

    public static void bind(int fd, SocketAddress socketAddress) throws IOException {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress)socketAddress;
            NativeInetAddress address = Native.toNativeInetAddress(addr.getAddress());
            int res = Native.bind(fd, address.address, address.scopeId, addr.getPort());
            if (res < 0) {
                throw Native.newIOException("bind", res);
            }
        } else if (socketAddress instanceof DomainSocketAddress) {
            DomainSocketAddress addr = (DomainSocketAddress)socketAddress;
            int res = Native.bindDomainSocket(fd, addr.path());
            if (res < 0) {
                throw Native.newIOException("bind", res);
            }
        } else {
            throw new Error("Unexpected SocketAddress implementation " + socketAddress);
        }
    }

    private static native int bind(int var0, byte[] var1, int var2, int var3);

    private static native int bindDomainSocket(int var0, String var1);

    public static void listen(int fd, int backlog) throws IOException {
        int res = Native.listen0(fd, backlog);
        if (res < 0) {
            throw Native.newIOException("listen", res);
        }
    }

    private static native int listen0(int var0, int var1);

    public static boolean connect(int fd, SocketAddress socketAddress) throws IOException {
        int res;
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
            NativeInetAddress address = Native.toNativeInetAddress(inetSocketAddress.getAddress());
            res = Native.connect(fd, address.address, address.scopeId, inetSocketAddress.getPort());
        } else if (socketAddress instanceof DomainSocketAddress) {
            DomainSocketAddress unixDomainSocketAddress = (DomainSocketAddress)socketAddress;
            res = Native.connectDomainSocket(fd, unixDomainSocketAddress.path());
        } else {
            throw new Error("Unexpected SocketAddress implementation " + socketAddress);
        }
        if (res < 0) {
            if (res == ERRNO_EINPROGRESS_NEGATIVE) {
                return false;
            }
            throw Native.newConnectException("connect", res);
        }
        return true;
    }

    private static native int connect(int var0, byte[] var1, int var2, int var3);

    private static native int connectDomainSocket(int var0, String var1);

    public static boolean finishConnect(int fd) throws IOException {
        int res = Native.finishConnect0(fd);
        if (res < 0) {
            if (res == ERRNO_EINPROGRESS_NEGATIVE) {
                return false;
            }
            throw Native.newConnectException("finishConnect", res);
        }
        return true;
    }

    private static native int finishConnect0(int var0);

    private static ConnectException newConnectException(String method, int err) {
        return new ConnectException(method + "() failed: " + ERRORS[- err]);
    }

    public static InetSocketAddress remoteAddress(int fd) {
        byte[] addr = Native.remoteAddress0(fd);
        if (addr == null) {
            return null;
        }
        return Native.address(addr, 0, addr.length);
    }

    public static InetSocketAddress localAddress(int fd) {
        byte[] addr = Native.localAddress0(fd);
        if (addr == null) {
            return null;
        }
        return Native.address(addr, 0, addr.length);
    }

    static InetSocketAddress address(byte[] addr, int offset, int len) {
        int port = Native.decodeInt(addr, offset + len - 4);
        try {
            InetAddress address;
            switch (len) {
                case 8: {
                    byte[] ipv4 = new byte[4];
                    System.arraycopy(addr, offset, ipv4, 0, 4);
                    address = InetAddress.getByAddress(ipv4);
                    break;
                }
                case 24: {
                    byte[] ipv6 = new byte[16];
                    System.arraycopy(addr, offset, ipv6, 0, 16);
                    int scopeId = Native.decodeInt(addr, offset + len - 8);
                    address = Inet6Address.getByAddress(null, ipv6, scopeId);
                    break;
                }
                default: {
                    throw new Error();
                }
            }
            return new InetSocketAddress(address, port);
        }
        catch (UnknownHostException e) {
            throw new Error("Should never happen", e);
        }
    }

    static int decodeInt(byte[] addr, int index) {
        return (addr[index] & 255) << 24 | (addr[index + 1] & 255) << 16 | (addr[index + 2] & 255) << 8 | addr[index + 3] & 255;
    }

    private static native byte[] remoteAddress0(int var0);

    private static native byte[] localAddress0(int var0);

    public static int accept(int fd, byte[] addr) throws IOException {
        int res = Native.accept0(fd, addr);
        if (res >= 0) {
            return res;
        }
        if (res == ERRNO_EAGAIN_NEGATIVE || res == ERRNO_EWOULDBLOCK_NEGATIVE) {
            return -1;
        }
        throw Native.newIOException("accept", res);
    }

    private static native int accept0(int var0, byte[] var1);

    public static int recvFd(int fd) throws IOException {
        int res = Native.recvFd0(fd);
        if (res > 0) {
            return res;
        }
        if (res == 0) {
            return -1;
        }
        if (res == ERRNO_EAGAIN_NEGATIVE || res == ERRNO_EWOULDBLOCK_NEGATIVE) {
            return 0;
        }
        throw Native.newIOException("recvFd", res);
    }

    private static native int recvFd0(int var0);

    public static int sendFd(int socketFd, int fd) throws IOException {
        int res = Native.sendFd0(socketFd, fd);
        if (res >= 0) {
            return res;
        }
        if (res == ERRNO_EAGAIN_NEGATIVE || res == ERRNO_EWOULDBLOCK_NEGATIVE) {
            return -1;
        }
        throw Native.newIOException("sendFd", res);
    }

    private static native int sendFd0(int var0, int var1);

    public static void shutdown(int fd, boolean read, boolean write) throws IOException {
        int res = Native.shutdown0(fd, read, write);
        if (res < 0) {
            throw Native.newIOException("shutdown", res);
        }
    }

    private static native int shutdown0(int var0, boolean var1, boolean var2);

    public static native int getReceiveBufferSize(int var0);

    public static native int getSendBufferSize(int var0);

    public static native int isKeepAlive(int var0);

    public static native int isReuseAddress(int var0);

    public static native int isReusePort(int var0);

    public static native int isTcpNoDelay(int var0);

    public static native int isTcpCork(int var0);

    public static native int getTcpNotSentLowAt(int var0);

    public static native int getSoLinger(int var0);

    public static native int getTrafficClass(int var0);

    public static native int isBroadcast(int var0);

    public static native int getTcpKeepIdle(int var0);

    public static native int getTcpKeepIntvl(int var0);

    public static native int getTcpKeepCnt(int var0);

    public static native int getSoError(int var0);

    public static native void setKeepAlive(int var0, int var1);

    public static native void setReceiveBufferSize(int var0, int var1);

    public static native void setReuseAddress(int var0, int var1);

    public static native void setReusePort(int var0, int var1);

    public static native void setSendBufferSize(int var0, int var1);

    public static native void setTcpNoDelay(int var0, int var1);

    public static native void setTcpCork(int var0, int var1);

    public static native void setTcpNotSentLowAt(int var0, int var1);

    public static native void setSoLinger(int var0, int var1);

    public static native void setTrafficClass(int var0, int var1);

    public static native void setBroadcast(int var0, int var1);

    public static native void setTcpKeepIdle(int var0, int var1);

    public static native void setTcpKeepIntvl(int var0, int var1);

    public static native void setTcpKeepCnt(int var0, int var1);

    public static void tcpInfo(int fd, EpollTcpInfo info) {
        Native.tcpInfo0(fd, info.info);
    }

    private static native void tcpInfo0(int var0, int[] var1);

    private static NativeInetAddress toNativeInetAddress(InetAddress addr) {
        byte[] bytes = addr.getAddress();
        if (addr instanceof Inet6Address) {
            return new NativeInetAddress(bytes, ((Inet6Address)addr).getScopeId());
        }
        return new NativeInetAddress(Native.ipv4MappedIpv6Address(bytes));
    }

    static byte[] ipv4MappedIpv6Address(byte[] ipv4) {
        byte[] address = new byte[16];
        System.arraycopy(IPV4_MAPPED_IPV6_PREFIX, 0, address, 0, IPV4_MAPPED_IPV6_PREFIX.length);
        System.arraycopy(ipv4, 0, address, 12, ipv4.length);
        return address;
    }

    public static native String kernelVersion();

    private static native int iovMax();

    private static native int uioMaxIov();

    public static native int sizeofEpollEvent();

    public static native int offsetofEpollData();

    private static native int epollin();

    private static native int epollout();

    private static native int epollrdhup();

    private static native int epollet();

    private static native int epollerr();

    private static native long ssizeMax();

    private Native() {
    }

    static {
        String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
        if (!name.startsWith("linux")) {
            throw new IllegalStateException("Only supported on Linux");
        }
        NativeLibraryLoader.load("netty-transport-native-epoll", PlatformDependent.getClassLoader(Native.class));
        EPOLLIN = Native.epollin();
        EPOLLOUT = Native.epollout();
        EPOLLRDHUP = Native.epollrdhup();
        EPOLLET = Native.epollet();
        EPOLLERR = Native.epollerr();
        IOV_MAX = Native.iovMax();
        UIO_MAX_IOV = Native.uioMaxIov();
        IS_SUPPORTING_SENDMMSG = Native.isSupportingSendmmsg();
        SSIZE_MAX = Native.ssizeMax();
        IPV4_MAPPED_IPV6_PREFIX = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1};
        ERRNO_EBADF_NEGATIVE = - Native.errnoEBADF();
        ERRNO_EPIPE_NEGATIVE = - Native.errnoEPIPE();
        ERRNO_ECONNRESET_NEGATIVE = - Native.errnoECONNRESET();
        ERRNO_EAGAIN_NEGATIVE = - Native.errnoEAGAIN();
        ERRNO_EWOULDBLOCK_NEGATIVE = - Native.errnoEWOULDBLOCK();
        ERRNO_EINPROGRESS_NEGATIVE = - Native.errnoEINPROGRESS();
        ERRORS = new String[1024];
        for (int i = 0; i < ERRORS.length; ++i) {
            Native.ERRORS[i] = Native.strError(i);
        }
        CONNECTION_RESET_EXCEPTION_READ = Native.newConnectionResetException("syscall:read(...)", ERRNO_ECONNRESET_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_WRITE = Native.newConnectionResetException("syscall:write(...)", ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_WRITEV = Native.newConnectionResetException("syscall:writev(...)", ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDFILE = Native.newConnectionResetException("syscall:sendfile(...)", ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDTO = Native.newConnectionResetException("syscall:sendto(...)", ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDMSG = Native.newConnectionResetException("syscall:sendmsg(...)", ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDMMSG = Native.newConnectionResetException("syscall:sendmmsg(...)", ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SPLICE = Native.newConnectionResetException("syscall:splice(...)", ERRNO_EPIPE_NEGATIVE);
        CLOSED_CHANNEL_EXCEPTION = new ClosedChannelException();
        CLOSED_CHANNEL_EXCEPTION.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
    }

    private static class NativeInetAddress {
        final byte[] address;
        final int scopeId;

        NativeInetAddress(byte[] address, int scopeId) {
            this.address = address;
            this.scopeId = scopeId;
        }

        NativeInetAddress(byte[] address) {
            this(address, 0);
        }
    }

}

