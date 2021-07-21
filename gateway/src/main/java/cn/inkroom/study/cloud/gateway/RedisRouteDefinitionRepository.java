package cn.inkroom.study.cloud.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
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
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository, InitializingBean {
    @Autowired
    private StringRedisTemplate template;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final String KEY = "spring_gateway_routers";
    @Autowired
    private ObjectMapper mapper;

    @Value("${spring.profiles.active}")
    private String active;


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
                logger.info("保存路由={}", routeDefinition.getId());
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


        if (template.hasKey(KEY)) {
            return;
        }
        List<RouteDefinition> de;
        if ("dev".equals(active)) {
            de = mapper.readValue("[{\"id\":\"admin-server\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/admin-server/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://admin-server\",\"order\":0},{\"id\":\"eureka-console\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/eureka-console/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://eureka\",\"order\":1},{\"id\":\"eureka\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/eureka/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://eureka\",\"order\":2},{\"id\":\"contract\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://contract\",\"order\":3}]", new TypeReference<List<RouteDefinition>>() {
            });
        } else {
            de = mapper.readValue("[{\"id\":\"admin-server\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/admin-server/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://admin-server\",\"order\":0},{\"id\":\"eureka-console\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/eureka-console/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://eureka\",\"order\":1},{\"id\":\"eureka\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/eureka/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://eureka\",\"order\":2},{\"id\":\"contract\",\"predicates\":[{\"name\":\"Host\",\"args\":{\"1\":\"yapi.bcyunqian.com\",\"2\":\"yq.pre.bcyunqian.com\",\"3\":\"www.bcyunqian.com\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://contract\",\"order\":3},{\"id\":\"contact-admin\",\"predicates\":[{\"name\":\"Host\",\"args\":{\"1\":\"admin.bcyunqian.com\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://contract-admin\",\"order\":4},{\"id\":\"uc\",\"predicates\":[{\"name\":\"Host\",\"args\":{\"1\":\"uc.bcyunqian.com\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://user-center\",\"order\":5},{\"id\":\"contract-test\",\"predicates\":[{\"name\":\"Host\",\"args\":{\"1\":\"www.test.bcyunqian.com\",\"2\":\"yq.test.bcyunqian.com\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://contract-test\",\"order\":6},{\"id\":\"contract-admin-test\",\"predicates\":[{\"name\":\"Host\",\"args\":{\"1\":\"admin.test.bcyunqian.com\"}},{\"name\":\"Path\",\"args\":{\"1\":\"/test/**\"}}],\"filters\":[{\"name\":\"StripPrefix\",\"args\":{\"1\":\"1\"}},{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://contract-test\",\"order\":7}]", new TypeReference<List<RouteDefinition>>() {
            });
        }
        for (RouteDefinition r : de) {
            save(Mono.just(r)).block();
        }

//        List<RouteDefinition> list = new ArrayList<>();
//
//        RouteDefinition routeDefinition = new RouteDefinition();
//        routeDefinition.setId("eureka-console");
//        routeDefinition.setUri(URI.create("lb://eureka"));
//        routeDefinition.setOrder(0);
//
//        //断言
//        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
//        PredicateDefinition predicateDefinition = new PredicateDefinition();
//        predicateDefinition.setName("Path");
//        predicateDefinition.setArgs(Map.of("patterns", "/eureka-console/**"));
//
//        predicateDefinitions.add(predicateDefinition);
//
//        //过滤器
//        List<FilterDefinition> filterDefinitions = new ArrayList<>();
//        FilterDefinition filterDefinition = new FilterDefinition();
//        filterDefinition.setName("PreserveHostHeader");
////        filterDefinition.setArgs(Map.of("regexp",".*","replacement","/"));
//        filterDefinitions.add(filterDefinition);
//
//        routeDefinition.setPredicates(predicateDefinitions);
//        routeDefinition.setFilters(filterDefinitions);
//        logger.info("开始保存");
//        list.add(routeDefinition);
//        save(Mono.just(routeDefinition)).block();
//
//        // eureka接口
//        routeDefinition = new RouteDefinition();
//        routeDefinition.setId("eureka");
//        routeDefinition.setUri(URI.create("lb://eureka"));
//        routeDefinition.setOrder(0);
//
//        //断言
//        predicateDefinitions.clear();
//        predicateDefinition = new PredicateDefinition();
//        predicateDefinition.setName("Path");
//        predicateDefinition.setArgs(Map.of("patterns", "/eureka/**"));
//
//        predicateDefinitions.add(predicateDefinition);
//
//        //过滤器
//        filterDefinitions.clear();
//        filterDefinition = new FilterDefinition();
//        filterDefinition.setName("PreserveHostHeader");
////        filterDefinition.setArgs(Map.of("regexp",".*","replacement","/"));
//        filterDefinitions.add(filterDefinition);
//
//        routeDefinition.setPredicates(predicateDefinitions);
//        routeDefinition.setFilters(filterDefinitions);
//        logger.info("开始保存");
//        list.add(routeDefinition);
//        save(Mono.just(routeDefinition)).block();
//
//
//        // admin-server
//        routeDefinition = new RouteDefinition();
//        routeDefinition.setId("admin-server");
//        routeDefinition.setUri(URI.create("lb://admin-server"));
//        routeDefinition.setOrder(0);
//
//        //断言
//        predicateDefinitions.clear();
//        predicateDefinition = new PredicateDefinition();
//        predicateDefinition.setName("Path");
//        predicateDefinition.setArgs(Map.of("patterns", "/admin-server/**"));
//
//        predicateDefinitions.add(predicateDefinition);
//
//        //过滤器
//        filterDefinitions.clear();
//        filterDefinition = new FilterDefinition();
//        filterDefinition.setName("PreserveHostHeader");
////        filterDefinition.setArgs(Map.of("regexp",".*","replacement","/"));
//        filterDefinitions.add(filterDefinition);
//
//        routeDefinition.setPredicates(predicateDefinitions);
//        routeDefinition.setFilters(filterDefinitions);
//        logger.info("开始保存");
//        list.add(routeDefinition);
//        save(Mono.just(routeDefinition)).block();
//
//
//        // eureka接口
//        routeDefinition = new RouteDefinition();
//        routeDefinition.setId("eureka");
//        routeDefinition.setUri(URI.create("lb://eureka"));
//        routeDefinition.setOrder(0);
//
//        //断言
//        predicateDefinitions.clear();
//        predicateDefinition = new PredicateDefinition();
//        predicateDefinition.setName("Path");
//        predicateDefinition.setArgs(Map.of("patterns", "/eureka/**"));
//
//        predicateDefinitions.add(predicateDefinition);
//
//        //过滤器
//        filterDefinitions.clear();
//        filterDefinition = new FilterDefinition();
//        filterDefinition.setName("PreserveHostHeader");
////        filterDefinition.setArgs(Map.of("regexp",".*","replacement","/"));
//        filterDefinitions.add(filterDefinition);
//
//        routeDefinition.setPredicates(predicateDefinitions);
//        routeDefinition.setFilters(filterDefinitions);
//        logger.info("开始保存");
//        list.add(routeDefinition);
//        save(Mono.just(routeDefinition)).block();


//        .subscribe();

//        Route.async(routeDefinition).asyncPredicate(predicate)
//                .replaceFilters(gatewayFilters).build();


    }
}
