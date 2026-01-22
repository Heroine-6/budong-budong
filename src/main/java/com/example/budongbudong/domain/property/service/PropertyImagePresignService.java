package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.storage.PresignedUrlInfo;
import com.example.budongbudong.common.storage.PresignedUrlService;
import com.example.budongbudong.domain.property.dto.request.PresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyImagePresignService {

    private static final String DIRECTORY = "properties";

    private final PresignedUrlService presignedUrlService;

    public List<PresignedUrlInfo> issuePresignedUrls(PresignedUrlRequest request) {
        List<PresignedUrlRequest.FileInfo> files = request.getFiles();

        List<PresignedUrlInfo> results = new ArrayList<>();
        for (PresignedUrlRequest.FileInfo file : files) {
            results.add(presignedUrlService.createPresignedUpload(
                    DIRECTORY,
                    file.getFileName(),
                    file.getContentType()
            ));
        }
        return results;
    }
}
