package cn.inkroom.study.cloud.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于redis存储路由信息
 *
 * @author inkbox
 * @date 2021/7/20
 */
//@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository , InitializingBean {
    @Autowired
    private StringRedisTemplate template;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final String KEY = "spring_gateway_routers";
    @Autowired
    private ObjectMapper mapper;



    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {

        logger.info("获取路由");
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        template.opsForHash().values(KEY).stream()
                .forEach(routeDefinition -> {
                    try {
                        routeDefinitions.add(mapper.readValue(routeDefinition.toString(), RouteDefinition.class));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        return Flux.fromIterable(routeDefinitions);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {
            try {
                logger.info("保存路由={}",routeDefinition.getId());
                template.opsForHash().put(KEY, routeDefinition.getId(), mapper.writeValueAsString(routeDefinition));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (template.opsForHash().hasKey(KEY, id)) {
                template.opsForHash().delete(KEY, id);
            }
            return Mono.empty();
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId("eureka");
        routeDefinition.setUri(URI.create("lb://eureka"));
        routeDefinition.setOrder(0);

        //断言
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        PredicateDefinition predicateDefinition = new PredicateDefinition();
        predicateDefinition.setName("Path");
        predicateDefinition.setArgs(Map.of("patterns","/eureka/index.html"));

        predicateDefinitions.add(predicateDefinition);

        //过滤器
        List<FilterDefinition> filterDefinitions = new ArrayList<>();
        FilterDefinition filterDefinition = new FilterDefinition();
        filterDefinition.setName("RewritePath");
        filterDefinition.setArgs(Map.of("regexp",".*","replacement","/"));
        filterDefinitions.add(filterDefinition);

        routeDefinition.setPredicates(predicateDefinitions);
        routeDefinition.setFilters(filterDefinitions);
        logger.info("开始保存");
        save(Mono.just(routeDefinition)).block();
//        .subscribe();



    }
}
