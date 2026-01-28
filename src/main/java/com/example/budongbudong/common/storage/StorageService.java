package com.example.budongbudong.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 *  파일 저장소 추상화 인터페이스
 *  로컬 파일 시스템, AWS S3 등 다양한 저장소 구현체를 유연하게 교체하기 위해 만듦.
 *
 *  비즈니스 로직(Service, Domain)은 실제 저장 방식에 의존하지 않고
 *  이 인터페이스를 통해 파일 업로드/삭제를 요청한다.
 *
 *  - [어디에 어떻게 저장되는지?]는 구현체(LocalStorageService, S3StorageService 등)가 책임지고
 *  - [저장해줘] 라는 요청만 받는 역할
 */
public interface StorageService {
    String upload(MultipartFile file, String directory);
    void delete(String fileUrl);
}
