package com.example.budongbudong.common.storage;

// 컨트롤러/서비스가 s3에 의존안하게끔
// storage.type에 따라 자동으로 다른 구현이 선택되게 만드는 역할
public interface PresignedUrlService {
    PresignedUrlInfo createPresignedUpload(String directory, String originalFilename, String contentType);
}
