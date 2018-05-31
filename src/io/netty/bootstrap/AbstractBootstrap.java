/*
 * Decompiled with CFR 0_129.
 */
package io.netty.bootstrap;

import io.netty.bootstrap.ChannelFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.AttributeKey;
import io.netty.util.UniqueName;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.StringUtil;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel>
implements Cloneable {
    volatile EventLoopGroup group;
    private volatile ChannelFactory<? extends C> channelFactory;
    private volatile SocketAddress localAddress;
    private final Map<ChannelOption<?>, Object> options = new LinkedHashMap();
    private final Map<AttributeKey<?>, Object> attrs = new LinkedHashMap();
    private volatile ChannelHandler handler;

    AbstractBootstrap() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    AbstractBootstrap(AbstractBootstrap<B, C> bootstrap) {
        this.group = bootstrap.group;
        this.channelFactory = bootstrap.channelFactory;
        this.handler = bootstrap.handler;
        this.localAddress = bootstrap.localAddress;
        Map<UniqueName, Object> map = bootstrap.options;
        synchronized (map) {
            this.options.putAll(bootstrap.options);
        }
        map = bootstrap.attrs;
        synchronized (map) {
            this.attrs.putAll(bootstrap.attrs);
        }
    }

    public B group(EventLoopGroup group) {
        if (group == null) {
            throw new NullPointerException("group");
        }
        if (this.group != null) {
            throw new IllegalStateException("group set already");
        }
        this.group = group;
        return (B)this;
    }

    public B channel(Class<? extends C> channelClass) {
        if (channelClass == null) {
            throw new NullPointerException("channelClass");
        }
        return this.channelFactory(new BootstrapChannelFactory<C>(channelClass));
    }

    public B channelFactory(ChannelFactory<? extends C> channelFactory) {
        if (channelFactory == null) {
            throw new NullPointerException("channelFactory");
        }
        if (this.channelFactory != null) {
            throw new IllegalStateException("channelFactory set already");
        }
        this.channelFactory = channelFactory;
        return (B)this;
    }

    public B localAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
        return (B)this;
    }

    public B localAddress(int inetPort) {
        return this.localAddress(new InetSocketAddress(inetPort));
    }

    public B localAddress(String inetHost, int inetPort) {
        return this.localAddress(new InetSocketAddress(inetHost, inetPort));
    }

    public B localAddress(InetAddress inetHost, int inetPort) {
        return this.localAddress(new InetSocketAddress(inetHost, inetPort));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T> B option(ChannelOption<T> option, T value) {
        if (option == null) {
            throw new NullPointerException("option");
        }
        if (value == null) {
            Map map = this.options;
            synchronized (map) {
                this.options.remove(option);
            }
        }
        Map map = this.options;
        synchronized (map) {
            this.options.put(option, value);
        }
        return (B)this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T> B attr(AttributeKey<T> key, T value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (value == null) {
            Map map = this.attrs;
            synchronized (map) {
                this.attrs.remove(key);
            }
        }
        Map map = this.attrs;
        synchronized (map) {
            this.attrs.put(key, value);
        }
        return (B)this;
    }

    public B validate() {
        if (this.group == null) {
            throw new IllegalStateException("group not set");
        }
        if (this.channelFactory == null) {
            throw new IllegalStateException("channel or channelFactory not set");
        }
        return (B)this;
    }

    public abstract B clone();

    public ChannelFuture register() {
        this.validate();
        return this.initAndRegister();
    }

    public ChannelFuture bind() {
        this.validate();
        SocketAddress localAddress = this.localAddress;
        if (localAddress == null) {
            throw new IllegalStateException("localAddress not set");
        }
        return this.doBind(localAddress);
    }

    public ChannelFuture bind(int inetPort) {
        return this.bind(new InetSocketAddress(inetPort));
    }

    public ChannelFuture bind(String inetHost, int inetPort) {
        return this.bind(new InetSocketAddress(inetHost, inetPort));
    }

    public ChannelFuture bind(InetAddress inetHost, int inetPort) {
        return this.bind(new InetSocketAddress(inetHost, inetPort));
    }

    public ChannelFuture bind(SocketAddress localAddress) {
        this.validate();
        if (localAddress == null) {
            throw new NullPointerException("localAddress");
        }
        return this.doBind(localAddress);
    }

    private ChannelFuture doBind(final SocketAddress localAddress) {
        final ChannelFuture regFuture = this.initAndRegister();
        final Channel channel = regFuture.channel();
        if (regFuture.cause() != null) {
            return regFuture;
        }
        if (regFuture.isDone()) {
            ChannelPromise promise = channel.newPromise();
            AbstractBootstrap.doBind0(regFuture, channel, localAddress, promise);
            return promise;
        }
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    promise.setFailure(cause);
                } else {
                    promise.executor = channel.eventLoop();
                }
                AbstractBootstrap.doBind0(regFuture, channel, localAddress, promise);
            }
        });
        return promise;
    }

    final ChannelFuture initAndRegister() {
        C channel = this.channelFactory().newChannel();
        try {
            this.init((Channel)channel);
        }
        catch (Throwable t) {
            channel.unsafe().closeForcibly();
            return new DefaultChannelPromise((Channel)channel, GlobalEventExecutor.INSTANCE).setFailure(t);
        }
        ChannelFuture regFuture = this.group().register((Channel)channel);
        if (regFuture.cause() != null) {
            if (channel.isRegistered()) {
                channel.close();
            } else {
                channel.unsafe().closeForcibly();
            }
        }
        return regFuture;
    }

    abstract void init(Channel var1) throws Exception;

    private static void doBind0(final ChannelFuture regFuture, final Channel channel, final SocketAddress localAddress, final ChannelPromise promise) {
        channel.eventLoop().execute(new Runnable(){

            @Override
            public void run() {
                if (regFuture.isSuccess()) {
                    channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    promise.setFailure(regFuture.cause());
                }
            }
        });
    }

    public B handler(ChannelHandler handler) {
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        this.handler = handler;
        return (B)this;
    }

    final SocketAddress localAddress() {
        return this.localAddress;
    }

    final ChannelFactory<? extends C> channelFactory() {
        return this.channelFactory;
    }

    final ChannelHandler handler() {
        return this.handler;
    }

    public EventLoopGroup group() {
        return this.group;
    }

    final Map<ChannelOption<?>, Object> options() {
        return this.options;
    }

    final Map<AttributeKey<?>, Object> attrs() {
        return this.attrs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder().append(StringUtil.simpleClassName(this)).append('(');
        if (this.group != null) {
            buf.append("group: ").append(StringUtil.simpleClassName(this.group)).append(", ");
        }
        if (this.channelFactory != null) {
            buf.append("channelFactory: ").append(this.channelFactory).append(", ");
        }
        if (this.localAddress != null) {
            buf.append("localAddress: ").append(this.localAddress).append(", ");
        }
        Map<UniqueName, Object> map = this.options;
        synchronized (map) {
            if (!this.options.isEmpty()) {
                buf.append("options: ").append(this.options).append(", ");
            }
        }
        map = this.attrs;
        synchronized (map) {
            if (!this.attrs.isEmpty()) {
                buf.append("attrs: ").append(this.attrs).append(", ");
            }
        }
        if (this.handler != null) {
            buf.append("handler: ").append(this.handler).append(", ");
        }
        if (buf.charAt(buf.length() - 1) == '(') {
            buf.append(')');
        } else {
            buf.setCharAt(buf.length() - 2, ')');
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    private static final class PendingRegistrationPromise
    extends DefaultChannelPromise {
        private volatile EventExecutor executor;

        private PendingRegistrationPromise(Channel channel) {
            super(channel);
        }

        @Override
        protected EventExecutor executor() {
            EventExecutor executor = this.executor;
            if (executor != null) {
                return executor;
            }
            return GlobalEventExecutor.INSTANCE;
        }
    }

    private static final class BootstrapChannelFactory<T extends Channel>
    implements ChannelFactory<T> {
        private final Class<? extends T> clazz;

        BootstrapChannelFactory(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T newChannel() {
            try {
                return (T)((Channel)this.clazz.newInstance());
            }
            catch (Throwable t) {
                throw new ChannelException("Unable to create Channel from class " + this.clazz, t);
            }
        }

        public String toString() {
            return StringUtil.simpleClassName(this.clazz) + ".class";
        }
    }

}

