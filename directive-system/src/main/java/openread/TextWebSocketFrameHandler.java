package openread;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理TextWebSocketFrame
 */
@Slf4j
public class TextWebSocketFrameHandler extends
        SimpleChannelInboundHandler<Object> {

    /**
     * 这里集中存放channel
     */
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final String URI = "ws";

    private WebSocketServerHandshaker handshaker;

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status() != HttpResponseStatus.OK) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status() != HttpResponseStatus.OK) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * wetsocket第一次连接握手
     */
    private void doHandlerHttpRequest(ChannelHandlerContext ctx, HttpRequest msg) {
        // http 解码失败
        if (!msg.decoderResult().isSuccess() || (!"websocket".equals(msg.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, (FullHttpRequest) msg, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        }
        // 可以获取msg的uri来判断
        String uri = msg.uri();
        if (!uri.substring(1).equals(URI)) {
            ctx.close();
        }
        // 不知道干嘛的
        // ctx.attr(AttributeKey.valueOf("type")).set(uri);
        // 可以通过url获取其他参数
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
                "ws://" + msg.headers().get("Host") + "/" + URI + "", null, false
        );
        handshaker = factory.newHandshaker(msg);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        // 进行连接
        handshaker.handshake(ctx.channel(), (FullHttpRequest) msg);
        // 可以做其他处理
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                Object obj) throws Exception {
        if (obj instanceof HttpRequest) {
//            doHandlerHttpRequest(ctx, (HttpRequest) obj);
        } else if (obj instanceof TextWebSocketFrame) {
            Channel incoming = ctx.channel();

            TextWebSocketFrame socketFrame = (TextWebSocketFrame) obj;
            for (Channel channel : channels) {
                if (channel != incoming) {
                    channel.writeAndFlush(new TextWebSocketFrame(socketFrame.text()));
                } else {
                    channel.writeAndFlush(new TextWebSocketFrame("我发的 " + socketFrame.text()));
                }
            }
        } else {
            throw new Exception(obj.toString());
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        log.debug("[设备端] - " + incoming.remoteAddress() + " 加入");
        incoming.writeAndFlush("[设备端] - " + incoming.remoteAddress() + " 加入");
        channels.add(incoming);

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();

        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));

        System.err.println("Client:" + incoming.remoteAddress() + "离开");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        System.out.println("Client:" + incoming.remoteAddress() + "在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        System.err.println("Client:" + incoming.remoteAddress() + "掉线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        System.err.println("Client:" + incoming.remoteAddress() + "异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
