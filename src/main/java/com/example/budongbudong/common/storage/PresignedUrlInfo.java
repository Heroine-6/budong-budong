package com.example.budongbudong.common.storage;

/**
 * @param uploadUrl 클라이언트가 파일을 업로드할 Presigned URL
 * @param fileUrl 업로드 완료 후 접근할 파일 URL
 */
public record PresignedUrlInfo(
        String uploadUrl,
        String fileUrl
) {
}
