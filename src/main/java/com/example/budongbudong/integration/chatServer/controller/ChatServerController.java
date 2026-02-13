package com.example.budongbudong.integration.chatServer.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.integration.chatServer.dto.response.ChatServerResponse;
import com.example.budongbudong.integration.chatServer.service.ChatServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/chat")
public class ChatServerController {

    private final ChatServerService chatServerService;

    @GetMapping("/v2/{propertyId}")
    public ResponseEntity<GlobalResponse<ChatServerResponse>> getChatContext(@PathVariable Long propertyId, @AuthenticationPrincipal AuthUser authUser) {

        ChatServerResponse response = chatServerService.getChatContext(propertyId, authUser.getUserId());

        return GlobalResponse.ok(response);
    }
}
