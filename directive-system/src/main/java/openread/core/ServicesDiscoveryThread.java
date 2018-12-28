package openread.core;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务发现线程
 * 1.每间隔固定进行心跳一次
 */
public class ServicesDiscoveryThread extends Thread {
    private static final long heartBeatTime = 2;

    private CyclicBarrier cyclicBarrier;
    private ScheduledExecutorService scheduledExecutorService;

    public ServicesDiscoveryThread(CyclicBarrier cyclicBarrier, ScheduledExecutorService scheduledExecutorService) {
        this.cyclicBarrier = cyclicBarrier;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        try {
            cyclicBarrier.await();
            System.out.println("=================================================");
            System.out.println("               服务发现线程启动                    ");
            System.out.println("=================================================");
            this.scheduledExecutorService.scheduleAtFixedRate((() -> {
                System.out.println("心跳 => " + new Date().toLocaleString());
            }), 0, heartBeatTime, TimeUnit.SECONDS);

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
