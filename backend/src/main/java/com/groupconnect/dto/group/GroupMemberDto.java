package com.groupconnect.dto.group;

import com.groupconnect.model.Role;
import java.time.LocalDateTime;

public class GroupMemberDto {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private Role role;
    private LocalDateTime joinedAt;

    public GroupMemberDto() {
    }

    public GroupMemberDto(Long id, Long userId, String username, String fullName, Role role, LocalDateTime joinedAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
