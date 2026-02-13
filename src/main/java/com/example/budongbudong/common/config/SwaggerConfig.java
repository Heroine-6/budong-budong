package com.example.budongbudong.common.config;

import com.example.budongbudong.common.utils.annotation.SecurityNotRequired;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("부동부동 API")
                        .description("""
                                ## 인증 방법
                                1. `인증` 탭 → `/api/auth/v1/signin` 으로 로그인
                                2. 응답의 `accessToken` 값 복사
                                3. 우측 상단 **Authorize 🔓** 버튼 클릭
                                4. 토큰값만 입력 (`Bearer ` 없이) → Authorize
                                """)
                        .version("v1.0.0")
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

                // 태그 순서
                .tags(List.of(
                        new Tag().name("인증").description("회원가입 · 로그인 · 토큰 갱신 · 카카오 소셜"),
                        new Tag().name("매물").description("매물 등록 · 조회 · 수정 · 삭제"),
                        new Tag().name("경매").description("일반 경매 · 네덜란드식 경매 등록 및 조회"),
                        new Tag().name("입찰").description("일반 입찰 · 네덜란드식 입찰 · 내 입찰 내역"),
                        new Tag().name("결제").description("결제 요청 · 승인 · 환불"),
                        new Tag().name("알림").description("내 알림 목록 조회"),
                        new Tag().name("사용자").description("알림 설정 · 카카오 연동"),
                        new Tag().name("실거래가").description("주변 시세 검색 · 입찰가 비교"),
                        new Tag().name("채팅").description("채팅 서버 연동")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 후 발급받은 accessToken을 입력하세요. 'Bearer ' 접두사 없이 토큰만 입력하면 됩니다.")
                        )
                );
    }

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            SecurityNotRequired annotation = handlerMethod.getMethodAnnotation(SecurityNotRequired.class);
            // SecurityNotRequire 어노테이션있을시 스웨거 시큐리티 설정 삭제
            if (annotation != null) {
                operation.security(Collections.emptyList());
            }
            return operation;
        };
    }
}