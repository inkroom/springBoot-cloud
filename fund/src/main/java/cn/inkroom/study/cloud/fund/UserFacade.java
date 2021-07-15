package cn.inkroom.study.cloud.fund;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author inkbox
 * @date 2021/7/15
 */
@FeignClient(value = "user", fallbackFactory = UserFallbackFactory.class)
public interface UserFacade {

    @GetMapping("/user/detail/{id}")
    String getUser(@PathVariable Long id);


    @GetMapping("/user/timeout")
    String timeout();

}
