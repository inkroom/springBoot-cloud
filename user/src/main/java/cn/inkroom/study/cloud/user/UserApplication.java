package cn.inkroom.study.cloud.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author inkbox
 * @date 2021/7/13
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserApplication {

//    @Bean
//    public EmbeddedServletContainerCustomizer containerCustomizer() {
//        return new EmbeddedServletContainerCustomizer() {
//            @Override
//            public void customize(ConfigurableEmbeddedServletContainer container) {
//                int port = SocketUtils.findAvailableTcpPort(8001, 8999);
//                container.setPort(port);
//                System.getProperties().put("server.port", port);
//            }
//        };
//    }
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
