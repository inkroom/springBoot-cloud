package cn.inkroom.study.cloud.fund;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class FeignController {
    @Autowired
    private UserFacade userFacade;
    @Value("${feign.circuitbreaker.enabled}")
    private String demo;

    @GetMapping("user/{id}")
    public String user(@PathVariable Long id) {
        System.out.println(demo);
        return userFacade.getUser(id) + "---" + demo;
    }

    @GetMapping("timeout")
    public String timeout() {
        return userFacade.timeout();
    }

}
