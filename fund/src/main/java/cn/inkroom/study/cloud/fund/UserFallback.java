package cn.inkroom.study.cloud.fund;

import org.springframework.stereotype.Component;

/**
 * @author inkbox
 * @date 2021/7/15
 */
@Component
public class UserFallback implements UserFacade{
    @Override
    public String getUser(Long id) {
        System.out.println("降级处理");
        return null;
    }

    @Override
    public String timeout() {
        System.out.println("timeout降级");
        return "timeout降级";
    }
}
