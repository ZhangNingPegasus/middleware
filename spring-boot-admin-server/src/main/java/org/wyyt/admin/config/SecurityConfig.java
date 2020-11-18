package org.wyyt.admin.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private AdminServerProperties adminServerProperties;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String path = adminServerProperties.getContextPath();

        SavedRequestAwareAuthenticationSuccessHandler successHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");

        http.authorizeRequests()
                //静态资源和login以及健康检查
                .antMatchers(path + "/assets/**").permitAll()
                .antMatchers(path + "/login").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage(path + "/login")
                .successHandler(successHandler)
                .and().logout().logoutUrl(path + "/logout")
                .and().httpBasic().and().csrf().disable();

    }
}