package cn.openread.handler;

import cn.openread.kits.ChannelAttrKits;
import cn.openread.kits.ConstantKits;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳机制
 *
 * @author Simon
 * @see https://blog.csdn.net/u013967175/article/details/78591810
 */
@Slf4j
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
    private int lossConnectCount = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            log.warn("许久未收到客户端 => {} 的消息了!", ctx.channel().remoteAddress());
            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state() == IdleState.READER_IDLE) {
//                lossConnectCount++;
//                if (lossConnectCount >= 3) {
//                    log.warn("关闭这个不活跃通道 => {} !", ctx.channel().remoteAddress());
//                    ctx.channel().close();
//                }
//            }
            if (event.state() == IdleState.WRITER_IDLE) {
                log.debug("主动进行对Id={} => PING", ChannelAttrKits.getAttr(ctx.channel(), ConstantKits.DEV_ID));
                ctx.channel().writeAndFlush(new PingWebSocketFrame());
            } else {
                log.debug("心跳机制检测到的事件 => {}", event.state().name());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        lossConnectCount = 0;
        log.debug("client says: " + msg.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
