package com.example.budongbudong.common.config;

import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.filter.JwtFilter;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> {
                    commonAuth(auth);
                    propertyAuth(auth);
                    auctionAuth(auth);
                    bidAuth(auth);
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            writeErrorResponse(response, ErrorCode.LOGIN_REQUIRED);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            writeErrorResponse(response, ErrorCode.FORBIDDEN);
                        })
                )
                .addFilterBefore(jwtFilter, SecurityContextHolderAwareRequestFilter.class)
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json;charset=UTF-8");

        GlobalResponse<Void> body =
                GlobalResponse.exception(false, errorCode, null);

        new ObjectMapper().writeValue(response.getWriter(), body);
    }

    private void commonAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/signin").permitAll();
    }

    private void propertyAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/properties", "/api/v1/properties/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/properties/my")
            .hasRole(UserRole.SELLER.name())

            .requestMatchers(HttpMethod.POST, "/api/v1/properties")
            .hasAnyRole(UserRole.SELLER.name(), UserRole.ADMIN.name())

            .requestMatchers(HttpMethod.PATCH, "/api/v1/properties/*")
            .hasAnyRole(UserRole.SELLER.name(), UserRole.ADMIN.name())

            .requestMatchers(HttpMethod.DELETE, "/api/v1/properties/*")
            .hasAnyRole(UserRole.SELLER.name(), UserRole.ADMIN.name());
    }

    private void auctionAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.POST, "/api/v1/auctions")
            .hasRole(UserRole.SELLER.name())

            .requestMatchers(HttpMethod.PATCH, "/api/v1/auctions/*/status")
            .hasAnyRole(UserRole.SELLER.name(), UserRole.ADMIN.name())

            .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/auctions/*/statistics",
                    "/api/v1/auctions/*/info"
            ).permitAll();
    }

    private void bidAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.GET, "/api/v1/bids/auctions/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/bids")
            .hasRole(UserRole.GENERAL.name())

            .requestMatchers(HttpMethod.GET, "/api/v1/bids/my")
            .hasRole(UserRole.GENERAL.name());
    }

}
