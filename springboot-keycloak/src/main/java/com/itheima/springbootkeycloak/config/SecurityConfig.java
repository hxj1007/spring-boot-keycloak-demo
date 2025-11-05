package com.itheima.springbootkeycloak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityConfig() {
        System.out.println("=== SecurityConfig 被加载 ===");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("=== 配置 SecurityFilterChain ===");

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/favicon.ico", "/css/**", "/js/**", "/debug", "/test", "/oauth-test").permitAll()
                        .requestMatchers("/nice").authenticated() // 暂时改为只需要认证，不需要特定权限
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }
}