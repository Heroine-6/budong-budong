package com.example.budongbudong.common.storage;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalPresignedUrlService implements PresignedUrlService {

    /**
     * storage.type=local일 때도 PresignedUrlService 빈이 존재하도록 만드는 역할
     * 그래서 /images/presign 호출 시 “빈 없음”으로 터지는 대신
     * S3_NOT_CONFIGURED 같은 명확한 에러를 반환
     */
    @Override
    public PresignedUrlInfo createPresignedUpload(String directory, String originalFilename, String contentType) {
        throw new CustomException(ErrorCode.S3_NOT_CONFIGURED);
    }
}
