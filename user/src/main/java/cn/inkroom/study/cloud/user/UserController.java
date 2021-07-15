package cn.inkroom.study.cloud.user;

import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * @author inkbox
 * @date 2021/7/14
 */
@RestController
@RequestMapping("user")
public class UserController {


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
        System.out.println("获取user信息" + id);
        return "user";
    }

    @PutMapping("detail")
    public String update() {
        System.out.println("更新用户信息");
        return "user";
    }

}
