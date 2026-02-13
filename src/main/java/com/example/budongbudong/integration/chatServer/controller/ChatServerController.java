package com.example.budongbudong.integration.chatServer.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.integration.chatServer.dto.response.ChatServerResponse;
import com.example.budongbudong.integration.chatServer.service.ChatServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "채팅")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/internal/chat")
public class ChatServerController {

    private final ChatServerService chatServerService;

    @Operation(summary = "채팅 컨텍스트 조회", description = "매물 채팅방 입장에 필요한 컨텍스트 정보를 반환합니다. (GENERAL 권한 필요)")
    @GetMapping("/{propertyId}")
    public ResponseEntity<GlobalResponse<ChatServerResponse>> getChatContext(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        ChatServerResponse response = chatServerService.getChatContext(propertyId, authUser.getUserId());

        return GlobalResponse.ok(response);
    }
}