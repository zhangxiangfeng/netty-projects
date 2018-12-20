package cn.openread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 基于WS的弹幕系统
 */
public class WebSocketBarrageServer {

    private int port;

    public WebSocketBarrageServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new WebSocketBarrageServer(port).run();

    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 4);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebsocketDanmuServerInitializer())
                    //tcp握手队列长度
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println(this.getClass().toGenericString() + " bind on port " + this.port);

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync();

            // 等待服务器  socket 关闭 。
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println(this.getClass().toGenericString() + " Closed");
        }
    }

    public static class WebsocketDanmuServerInitializer extends
            ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("http-decodec", new HttpRequestDecoder());
            pipeline.addLast("http-aggregator", new HttpObjectAggregator(65535));
            pipeline.addLast("http-encodec", new HttpResponseEncoder());
            pipeline.addLast("http-chunked", new ChunkedWriteHandler());
            pipeline.addLast("http-request", new HttpRequestHandler("/ws"));
            pipeline.addLast("WebSocket-protocol", new WebSocketServerProtocolHandler("/ws"));
            pipeline.addLast("WebSocket-request", new TextWebSocketFrameHandler());

        }
    }
}