package openread.core;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务发现线程
 * 1.每间隔固定进行心跳一次
 */
@AllArgsConstructor
@Slf4j
public class ServicesDiscoveryThread extends Thread {
    private static final long heartBeatTime = 10;

    private CyclicBarrier cyclicBarrier;
    private ScheduledExecutorService scheduledExecutorService;
    private String appName;
    private int appPort;
    private String eurekaAddr;


    @Override
    public void run() {
        try {
            cyclicBarrier.await();
            this.scheduledExecutorService.scheduleAtFixedRate((() -> {
                try {
                    String url = eurekaAddr + "/eureka/apps/${appName}/${hostname}:${appName}:${port}";


                    Map<String, String> paramMap = new HashMap<>(3);
                    paramMap.put("appName", appName);
                    paramMap.put("port", String.valueOf(appPort));
                    paramMap.put("hostname", InetAddress.getLocalHost().getHostName());

                    StringSubstitutor sub = new StringSubstitutor(paramMap);
                    url = sub.replace(url);

                    HttpResponse httpResponse = HttpRequest.put(url).send();

                    log.debug("服务发现线程,心跳 =>{}，返回状态码=> {} ", new Date().toLocaleString(), httpResponse.statusCode());
                } catch (UnknownHostException e) {
                    log.error(e.getMessage(), e);
                }
            }), heartBeatTime, heartBeatTime, TimeUnit.SECONDS);

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
