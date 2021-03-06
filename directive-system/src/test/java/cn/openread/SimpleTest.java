package cn.openread;

import cn.openread.dto.MQMsgDTO;
import cn.openread.kits.MQMsgWrapKits;
import cn.openread.mq.RedisQueueAPI;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.net.MimeTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SimpleTest {

    private static final String queueName = "SIMON-MQ-DEV";

    @Test
    public void test5() throws InterruptedException {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (i % 2 == 0) {
                RedisQueueAPI.offerQueue(queueName, MQMsgWrapKits.encodeMsg(MQMsgDTO
                        .builder()
                        .devId("F0001")
                        .directive("OPEN_DOOR-" + "F0001")
                        .build()));
                Thread.sleep(1000);
            } else {
                RedisQueueAPI.offerQueue(queueName, MQMsgWrapKits.encodeMsg(MQMsgDTO
                        .builder()
                        .devId("F0002")
                        .directive("OPEN_DOOR-" + "F0002")
                        .build()));
                Thread.sleep(1000);
            }

        }
        System.out.println("指令发送完毕");
    }

    @Test
    public void test4() throws InterruptedException {
        while (true) {
            String result = RedisQueueAPI.takeQueue(queueName);
            log.debug(result);
//            Thread.sleep(1000L);
        }
    }

    @Test
    public void test3() throws UnknownHostException {

        InetAddress ia = InetAddress.getLocalHost();
        String host = ia.getHostName();//获取计算机主机名
        String IP = ia.getHostAddress();//获取计算机IP

        System.out.println(host);

        System.out.println(IP);
    }


    @Test
    public void test2() {

        Map valuesMap = new HashMap();
        valuesMap.put("animal", "quick brown fox");
        valuesMap.put("target", "lazy dog");
        String templateString = "The ${animal} jumped over the ${target}.";
        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String resolvedString = sub.replace(templateString);
        System.out.println(resolvedString);
    }

    @Test
    public void test1() {

        String regXML = "<instance>\n" +
                "\t<instanceId>demo-order2:11101</instanceId>\n" +
                "    <hostName>127.0.0.1</hostName>\n" +
                "\t<app>DEMO-ORDER2</app>\n" +
                "    <ipAddr>127.0.0.1</ipAddr>\n" +
                "\t<status>UP</status>\n" +
                "\t<overriddenstatus>UNKNOWN</overriddenstatus>\n" +
                "\t<port enabled=\"true\">11101</port>\n" +
                "\t<securePort enabled=\"false\">443</securePort>\n" +
                "\t<countryId>1</countryId>\n" +
                "\t<dataCenterInfo class=\"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo\">\n" +
                "\t    <name>MyOwn</name>\n" +
                "\t</dataCenterInfo>\n" +
                "\t<metadata class=\"java.util.Collections$EmptyMap\"/>\n" +
                "\t<vipAddress>demo-order2</vipAddress>\n" +
                "\t<secureVipAddress>demo-order2</secureVipAddress>\n" +
                "\t<statusPageUrl>http://10.56.5.18:11101/actuator/info</statusPageUrl>\n" +
                "\t<healthCheckUrl>http://10.56.5.18:11101/actuator/health</healthCheckUrl>\n" +
                "\t<isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>\n" +
                "</instance>";

        String eurekaAddr = "http://localhost:8761";
        String appName = "demo-order2";
        String url = eurekaAddr + "/eureka/apps/" + appName;
        HttpResponse httpResponse = HttpRequest.post(url)
                .header("Content-Type", "application/xml")
                .bodyText(regXML, MimeTypes.MIME_APPLICATION_XML)
                .send();
        System.out.println(httpResponse.statusCode());
    }
}
