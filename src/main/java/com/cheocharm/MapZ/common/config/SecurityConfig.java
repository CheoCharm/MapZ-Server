package com.cheocharm.MapZ.common.config;

import com.cheocharm.MapZ.common.jwt.JwtAuthenticationFilter;
import com.cheocharm.MapZ.common.jwt.JwtAuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                    //users
                    .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .antMatchers(HttpMethod.POST,"/api/users/login").permitAll()
                    .antMatchers(HttpMethod.GET,"/api/users/valid/email/**").permitAll()
                    .antMatchers(HttpMethod.POST,"/api/users/signup").permitAll()
                    .antMatchers(HttpMethod.POST,"/api/users/signin").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/users/password/**").permitAll()
                    .antMatchers(HttpMethod.PATCH,"/api/users/password/").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/users/user").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/users/mypage").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/users/refresh").authenticated()

                    //group
                    .antMatchers(HttpMethod.POST, "/api/group").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/group").authenticated()
                    .antMatchers(HttpMethod.PATCH, "/api/group/status").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/group/join").authenticated()
                    .antMatchers(HttpMethod.PATCH, "/api/group/join").authenticated()
                    .antMatchers(HttpMethod.PATCH, "/api/group/exit").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/group/invite").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/group/mygroup").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/group/member").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/api/group/user").authenticated()

                    //diary
                    .antMatchers(HttpMethod.POST, "/api/diary/like").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/diary").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/diary").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/api/diary").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/diary/like").authenticated()
                    .antMatchers(HttpMethod.GET, "/api/diary/mylike").authenticated()
                    .antMatchers(HttpMethod.GET,"/api/diary/my").authenticated()

                    //commnet
                    .antMatchers(HttpMethod.POST, "/api/comment").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/api/comment").authenticated()

                    //actuator
                    .antMatchers("/actuator/health").permitAll()
                    .anyRequest().denyAll()
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPointCustom);

         http.addFilterBefore(new JwtAuthenticationFilter(jwtAuthenticationUtils, handlerExceptionResolver), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
