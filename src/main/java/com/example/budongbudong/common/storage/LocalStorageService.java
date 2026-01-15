package com.example.budongbudong.common.storage;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 로컬 파일 시스템에 파일을 저장하는 StorageService 구현체
 *
 * 개발 및 테스트 환경에서 사용하기 위한 구현체
 * 실제 운영 환경에서는 AWS S3, GCP Storage 등으로 교체될 수 있음
 *
 * 비즈니스 로직은 이 클래스가 아닌 StorageService 인터페이스에만 의존하도록 설계
 * 저장 방식이 변경되더라도 서비스 로직 수정이 최소화
 */
@Service
public class LocalStorageService implements StorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    public String upload(MultipartFile file, String directory) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir, directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(savedFilename);
            Files.copy(file.getInputStream(), filePath);

            return baseUrl + "/" + directory + "/" + savedFilename;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String filePath = fileUrl.replace(baseUrl, uploadDir);
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}
