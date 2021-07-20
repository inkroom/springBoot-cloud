package cn.inkroom.study.cloud.fund;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author inkbox
 * @date 2021/7/15
 */
@RestController
@RequestMapping("fund/feign")
@RefreshScope
public class FeignController {
    @Autowired
    private UserFacade userFacade;
    @Value("${feign.circuitbreaker.enabled:empty}")
    private String demo;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("user/{id}")
    public String user(@PathVariable Long id) {
        logger.info("调用前{}", id);
        System.out.println(demo);
        String user = userFacade.getUser(id);
        logger.info("调用{}后{}", id, user);
        return user + "---" + demo;
    }

    @GetMapping("timeout")
    public String timeout() {
        return userFacade.timeout();
    }

    @GetMapping("temp")
    public String temp(){
        logger.info("demo={}",demo);
        return demo;
    }
}
