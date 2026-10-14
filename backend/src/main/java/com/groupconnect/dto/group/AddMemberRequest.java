package com.groupconnect.dto.group;

import com.groupconnect.model.Role;
import jakarta.validation.constraints.NotBlank;

public class AddMemberRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    private Role role = Role.MEMBER;

    public AddMemberRequest() {
    }

    public AddMemberRequest(String usernameOrEmail, Role role) {
        this.usernameOrEmail = usernameOrEmail;
        this.role = (role != null) ? role : Role.MEMBER;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
