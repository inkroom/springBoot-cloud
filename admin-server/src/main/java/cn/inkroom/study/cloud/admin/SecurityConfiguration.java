package cn.inkroom.study.cloud.admin;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.boot.autoconfigure.security.servlet.SpringBootWebSecurityConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.WebSecurityEnablerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * @author inkbox
 * @date 2021/7/18
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private String adminContextPath;

    public SecurityConfiguration(AdminServerProperties adminContextPath) {
        this.adminContextPath = adminContextPath.getContextPath();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setTargetUrlParameter("redirectTo");
        http.authorizeRequests()
                .antMatchers(adminContextPath+"/assets/**").permitAll()
                .antMatchers(adminContextPath+"/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage(adminContextPath+"/login")
                .successHandler(handler)
                .and()
                .logout().logoutUrl(adminContextPath+"/logout")
                .and()
                .httpBasic()
                .and()
                .csrf().disable();



    }
}
