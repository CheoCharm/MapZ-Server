package com.cheocharm.MapZ.common.config;

import com.cheocharm.MapZ.common.jwt.JwtAuthenticationFilter;
import com.cheocharm.MapZ.common.jwt.JwtAuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final AuthenticationEntryPointCustom authenticationEntryPointCustom;

    @Override
    public void configure(WebSecurity web) throws Exception {

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .anyRequest().hasAuthority("ROLE_USER")
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPointCustom);

         http.addFilterBefore(new JwtAuthenticationFilter(jwtAuthenticationUtils), UsernamePasswordAuthenticationFilter.class);
    }

}
