package com.heim.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.IOException;

public class NettyServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boosGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);

//
//        ServerBootstrap bootstrapSSl = new ServerBootstrap();
//        bootstrapSSl.group(boosGroup, workerGroup);
//        bootstrapSSl.channel(NioServerSocketChannel.class);

        //   SSLHandlerProvider.initSSLContext();
        // ===========================================================
        // 1. define a separate thread pool to execute handlers with
        //    slow business logic. e.g database operation
        // ===========================================================
        final EventExecutorGroup group = new DefaultEventExecutorGroup(1500); //thread pool of 1500


        bootstrap.

                handler(new LoggingHandler(LogLevel.DEBUG)).
                childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                //pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, 60));
                //  pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, 5)); // add with name


                //  pipeline.addLast("xmppEncoder",new XmppMsgEncoder()); // add without name, name auto generated
                //  pipeline.addLast("xmppDecoder",new XmppMsgDecoder()); // add without name, name auto generated


                pipeline.addLast("stringEnc", new StringEncoder());
                pipeline.addLast("stringDec", new StringDecoder());
                //     //===========================================================
                // 2. run handler with slow business logic
                //    in separate thread from I/O thread
                //===========================================================
                //pipeline.addLast(SSLHandlerProvider.getSSLHandler());
                pipeline.addLast(group, "serverHandler", new ServerHandler(pipeline));

                pipeline.addLast(new SecureChatServerInitializer(SSLHandlerProvider.getContext()));

            }
        });
//


        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.bind(5222).sync();
        // bootstrap.bind(5223).sync();
        //  bootstrapSSl.childOption(ChannelOption.SO_KEEPALIVE, true);
//        bootstrapSSl.bind(5223).sync();
    }
}