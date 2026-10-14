package com.groupconnect.dto.chat;

import java.time.LocalDateTime;

public class ChatMessageDto {

    private Long id;
    private Long groupId;
    private Long senderId;
    private String senderUsername;
    private String senderFullName;
    private String content;
    private String type = "CHAT";
    private LocalDateTime createdAt;

    public ChatMessageDto() {
    }

    public ChatMessageDto(Long id, Long groupId, Long senderId, String senderUsername,
                          String senderFullName, String content, String type, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderFullName = senderFullName;
        this.content = content;
        this.type = (type != null) ? type : "CHAT";
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderFullName() {
        return senderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
