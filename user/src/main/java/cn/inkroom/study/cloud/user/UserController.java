package cn.inkroom.study.cloud.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * @author inkbox
 * @date 2021/7/14
 */
@RestController
@RequestMapping("user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("timeout")
    public String timeout() {
        try {
            int abs = Math.abs(new Random().nextInt() % 1500);
            System.out.println("超时：" + abs);
            Thread.sleep(abs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "正常";
    }

    @GetMapping("exception")
    public String exception() {

        throw new RuntimeException("异常了");

    }


    @GetMapping("detail/{userId}")
    public String detail(@PathVariable("userId") Long id) {
        logger.info("要获取{}的数据", id);
        System.out.println("获取user信息" + id);
        logger.info("假设已经获取完了数据{}", id);
        return "user";
    }

    @PutMapping("detail")
    public String update() {
        System.out.println("更新用户信息");
        return "user";
    }

}
