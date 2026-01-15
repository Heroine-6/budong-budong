package com.example.budongbudong.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 정적 리소스(이미지 등) 접근 설정을 담당하는 Web 설정 클래스
 * S3 등 외부 스토리지를 사용할 경우 이 설정은 필요 없어질 수 있음
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // /uploads/properties/abc.jpg 이 구조로 저장됨
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
