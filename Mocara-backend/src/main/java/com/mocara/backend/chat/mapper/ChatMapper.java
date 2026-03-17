package com.mocara.backend.chat.mapper;

import com.mocara.backend.api.v1.dto.ChatMessageDto;
import com.mocara.backend.chat.entity.ChatMessageEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChatMapper {

    public ChatMessageDto toDto(ChatMessageEntity entity) {
        return new ChatMessageDto(
                entity.getId(),
                entity.getRole(),
                entity.getContent(),
                entity.getTimestampMs(),
                entity.isEscalated(),
                entity.getAvatarEmotion()
        );
    }

    public ChatMessageEntity fromDto(ChatMessageDto dto) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(dto.id() != null ? dto.id() : UUID.randomUUID());
        entity.setRole(dto.role());
        entity.setContent(dto.content());
        entity.setTimestampMs(dto.timestamp());
        entity.setEscalated(dto.isEscalated());
        entity.setAvatarEmotion(dto.avatarEmotion());
        return entity;
    }
}

