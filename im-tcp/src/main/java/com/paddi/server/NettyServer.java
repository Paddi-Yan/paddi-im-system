package com.paddi.server;

import com.paddi.codec.MessageDecoder;
import com.paddi.codec.MessageEncoder;
import com.paddi.config.BootstrapConfig;
import com.paddi.handler.HeartBeatHandler;
import com.paddi.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 10:40:28
 *
 */
@Slf4j
public class NettyServer {
    private BootstrapConfig.TcpConfig config;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap server;

    public NettyServer(BootstrapConfig.TcpConfig tcpConfig) {
        this.config = tcpConfig;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.server = new ServerBootstrap();
        this.server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                 //服务端可连接队列大小
                .option(ChannelOption.SO_BACKLOG, 1024)
                 //允许重复使用本地地址和端口
                .option(ChannelOption.SO_REUSEADDR, true)
                 //是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.TCP_NODELAY, true)
                 //保活开关2h没有数据服务端会发送心跳包
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new MessageDecoder());
                        channel.pipeline().addLast(new MessageEncoder());
                        //channel.pipeline().addLast(new IdleStateHandler(0, 0, 10));
                        channel.pipeline().addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        channel.pipeline().addLast(new NettyServerHandler(config.getBrokerId(), config.getLogicUrl()));
                    }
                });
    }

    public void start() {
        this.server.bind(config.getTcpPort());
    }
}
