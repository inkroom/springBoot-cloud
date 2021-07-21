package cn.inkroom.study.cloud.eureka.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 *
 * 用来解决加入security之后静态资源无法访问问题
 *
 * @author inkbox
 * @date 2021/7/21
 */
@Configuration
public class WebMvcController extends WebMvcConfigurationSupport {

    @Value("${eureka.dashboard.path:}")
    private String dashboardPath = "";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //
        registry.addResourceHandler(dashboardPath + "/assets/login/**").addResourceLocations("classpath:/templates/login/assets/");
        registry.addResourceHandler(dashboardPath+"/assets/eureka/**").addResourceLocations("classpath:/static/eureka/");
    }
}
