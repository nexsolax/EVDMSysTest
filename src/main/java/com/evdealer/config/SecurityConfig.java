package com.evdealer.config;

import com.evdealer.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**",
                                 // springdoc default
                                 "/v3/api-docs",
                                 "/v3/api-docs/**",
                                 "/v3/api-docs/swagger-config",
                                 // springdoc overridden path (see application.properties)
                                 "/api-docs",
                                 "/api-docs/**",
                                 "/api-docs/swagger-config",
                                 "/swagger-ui/**",
                                 "/swagger-ui.html").permitAll()
                // Public access for customers to view and purchase vehicles
                .requestMatchers("/",
                                 "/catalog",
                                 "/promotions",
                                 "/register",
                                 "/quotation",
                                 "/order",
                                 "/feedback",
                                 "/appointment",
                                 "/search",
                                 "/inventory/**",
                                 "/brands/**",
                                 "/models/**",
                                 "/variants/**",
                                 "/uploads/**",
                                 "/colors/**",
                                 "/api/public/**",
                                 "/api/vehicles/**",
                                 "/api/vehicle-brands/**",
                                 "/api/vehicle-models/**", 
                                 "/api/vehicle-variants/**",
                                 "/api/vehicle-colors/**",
                                 "/api/vehicle-inventory/**",
                                 "/api/promotions/**",
                                 "/api/quotations/**",
                                 "/api/images/upload/**").permitAll()
                // Admin and management endpoints require authentication
                .requestMatchers("/api/users/**",
                                 "/api/dealers/**",
                                 "/api/pricing-policies/**",
                                 "/api/installment-plans/**",
                                 "/api/dealer-targets/**",
                                 "/api/reports/**",
                                 "/api/warehouses/**",
                                 "/api/vehicle-deliveries/**",
                                 "/api/sales-contracts/**",
                                 "/api/inventory-management/**",
                                 "/api/customer-payments/**",
                                 "/api/dealer-payments/**",
                                 "/api/images/**",
                                 "/api/orders/**",
                                 "/api/customers/**",
                                 "/api/feedbacks/**",
                                 "/api/appointments/**",
                                 "/api/dealer-orders/**",
                                 "/api/dealer-quotations/**",
                                 "/api/dealer-invoices/**").authenticated()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized: " + authException.getMessage() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String authInfo = auth != null ? "User: " + auth.getName() + ", Authorities: " + auth.getAuthorities() : "No authentication";
                    response.getWriter().write("{\"error\":\"Access Denied\",\"authInfo\":\"" + authInfo + "\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

