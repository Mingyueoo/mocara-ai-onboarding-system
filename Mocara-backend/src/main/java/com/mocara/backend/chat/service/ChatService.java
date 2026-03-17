package com.mocara.backend.chat.service;

import com.mocara.backend.api.v1.dto.ChatMessageDto;
import com.mocara.backend.chat.entity.ChatMessageEntity;
import com.mocara.backend.chat.mapper.ChatMapper;
import com.mocara.backend.chat.repo.ChatMessageRepository;
import com.mocara.backend.session.repo.PatientSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final PatientSessionRepository patientSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMapper chatMapper;
    private final MockChatResponder mockChatResponder;

    public ChatService(
            PatientSessionRepository patientSessionRepository,
            ChatMessageRepository chatMessageRepository,
            ChatMapper chatMapper,
            MockChatResponder mockChatResponder
    ) {
        this.patientSessionRepository = patientSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatMapper = chatMapper;
        this.mockChatResponder = mockChatResponder;
    }

    @Transactional
    public ChatMessageDto sendMessage(UUID sessionId, String input, List<ChatMessageDto> context) {
        var session = patientSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // persist user message (optional for client, helpful for audit)
        ChatMessageEntity user = new ChatMessageEntity();
        user.setId(UUID.randomUUID());
        user.setSession(session);
        user.setRole(com.mocara.backend.common.enums.UserRole.PATIENT);
        user.setContent(input);
        user.setTimestampMs(System.currentTimeMillis());
        user.setEscalated(false);
        user.setAvatarEmotion(com.mocara.backend.common.enums.AvatarEmotion.NEUTRAL);
        chatMessageRepository.save(user);

        ChatMessageDto reply = mockChatResponder.generateResponse(input, session.getDrugId(), context);

        ChatMessageEntity assistant = chatMapper.fromDto(reply);
        assistant.setSession(session);
        chatMessageRepository.save(assistant);

        return reply;
    }
}

