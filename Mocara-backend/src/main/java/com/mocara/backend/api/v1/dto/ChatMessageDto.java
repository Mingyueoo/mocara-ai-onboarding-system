package com.mocara.backend.api.v1.dto;

import com.mocara.backend.common.enums.AvatarEmotion;
import com.mocara.backend.common.enums.UserRole;

import java.util.UUID;

public record ChatMessageDto(
        UUID id,
        UserRole role,
        String content,
        long timestamp,
        boolean isEscalated,
        AvatarEmotion avatarEmotion
) {}

