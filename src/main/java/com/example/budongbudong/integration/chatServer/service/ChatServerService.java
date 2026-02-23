package com.example.budongbudong.integration.chatServer.service;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import com.example.budongbudong.integration.chatServer.dto.response.ChatServerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServerService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Transactional(readOnly = true)
    public ChatServerResponse getChatContext(Long propertyId, Long bidderId) {

        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        return new ChatServerResponse(propertyId, property.getUser().getId(), bidderId, property.getName(), property.getAddress(), bidder.getName());
    }
}
