package cn.inkroom.study.cloud.product;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author inkbox
 * @date 2021/7/13
 */
@RestController
@RequestMapping("product")
public class ProductController {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserFacade userFacade;

    @GetMapping("/purchase/{userId}/{productId}/{amount}")
    public String purchaseProduct(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @PathVariable int amount
    ) {

        String url = "http://FUND/fund/account/balance/{userId}/{amount}";
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("amount", amount);

        String s = restTemplate.postForObject(url, null, String.class, map);
        System.out.println("found的结果=" + s);
        return s;
    }

    @GetMapping("timeout")
    public String timeout() {
        return userFacade.timeout();
    }

    @GetMapping("exception")
    public String exception() {
        return userFacade.exception();
    }

    @GetMapping("cache")
    public String cache() {

        HystrixRequestContext hystrixRequestContext = HystrixRequestContext.initializeContext();

        userFacade.getUser(1L);
        userFacade.getUser(1L);
        userFacade.getUser(2L);
        userFacade.updateUser(1L);
        userFacade.getUser(1L);

        hystrixRequestContext.close();

        return "res";

    }

}

