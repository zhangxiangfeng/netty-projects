package cn.openread.mq;

import cn.openread.dto.MQMsgDTO;
import cn.openread.kits.ConstantKits;
import cn.openread.kits.MQMsgWrapKits;
import cn.openread.kits.MatcherChannelKits;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;

/**
 * 指令服务线程
 *
 * @author Simon
 */
@AllArgsConstructor
@Slf4j
public class RedisQueueListenerThread extends Thread {
    private static final long heartBeatTime = 1;

    private String queueName;
    private ExecutorService executorService;
    private CyclicBarrier cyclicBarrier;

    @Override
    public void run() {
        try {
            cyclicBarrier.await();
            log.debug("=================================================");
            log.debug("                  MQ监听线程启动                   ");
            log.debug("=================================================");
            while (true) {
                try {
                    //step 1.从队列取数据
                    String msg = RedisQueueAPI.takeQueue(queueName);

                    //step 2.进行业务处理
                    if (StringUtils.isNotBlank(msg)) {
                        this.executorService.execute((() -> {
                            MQMsgDTO mqMsg = MQMsgWrapKits.decodeMsg(msg);
                            log.debug("RedisQueueListenerThread 监听到指令 => {},开始处理", mqMsg);

                            Channel channel = MatcherChannelKits.getChannelByNameAndValue(ConstantKits.DEV_ID, mqMsg.getDevId());

                            if (channel != null) {
                                log.debug("指令处理中,开始发送 => {}", mqMsg.getDirective());
                                channel.writeAndFlush(new TextWebSocketFrame(mqMsg.getDirective()));
                                log.debug("本次指令 => {} 处理完毕", mqMsg);
                            } else {
                                //TODO step 3.没有处理就暂时遗弃
                                log.warn("没有找到对应的客户端,指令遗弃处理 => {}", mqMsg);
                            }
                        }));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    //step last.避免长时间空轮询,形成cpu 100%的问题
                    Thread.yield();
                }
            }
        } catch (InterruptedException | BrokenBarrierException e) {
            log.error(e.getMessage(), e);
        }
    }
}
