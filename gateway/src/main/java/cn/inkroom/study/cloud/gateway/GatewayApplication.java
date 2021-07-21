package cn.inkroom.study.cloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
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
    @Autowired
    private GatewayProperties gatewayProperties;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public RedisRouteDefinitionRepository redisRouteDefinitionRepository(){
        return new RedisRouteDefinitionRepository();
    }

//    @Bean
    public RouteLocator locatorProd(RouteLocatorBuilder builder) {


        logger.info("配置的路由{}", gatewayProperties.getRoutes());

        RouteLocatorBuilder.Builder contract = builder.routes();
        // eureka注册中心，暴露出来用于服务上下线
        contract.route("eureka", f ->
                f.path("/eureka/index.html")
                        .filters(fil -> fil.rewritePath(".*", "/").preserveHostHeader())
                        .uri("lb://EUREKA"));
        contract.route("eureka-console",
                f -> f.path("/eureka-console/**").filters(GatewayFilterSpec::preserveHostHeader).uri("lb://eureka"));

        //匹配eureka的静态资源以及一些api请求
        contract.route("eureka", f -> f.path("/eureka/**").filters(GatewayFilterSpec::preserveHostHeader).uri("lb://eureka"));
//        admin-server 网关
        contract.route("admin-server", f -> f.path("/admin-server/**")
                .filters(GatewayFilterSpec::preserveHostHeader).uri("lb://admin-server"));

        if ("dev".equals(active)) {
            contract.route("contract", f -> f.path("/**").filters(GatewayFilterSpec::preserveHostHeader)
                    .uri("lb://contract"));
        } else {
            contract.route("contract", f -> f.host("www.bcyunqian.com")
                    .and().host("yapi.bcyunqian.com")
                    .and().host("yq.pre.bcyunqian.com")
                    .filters(GatewayFilterSpec::preserveHostHeader)
                    .uri("lb://contract"))
                    .route("contact-admin", f -> f.host("admin.bcyunqian.com")
                            .filters(GatewayFilterSpec::preserveHostHeader).uri("lb://contract-admin"))
                    .route("uc", f -> f.host("uc.bcyunqian.com")
                            .filters(GatewayFilterSpec::preserveHostHeader).uri("lb://user-center"))
//                    前台测试环境
                    .route("contract-test", f -> f.host("www.test.bcyunqian.com")
                            .and().host("yapi.test.bcyunqian.com")
                            .filters(GatewayFilterSpec::preserveHostHeader)
                            .uri("lb://contract-test"))
//                    后台管理测试环境
                    .route("contract-admin-test", f -> f.host("admin.bcyunqian.com")
                            .and()
                            .path("/test/**")
                            .filters(filter -> filter.stripPrefix(1))
                            .uri("lib://contract-admin-test")
                    )
            ;
        }
        return contract.build();
    }


}
