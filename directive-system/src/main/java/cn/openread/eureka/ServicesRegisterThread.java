package cn.openread.eureka;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.net.MimeTypes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 服务注册线程
 */
@AllArgsConstructor
@Slf4j
public class ServicesRegisterThread extends Thread {
    private CyclicBarrier cyclicBarrier;
    private ScheduledExecutorService scheduledExecutorService;
    private String appName;
    private int appPort;
    private String eurekaAddr;
    private String localAddr;


    @Override
    public void run() {
        try {
            cyclicBarrier.await();

            log.debug("=================================================");
            log.debug("               服务注册线程启动                   ");
            log.debug("=================================================");
            this.scheduledExecutorService.execute(() -> {

                try {
                    String regXML = "<instance>" +
                            "<instanceId>${hostname}:${appName}:${port}</instanceId>" +
                            "    <hostName>${hostname}</hostName>" +
                            "<app>${appName}</app>" +
                            "    <ipAddr>${ip}</ipAddr>" +
                            "<status>UP</status>" +
                            "<overriddenstatus>UNKNOWN</overriddenstatus>" +
                            "<port enabled=\"true\">${port}</port>" +
                            "<securePort enabled=\"false\">443</securePort>" +
                            "<countryId>1</countryId>" +
                            "<dataCenterInfo class=\"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo\">" +
                            "    <name>MyOwn</name>" +
                            "</dataCenterInfo>" +
                            "<metadata class=\"java.util.Collections$EmptyMap\"/>" +
                            "<vipAddress>${appName}</vipAddress>" +
                            "<secureVipAddress>${appName}</secureVipAddress>" +
                            "<statusPageUrl>http://${ip}:${port}/actuator/info</statusPageUrl>" +
                            "<healthCheckUrl>http://${ip}:${port}/actuator/health</healthCheckUrl>" +
                            "<isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>" +
                            "</instance>";

                    Map<String, String> paramMap = new HashMap<>(4);
                    paramMap.put("appName", appName);
                    paramMap.put("port", String.valueOf(appPort));
                    paramMap.put("hostname", InetAddress.getLocalHost().getHostName());
                    paramMap.put("ip", localAddr);


                    StringSubstitutor sub = new StringSubstitutor(paramMap);
                    regXML = sub.replace(regXML);

                    String url = eurekaAddr + "/eureka/apps/" + appName;
                    HttpResponse httpResponse = HttpRequest.post(url)
                            .header("Content-Type", "application/xml")
                            .bodyText(regXML, MimeTypes.MIME_APPLICATION_XML)
                            .send();
                    log.info("启动注册Eureka中心,返回状态码 => " + httpResponse.statusCode());
                } catch (UnknownHostException e) {
                    log.error(e.getMessage(), e);
                }

            });

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
