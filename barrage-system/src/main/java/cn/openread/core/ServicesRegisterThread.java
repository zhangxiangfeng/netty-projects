package cn.openread.core;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 服务注册线程
 */
public class ServicesRegisterThread extends Thread {
    private CyclicBarrier cyclicBarrier;
    private ScheduledExecutorService scheduledExecutorService;

    public ServicesRegisterThread(CyclicBarrier cyclicBarrier, ScheduledExecutorService scheduledExecutorService) {
        this.cyclicBarrier = cyclicBarrier;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        try {
            cyclicBarrier.await();
            System.out.println("=================================================");
            System.out.println("               服务注册线程启动                   ");
            System.out.println("=================================================");
            this.scheduledExecutorService.execute(() -> {
                System.out.println("注册服务 => " + new Date().toLocaleString());
            });

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
