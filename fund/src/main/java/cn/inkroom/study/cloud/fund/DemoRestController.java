package cn.inkroom.study.cloud.fund;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author inkbox
 * @date 2021/7/13
 */
@RestController
@RequestMapping("fund")
public class DemoRestController {
    @Autowired
    private HttpServletRequest request;

    @PostMapping("/account/balance/{userId}/{amount}")
    public String deductingBalance() {

        return "端口:" + request.getServerPort();
    }


}
