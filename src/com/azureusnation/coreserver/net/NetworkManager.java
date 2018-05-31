/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net;

import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.pipeline.BlankHandler;
import com.azureusnation.coreserver.net.pipeline.FramingHandler;
import com.azureusnation.coreserver.net.pipeline.PacketCodecHandler;
import com.azureusnation.coreserver.net.pipeline.PacketHandler;
import com.azureusnation.coreserver.net.state.State;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class NetworkManager {
    private List<PlayerConnection> playerConnections = new LinkedList<PlayerConnection>();
    private static NetworkManager instance;
    private ServerBootstrap bootstrap;

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void tick() {
        for (PlayerConnection connection : this.playerConnections) {
            ServerBoundPacket packet;
            while ((packet = connection.getPacketQueue().poll()) != null) {
                connection.receivePacket(packet);
            }
            connection.tick();
        }
    }

    public List<PlayerConnection> getPlayerConnections() {
        return this.playerConnections;
    }

    public void init() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap();
        ((ServerBootstrap)((ServerBootstrap)this.bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)).childHandler(new ChannelInitializer<SocketChannel>(){

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                PlayerConnection pc = new PlayerConnection(ch);
                NetworkManager.this.playerConnections.add(pc);
                ch.pipeline().addLast("encryption", (ChannelHandler)new BlankHandler()).addLast("framing", (ChannelHandler)new FramingHandler()).addLast("compression", (ChannelHandler)new BlankHandler()).addLast("packetCodec", (ChannelHandler)new PacketCodecHandler(pc)).addLast("handler", (ChannelHandler)new PacketHandler(pc));
                ch.closeFuture().addListener(future -> {
                    pc.getConnectionState().disconnect();
                    NetworkManager.getInstance().getPlayerConnections().remove(pc);
                    System.out.println("Client disconnected");
                });
            }
        }).option(ChannelOption.SO_BACKLOG, 128)).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    public void shutdown() {
        this.bootstrap.group().shutdownGracefully();
        this.bootstrap.childGroup().shutdownGracefully();
    }

    public void listen(int port) {
        this.bootstrap.bind(port);
        System.out.println("Bound to port " + port);
    }

}

