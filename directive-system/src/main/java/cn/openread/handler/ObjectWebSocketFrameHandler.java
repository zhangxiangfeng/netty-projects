package cn.openread.handler;


import cn.openread.kits.ChannelAttrKits;
import cn.openread.kits.ConstantKits;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理Object
 */
@Slf4j
public class ObjectWebSocketFrameHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 这里集中存放channel
     */
    public static ChannelGroup channels = ConstantKits.channels;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                Object object) {
        Channel incoming = ctx.channel();

        if (object instanceof TextWebSocketFrame) {

            TextWebSocketFrame obj = (TextWebSocketFrame) object;
            for (Channel channel : channels) {
                String devId = ChannelAttrKits.getAttr(channel, "devId");

                log.debug("接收到内容：{}", obj.text());
                if (channel != incoming) {
                    channel.writeAndFlush(new TextWebSocketFrame(obj.text() + " devId => " + devId));
                } else {
                    channel.writeAndFlush(new TextWebSocketFrame("我发的 " + obj.text() + " devId => " + devId));
                }
            }
        } else {
            log.debug("接受到的数据类：" + object);
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

        log.warn("Client:" + incoming.remoteAddress() + "离开");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        log.debug("Client:" + incoming.remoteAddress() + "在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        log.error("Client:" + incoming.remoteAddress() + "掉线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.toString());
        ctx.close();
    }

}
