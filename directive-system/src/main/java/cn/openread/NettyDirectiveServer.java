package cn.openread;

import cn.openread.eureka.ServicesDiscoveryThread;
import cn.openread.eureka.ServicesDownThread;
import cn.openread.eureka.ServicesRegisterThread;
import cn.openread.handler.HeartBeatServerHandler;
import cn.openread.handler.HttpRequestHandler;
import cn.openread.handler.ObjectWebSocketFrameHandler;
import cn.openread.kits.ChannelAttrKits;
import cn.openread.kits.ConstantKits;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于WS的指令系统
 *
 * @author Simon
 */
@Slf4j
public class NettyDirectiveServer {
    private static final String appName = "directive-system";
    private static final String eurekaAddr = "http://192.168.1.10:8761";
    private static final String localAddr = "127.0.0.1";
    private static final String queueName = "SIMON-MQ-DEV";

    private static final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
    private static final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private int port;
    public static ServicesRegisterThread servicesRegisterThread;

    public NettyDirectiveServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 18888;
        }
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.debug("进行job运行============>");

                for (Channel channel : ConstantKits.channels) {
                    log.debug("写内容给=>{}", ChannelAttrKits.getAttr(channel, ConstantKits.DEV_ID));

//                    log.debug("发送PING");
//                    channel.writeAndFlush(new PingWebSocketFrame());
                    log.debug("发送PONG");
                    channel.writeAndFlush(new PongWebSocketFrame());
                    channel.writeAndFlush(new TextWebSocketFrame("文本内容"));
                }
            }
        }, 1L, 5L, TimeUnit.SECONDS);
        //step 1.启动服务注册
        servicesRegisterThread = new ServicesRegisterThread(cyclicBarrier, scheduledExecutorService, appName, port, eurekaAddr, localAddr);
        servicesRegisterThread.start();

        //step 2.启动服务发现
        ServicesDiscoveryThread discoveryThread = new ServicesDiscoveryThread(cyclicBarrier, scheduledExecutorService, appName, port, eurekaAddr);
        discoveryThread.start();
//
//        //step 3.启动MQ队列监听
//        RedisQueueListenerThread redisQueueListenerThread = new RedisQueueListenerThread(queueName, scheduledExecutorService, cyclicBarrier);
//        redisQueueListenerThread.start();

        //step last.启动netty
        new NettyDirectiveServer(port).run();

    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 4);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebsocketServerInitializer())
                    //tcp握手队列长度
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println(this.getClass().toGenericString() + " bind on port " + this.port);
            System.out.println("Access Address => http://127.0.0.1:" + port + "");

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync();

//            cyclicBarrier.await();

            // 等待服务器  socket 关闭
            f.channel().closeFuture().addListener(future -> {
                ServicesDownThread servicesDownThread = new ServicesDownThread(scheduledExecutorService);
                servicesDownThread.start();
            }).sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println(this.getClass().toGenericString() + " Closed");
        }
    }

    public static class WebsocketServerInitializer extends
            ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("http-decode-encode", new HttpServerCodec());
            pipeline.addLast("http-aggregator", new HttpObjectAggregator(65535));
            pipeline.addLast("http-chunked", new ChunkedWriteHandler());

            //http -> web socket
            pipeline.addLast("http-request", new HttpRequestHandler("/ws"));

            //这里表示600秒没收到客户端的发来的数据,就触发函数userEventTriggered
            pipeline.addLast("ping-pong", new IdleStateHandler(30, 30, 0, TimeUnit.SECONDS));
            pipeline.addLast("WebSocket-protocol", new WebSocketServerProtocolHandler("/ws", true));
            pipeline.addLast("ws-compress", new WebSocketServerCompressionHandler());
            pipeline.addLast("WebSocket-request", new ObjectWebSocketFrameHandler());
            pipeline.addLast("heart-beat", new HeartBeatServerHandler());
        }
    }
}