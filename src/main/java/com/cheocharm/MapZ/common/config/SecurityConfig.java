package com.cheocharm.MapZ.common.config;

import com.cheocharm.MapZ.common.jwt.JwtAuthenticationFilter;
import com.cheocharm.MapZ.common.jwt.JwtAuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final AuthenticationEntryPointCustom authenticationEntryPointCustom;
    private final HandlerExceptionResolver handlerExceptionResolver;

    private static final String[] IGNORE_URI = {
            "/v3/api-docs/**",
            "/mapz.html/**",
            "/swagger-ui/**"
    };

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(IGNORE_URI);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers("/mapz.html/**").permitAll()
                    .antMatchers("/swagger-ui/**").permitAll()
                .and()
                .authorizeRequests()
                //????????? ??????
                    .antMatchers("/board/**").hasAuthority("ROLE_USER")
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPointCustom);

         http.addFilterBefore(new JwtAuthenticationFilter(jwtAuthenticationUtils, handlerExceptionResolver), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
