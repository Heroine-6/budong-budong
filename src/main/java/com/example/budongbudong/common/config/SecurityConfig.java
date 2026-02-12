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
    private final ObjectMapper objectMapper;

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
                    realDealAuth(auth);
                    paymentAuth(auth);
                    chatServerAuth(auth);
                    notificationAuth(auth);
                    userAuth(auth);
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
                GlobalResponse.exception(errorCode, null);

        objectMapper.writeValue(response.getWriter(), body);
    }

    private void commonAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/v1/signup", "/api/auth/v1/signin", "/api/auth/v1/refresh", "/api/auth/v1/send", "/api/auth/v1/verify", "/api/auth/v2/kakao").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                // Swagger / OpenAPI 허용
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()

                // Elasticsearch 동기화
                .requestMatchers("/api/v1/properties/sync").permitAll()

                // 서버 health check
                .requestMatchers("/actuator/**").permitAll()

                //html
                .requestMatchers(
                        "/paymentRequest.html",
                        "/success.html",
                        "/fail.html",
                        "/",
                        "/index.html",
                        "/budongbudong",
                        "/search",
                        "/search.html",
                        "/signin",
                        "/signin.html",
                        "/signup",
                        "/signup.html",
                        "/payments.html"
                ).permitAll();
    }

    private void propertyAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.POST, "/api/v1/properties/lookup").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v1/properties", "/api/v1/properties/*").permitAll()
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
        auth.requestMatchers(HttpMethod.POST, "/api/auctions/v1", "/api/auctions/v3/dutch")
                .hasRole(UserRole.SELLER.name())

                .requestMatchers(HttpMethod.PATCH, "/api/auctions/v1*")
                .hasAnyRole(UserRole.SELLER.name(), UserRole.ADMIN.name())

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/auctions/v1/*/statistics",
                        "/api/auctions/v1/*/info"
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

    private void realDealAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        // 실거래가 검색 API - 비로그인 허용
        auth.requestMatchers(HttpMethod.GET, "/api/v2/real-deals/**").permitAll();
    }

    private void paymentAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.POST, "/api/payments/v2/auctions/**")
                .hasAnyRole(UserRole.GENERAL.name(), UserRole.ADMIN.name())
                .requestMatchers(HttpMethod.GET, "/api/payments/v2", "/api/payments/v2/*")
                .hasAnyRole(UserRole.GENERAL.name(), UserRole.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/payments/v2/*/refund")
                .hasAnyRole(UserRole.GENERAL.name(), UserRole.ADMIN.name());
    }

    private void chatServerAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.GET, "/api/v2/internal/**")
                .hasRole(UserRole.GENERAL.name());
    }

    private void notificationAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.GET, "/api/notifications/v2/my")
                .hasAnyRole(UserRole.GENERAL.name(), UserRole.SELLER.name(), UserRole.ADMIN.name());
    }

    private void userAuth(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.PATCH, "/api/users/v2/notifications")
                .hasAnyRole(UserRole.GENERAL.name(), UserRole.SELLER.name(), UserRole.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/users/v2/kakao/link")
                .hasAnyRole(UserRole.GENERAL.name(), UserRole.SELLER.name(), UserRole.ADMIN.name());
    }
}
