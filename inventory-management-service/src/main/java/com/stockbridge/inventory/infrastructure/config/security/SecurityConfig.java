package com.stockbridge.inventory.infrastructure.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
                                    RestSecurityErrorHandler errorHandler) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())     // API stateless con token en header, sin cookies
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/auth/**", "/actuator/health", "/actuator/info").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                        .requestMatchers("/api/products/**").hasRole("ADMIN")

                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/movements/**").hasAnyRole("BODEGA", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/requests/*/approve",
                                "/api/requests/*/reject", "/api/requests/*/deliver").hasAnyRole("BODEGA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/requests/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/requests").hasAnyRole("BODEGA", "ADMIN")
                        .requestMatchers("/api/requests/**").authenticated()   // create, get por id, cancel (la propiedad se valida en el caso de uso)

                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}