package cn.inkroom.study.cloud.fund;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author inkbox
 * @date 2021/7/15
 */
@Component
public class UserFallbackFactory implements FallbackFactory<UserFacade> {

    @Autowired
    private UserFallback fallback;

    @Override
    public UserFacade create(Throwable cause) {
        System.out.println("异常了" + cause);
        return fallback;
    }
}
