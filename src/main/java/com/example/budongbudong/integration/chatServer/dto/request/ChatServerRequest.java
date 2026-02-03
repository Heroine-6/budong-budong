package com.example.budongbudong.integration.chatServer.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatServerRequest {

    @NotNull
    private Long propertyId;

    @NotNull
    private Long sellerId;

    @NotNull
    private Long bidderId;
}
