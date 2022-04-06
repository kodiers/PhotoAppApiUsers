package com.tfl.photoappapiusers.security;

import com.tfl.photoappapiusers.service.UsersService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final Environment environment;
    private final UsersService usersService;
    private final BCryptPasswordEncoder passwordEncoder;

    public WebSecurity(Environment environment, UsersService usersService, BCryptPasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/**")
                .hasIpAddress(environment.getProperty("gateway.ip"))
                .antMatchers(HttpMethod.GET, "/actuator/health")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/actuator/circuitbreakerevents")
                .permitAll()
                .and()
                .addFilter(getAuthenticationFilter())
                .headers()
                .frameOptions()
                .disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(usersService)
                .passwordEncoder(passwordEncoder);
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(usersService, environment,
                authenticationManager());
        authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url.path"));
        return authenticationFilter;
    }
}
