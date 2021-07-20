package cn.inkroom.study.cloud.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author inkbox
 * @date 2021/7/15
 */
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Value("${spring.profiles.active}")
    private String active;

    @Bean
    public RouteLocator locatorProd(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder contract = builder.routes();
        // eureka注册中心，暴露出来用于服务上下线
        contract.route("eureka", f ->
                f.path("/eureka/index.html")
                        .filters(fil->fil.rewritePath(".*","/"))
                        .uri("lb://EUREKA"));
        //匹配eureka的静态资源以及一些api请求
        contract.route("eureka", f -> f.path("/eureka/**").uri("lb://eureka"));

        if ("dev".equals(active)) {
            contract.route("contract", f -> f.path("/**")
                    .uri("lb://contract"));
        } else {
            contract.route("contract", f -> f.host("www.bcyunqian.com")
                    .and().host("yapi.bcyunqian.com")
                    .and().host("yq.pre.bcyunqian.com")
                    .uri("lb://contract"))
                    .route("contact-admin", f -> f.host("admin.bcyunqian.com").uri("lb://contract-admin"))
                    .route("uc", f -> f.host("uc.bcyunqian.com").uri("lb://user-center"))
                    .route("contract-test", f -> f.host("www.test.bcyunqian.com")
                            .and().host("yapi.test.bcyunqian.com").uri("lb://contract-test"))
            ;
        }
        return contract.build();
    }

}
