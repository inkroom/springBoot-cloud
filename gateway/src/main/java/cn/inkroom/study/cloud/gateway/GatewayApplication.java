package cn.inkroom.study.cloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.web.embedded.netty.NettyWebServer;
import org.springframework.cloud.context.environment.EnvironmentManager;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * @author inkbox
 * @date 2021/7/15
 */
@SpringBootApplication
public class GatewayApplication implements EnvironmentAware {
    public static void main(String[] args) {
//        EnvironmentPostProcessor
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Value("${spring.profiles.active}")
    private String active;
    @Value("${management.endpoint.health.show-details}")
    private String t;

    @Autowired
    private GatewayProperties gatewayProperties;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public RedisRouteDefinitionRepository redisRouteDefinitionRepository(){
        return new RedisRouteDefinitionRepository();
    }

    @Bean
    public RouteLocator locatorProd(RouteLocatorBuilder builder) {

        RouteLocatorBuilder.Builder contract = builder.routes();
        //屏蔽监控端点
        contract.route("actuator", f -> f.path("/actuator/**").filters(gatewayFilterSpec -> gatewayFilterSpec.filter((exchange, chain) -> {
            // 直接404
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().setContentType(MediaType.valueOf(MediaType.TEXT_PLAIN_VALUE + ";charset=utf-8"));
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.writeWith(Mono.just(response.bufferFactory().wrap("404了".getBytes(StandardCharsets.UTF_8))));
        })).uri("lb://404"));//反正不会到后面，地址随便填一个就行

//        // eureka注册中心，暴露出来用于服务上下线
//        contract.route("eureka", f ->
//                f.path("/eureka/index.html")
//                        .filters(fil -> fil.rewritePath(".*", "/").preserveHostHeader())
//                        .uri("lb://EUREKA"));
//        contract.route("eureka-console",
//                f -> f.path("/eureka-console/**").filters(GatewayFilterSpec::preserveHostHeader).uri("lb://eureka"));
//
//        //匹配eureka的静态资源以及一些api请求
//        contract.route("eureka", f -> f.path("/eureka/**").filters(GatewayFilterSpec::preserveHostHeader).uri("lb://eureka"));
////        admin-server 网关
//        contract.route("admin-server", f -> f.path("/admin-server/**")
//                .filters(GatewayFilterSpec::preserveHostHeader).uri("lb://admin-server"));
//
//        if ("dev".equals(active)) {
//            contract.route("contract", f -> f.path("/**").filters(GatewayFilterSpec::preserveHostHeader)
//                    .uri("lb://contract"));
//        } else {
//            contract.route("contract", f -> f.host("www.bcyunqian.com")
//                    .and().host("yapi.bcyunqian.com")
//                    .and().host("yq.pre.bcyunqian.com")
//                    .filters(GatewayFilterSpec::preserveHostHeader)
//                    .uri("lb://contract"))
//                    .route("contact-admin", f -> f.host("admin.bcyunqian.com")
//                            .filters(GatewayFilterSpec::preserveHostHeader).uri("lb://contract-admin"))
//                    .route("uc", f -> f.host("uc.bcyunqian.com")
//                            .filters(GatewayFilterSpec::preserveHostHeader).uri("lb://user-center"))
////                    前台测试环境
//                    .route("contract-test", f -> f.host("www.test.bcyunqian.com")
//                            .and().host("yapi.test.bcyunqian.com")
//                            .filters(GatewayFilterSpec::preserveHostHeader)
//                            .uri("lb://contract-test"))
////                    后台管理测试环境
//                    .route("contract-admin-test", f -> f.host("admin.bcyunqian.com")
//                            .and()
//                            .path("/test/**")
//                            .filters(filter -> filter.stripPrefix(1))
//                            .uri("lib://contract-admin-test")
//                    )
//            ;
//        }
        return contract.build();
    }


    @Override
    public void setEnvironment(Environment environment) {
        logger.info("环境变量=2{}", environment);
    }
}
