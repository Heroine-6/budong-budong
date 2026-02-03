package com.example.budongbudong.integration.chatServer.service;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import com.example.budongbudong.integration.chatServer.dto.request.ChatServerRequest;
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
    public ChatServerResponse getChatContext(ChatServerRequest request) {

        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User bidder = userRepository.findById(request.getBidderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        if (!property.getUser().getId().equals(seller.getId())) {
            throw new CustomException(ErrorCode.SELLER_NOT_MATCH);
        }

        return new ChatServerResponse(property.getName(), property.getAddress(), bidder.getName());
    }
}
