package org.qualifaizebackendapi.security;

import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.exception.CustomAccessDeniedHandler;
import org.qualifaizebackendapi.exception.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final QualifAIzeUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // ===============================================
                        // PUBLIC ENDPOINTS (No Authentication Required)
                        // ===============================================
                        .requestMatchers(
                                "/api/v1/user/auth/**",           // Registration & Login
                                "/v3/api-docs/**",                // OpenAPI docs
                                "/swagger-ui/**",                 // Swagger UI
                                "/swagger-ui.html",               // Swagger UI HTML
                                "/",                              // Root path
                                "/error"                          // Error handling
                        ).permitAll()
                        // ===============================================
                        // USER MANAGEMENT ENDPOINTS
                        // ===============================================
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/me").hasAnyRole("GUEST", "USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/user/**").hasAnyRole("GUEST", "USER", "ADMIN")
                        // Only admins can view all users, delete and promote them
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/promote/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/user/**").hasRole("ADMIN")
                        // ===============================================
                        // DOCUMENT/PDF MANAGEMENT ENDPOINTS
                        // ===============================================
                        // All PDF endpoints require admin role
                        .requestMatchers("/api/v1/pdf/**").hasRole("ADMIN")
                        // ===============================================
                        // INTERVIEW MANAGEMENT ENDPOINTS
                        // ===============================================
                        .requestMatchers(HttpMethod.POST, "/api/v1/interview").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/interview/assigned").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/interview/next/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/interview/answer/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/interview/*").hasAnyRole("USER", "ADMIN")
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration aConfig) throws Exception {
        return aConfig.getAuthenticationManager();
    }
}
