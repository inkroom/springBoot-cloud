package cn.inkroom.study.cloud.eureka.server;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author inkbox
 * @date 2021/7/20
 */
@Component
public class InstanceFacade {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RestTemplate template;
    @Value("${eureka.client.service-url.defaultZone}")
    private String myUrl;

    @HystrixCommand(fallbackMethod = "aliveAndOutOfServiceFallback")
    public Boolean aliveAndOutOfService(InstanceInfo instance) {
        String r = template.getForObject(myUrl + "/apps/" + instance.getAppName() + "/" + instance.getInstanceId(), String.class);
        logger.info("{}收到的数据={}", instance, r);
        return r != null && r.contains("<status>OUT_OF_SERVICE</status>");
    }

    @HystrixCommand(fallbackMethod = "shutdownFallback")
    public boolean shutdown(InstanceInfo instanceInfo) {


        String url = "http://" + instanceInfo.getHostName() + ":" + instanceInfo.getPort() + "/actuator/shutdown";
        logger.info("发送关机指令={}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(null, headers);
        String s = template.postForObject(url, req, String.class);
        logger.info("主动关闭={}", s);
        return true;
    }

    public boolean shutdownFallback(InstanceInfo instanceInfo, Throwable e) {
        logger.info("{}关机失败", instanceInfo.getInstanceId(), e);
        return false;
    }

    /**
     * 可能该实例已经被关闭
     *
     * @param instance
     * @return
     */
    private Boolean aliveAndOutOfServiceFallback(InstanceInfo instance, Throwable e) {
        logger.debug("{}出了异常", instance, e);
        return null;
    }
}
