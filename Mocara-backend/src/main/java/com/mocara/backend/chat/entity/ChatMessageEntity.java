package com.mocara.backend.chat.entity;

import com.mocara.backend.common.enums.AvatarEmotion;
import com.mocara.backend.common.enums.UserRole;
import com.mocara.backend.session.entity.PatientSessionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private PatientSessionEntity session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "timestamp_ms", nullable = false)
    private long timestampMs;

    @Column(name = "is_escalated", nullable = false)
    private boolean escalated;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_emotion", nullable = false)
    private AvatarEmotion avatarEmotion = AvatarEmotion.NEUTRAL;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PatientSessionEntity getSession() {
        return session;
    }

    public void setSession(PatientSessionEntity session) {
        this.session = session;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public AvatarEmotion getAvatarEmotion() {
        return avatarEmotion;
    }

    public void setAvatarEmotion(AvatarEmotion avatarEmotion) {
        this.avatarEmotion = avatarEmotion;
    }
}

