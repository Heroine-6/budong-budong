package com.example.budongbudong.integration.chatServer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatServerResponse {

    private Long propertyId;
    private Long sellerId;
    private Long bidderId;
    private String propertyName;
    private String propertyAddress;
    private String bidderName;
}
