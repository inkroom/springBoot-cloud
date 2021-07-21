package cn.inkroom.study.cloud.gateway;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.actuate.GatewayControllerEndpoint;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author inkbox
 * @date 2021/7/20
 */
@Component
public class UrlFilter implements GlobalFilter, Ordered {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private EurekaClient context;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("{}  {},所有实例={}", exchange.getRequest().getURI(), context.getApplication("EUREKA").getInstances().get(0).getPort(), context.getApplications());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public static void main(String[] args) {
        Mono.just(new String("323232323")).flatMap(new Function<String, Mono<?>>() {
            @Override
            public Mono<?> apply(String s) {
                System.out.println(s);
                return Mono.empty();
            }
        }).subscribe();
    }
}
