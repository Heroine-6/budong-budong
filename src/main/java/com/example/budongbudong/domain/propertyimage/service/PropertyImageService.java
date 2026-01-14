package com.example.budongbudong.domain.propertyimage.service;

import com.example.budongbudong.domain.propertyimage.repository.PropertyImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;
}
