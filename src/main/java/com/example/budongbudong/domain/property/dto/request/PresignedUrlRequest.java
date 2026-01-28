package com.example.budongbudong.domain.property.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PresignedUrlRequest {

    @NotEmpty(message = "파일 정보는 필수입니다.")
    private List<@Valid FileInfo> files;

    @Getter
    @NoArgsConstructor
    public static class FileInfo {
        @NotBlank(message = "파일 이름은 필수입니다.")
        private String fileName;

        private String contentType;
    }
}
