package com.groupconnect.dto.group;

import java.time.LocalDateTime;

public class GroupResponseDto {

    private Long id;
    private String name;
    private String description;
    private Long createdById;
    private String createdByUsername;
    private long memberCount;
    private LocalDateTime createdAt;
    private boolean isMember;
    private boolean isAdmin;

    public GroupResponseDto() {
    }

    public GroupResponseDto(Long id, String name, String description, Long createdById,
                            String createdByUsername, long memberCount, LocalDateTime createdAt,
                            boolean isMember, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdById = createdById;
        this.createdByUsername = createdByUsername;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.isMember = isMember;
        this.isAdmin = isAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean member) {
        isMember = member;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
