package cn.openread.eureka;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 服务下线线程
 */
@Slf4j
public class ServicesDownThread extends Thread {
    private ScheduledExecutorService scheduledExecutorService;

    public ServicesDownThread(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        log.debug("=================================================");
        log.debug("               服务下线线程启动                   ");
        log.debug("=================================================");
        this.scheduledExecutorService.execute(() -> {


        });
    }
}
