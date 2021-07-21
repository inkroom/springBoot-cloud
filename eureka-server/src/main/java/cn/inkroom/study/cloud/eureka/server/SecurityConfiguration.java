package cn.inkroom.study.cloud.eureka.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.server.EurekaDashboardProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * @author inkbox
 * @date 2021/7/18
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private String adminContextPath;

    @Value("${eureka.dashboard.path:}")
    private String dashboardPath = "";

    public SecurityConfiguration(EurekaDashboardProperties adminContextPath) {
        this.adminContextPath = adminContextPath.getPath();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setTargetUrlParameter("redirectTo");
        http.authorizeRequests()
                .antMatchers(adminContextPath + "/assets/**", "/assets/**").permitAll()
                .antMatchers(adminContextPath + "/login/assets/**").permitAll()
                .antMatchers(adminContextPath + "/login").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/error").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage(adminContextPath + "/login")
                .defaultSuccessUrl(adminContextPath + "/")
//                .loginProcessingUrl(adminContextPath + "/login")
//                .successHandler(handler)
                .and()
                .logout().logoutUrl(adminContextPath + "/logout")
                .and()
                .httpBasic()
                .and()
                .csrf().disable();


    }
}
