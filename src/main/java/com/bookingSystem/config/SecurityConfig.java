package com.bookingSystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig
{
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable()
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                .requestMatchers("/api/auth/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                                // FOR User Management
                                // ADMIN
                                .requestMatchers(HttpMethod.GET,
                                        "/api/booking/users").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET,
                                        "/api/booking/users/*").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.PUT,
                                        "/api/booking/users/*").hasAnyRole("ADMIN","USER")
                                .requestMatchers(HttpMethod.DELETE,
                                        "/api/booking/users/*").hasRole("ADMIN")

                                // Resource Management
                                // USER & ADMIN
                                .requestMatchers(HttpMethod.GET,
                                        "/api/booking/resources").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.GET,
                                        "/api/booking/resources/*").hasAnyRole("ADMIN", "USER")
                                // ADMIN ONLY
                                .requestMatchers(HttpMethod.POST,
                                        "/api/booking/resources").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT,
                                        "/api/booking/resources/*").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE,
                                        "/api/booking/resources/*").hasRole("ADMIN")

                                // Reservation Management
                                // USER & ADMIN
                                .requestMatchers(HttpMethod.GET,
                                        "/api/booking/reservations").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET,
                                        "/api/booking/reservations/*").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.GET,
                                        "/api/booking/reservations/users/**").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.POST,
                                        "/api/booking/reservations").hasAnyRole("ADMIN","USER")
                                // ADMIN ONLY
                                .requestMatchers(HttpMethod.PUT,
                                        "/api/booking/reservations/*").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE,
                                        "/api/booking/reservations/*").hasRole("ADMIN")
                                .anyRequest().authenticated()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

}
