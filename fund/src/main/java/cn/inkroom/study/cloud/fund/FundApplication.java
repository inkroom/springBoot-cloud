package cn.inkroom.study.cloud.fund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author inkbox
 * @date 2021/7/13
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"cn.inkroom.study.cloud.fund"})
public class FundApplication {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(FundApplication.class, args);
    }
}
