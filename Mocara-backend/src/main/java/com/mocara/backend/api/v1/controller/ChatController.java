package com.mocara.backend.api.v1.controller;

import com.mocara.backend.api.v1.dto.ChatMessageDto;
import com.mocara.backend.api.v1.dto.ChatSendRequestDto;
import com.mocara.backend.auth.security.CurrentUserProvider;
import com.mocara.backend.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserProvider currentUserProvider;

    public ChatController(ChatService chatService, CurrentUserProvider currentUserProvider) {
        this.chatService = chatService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/messages")
    public ChatMessageDto send(@Valid @RequestBody ChatSendRequestDto request) {
        return chatService.sendMessage(request.sessionId(), request.input(), request.context(), currentUserProvider.currentUser());
    }
}

