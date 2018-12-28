package openread.core;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 服务下线线程
 */
public class ServicesDownThread extends Thread {
    private CyclicBarrier cyclicBarrier;
    private ScheduledExecutorService scheduledExecutorService;

    public ServicesDownThread(CyclicBarrier cyclicBarrier, ScheduledExecutorService scheduledExecutorService) {
        this.cyclicBarrier = cyclicBarrier;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        try {
            cyclicBarrier.await();
            System.out.println("=================================================");
            System.out.println("               服务下线线程启动                   ");
            System.out.println("=================================================");
            this.scheduledExecutorService.execute(() -> {
                System.out.println(new Date().toLocaleString());
            });

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
