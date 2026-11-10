package com.datn.pc_store.modules.auth.config;

import com.datn.pc_store.modules.auth.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigin;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String reviews = "/api/products/{productId}/reviews";
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(a -> a
                        // Public authentication and API documentation.
                        .requestMatchers("/api/auth/login", "/api/auth/register",
                                "/api/auth/refresh", "/api/auth/logout",
                                "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Authorize reviews before the broader public catalog matcher.
                        // ReviewService checks the completed purchase and review ownership on writes.
                        .requestMatchers(HttpMethod.GET, reviews, reviews + "/{id}").permitAll()
                        .requestMatchers(reviews, reviews + "/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/categories/**", "/api/brands/**", "/api/products/**").permitAll()

                        // Administrative catalog, users, suppliers and warranty policies.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Purchase orders: warehouse staff can read, managers can also modify.
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders", "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF_WAREHOUSE")
                        .requestMatchers("/api/purchase-orders", "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // Warehouse documents and deliveries.
                        .requestMatchers("/api/goods-receipts", "/api/goods-receipts/**",
                                "/api/purchase-returns", "/api/purchase-returns/**",
                                "/api/deliveries", "/api/deliveries/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF_WAREHOUSE")
                        .requestMatchers("/api/product-serials", "/api/product-serials/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF_WAREHOUSE", "STAFF_TECH", "STAFF_SALES")

                        // Warranty lookup and intake.
                        .requestMatchers("/api/warranties", "/api/warranties/**",
                                "/api/warranty-requests", "/api/warranty-requests/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STAFF_SALES")

                        // Orders: ownership and allowed transitions are checked by OrderService.
                        .requestMatchers(HttpMethod.DELETE, "/api/orders", "/api/orders/**")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/orders", "/api/orders/**").authenticated()

                        // Remaining auth operations and comment writes require a logged-in user.
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigin);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}
