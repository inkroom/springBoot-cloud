package cn.inkroom.study.cloud.product;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheKey;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @author inkbox
 * @date 2021/7/14
 */
@Service
public class UserFacadeImpl implements UserFacade {
    @Autowired
    private RestTemplate template;

    @Override
    @HystrixCommand(fallbackMethod = "timeoutFallback")
    public String timeout() {

        String r = template.getForObject("http://USER/user/timeout", String.class);

        return r;
    }

    @Override
    @HystrixCommand(fallbackMethod = "exceptionFallback", ignoreExceptions = NullPointerException.class)
    public String exception() {

        throw new NullPointerException("exception test");
//        String r = template.getForObject("http://USER/user/exception", String.class);
//
//        return r;
    }

    @HystrixCommand(fallbackMethod = "cacheFallback")
    @CacheResult
    @Override
    public String getUser(@CacheKey Long id) {
        System.out.println("getUser:" + id);
        return template.getForObject("http://USER/user/detail/{userId}", String.class, 1);
    }

    @HystrixCommand(fallbackMethod = "cacheFallback")
    @CacheRemove(commandKey = "getUser")
    @Override
    public String updateUser(@CacheKey Long id) {
        template.put("http://USER/user/detail", null);
        return "" + id;
    }

    public String cacheFallback(Long id) {

        return "cacheFallback:" + id;
    }

    public String timeoutFallback() {
        return "timeoutFallback";
    }

    public String exceptionFallback(
//            String r, Throwable throwable
    ) {
//        System.out.println(r);
//        System.out.println(throwable);
        return "exceptionFallback";
    }
}
