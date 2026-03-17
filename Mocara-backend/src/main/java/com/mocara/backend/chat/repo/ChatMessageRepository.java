package com.mocara.backend.chat.repo;

import com.mocara.backend.chat.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {
    List<ChatMessageEntity> findBySessionSessionIdOrderByTimestampMsAsc(UUID sessionId);
}

