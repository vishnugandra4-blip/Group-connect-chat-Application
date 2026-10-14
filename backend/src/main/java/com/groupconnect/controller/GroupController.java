package com.groupconnect.controller;

import com.groupconnect.dto.group.AddMemberRequest;
import com.groupconnect.dto.group.CreateGroupRequest;
import com.groupconnect.dto.group.GroupMemberDto;
import com.groupconnect.dto.group.GroupResponseDto;
import com.groupconnect.security.UserPrincipal;
import com.groupconnect.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                        @AuthenticationPrincipal UserPrincipal currentUser) {
        GroupResponseDto group = groupService.createGroup(request, currentUser);
        return new ResponseEntity<>(group, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> getAllGroups(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<GroupResponseDto> groups = groupService.getAllGroups(currentUser);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupResponseDto>> getMyGroups(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<GroupResponseDto> groups = groupService.getMyGroups(currentUser);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDto> getGroupById(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        GroupResponseDto group = groupService.getGroupById(id, currentUser);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<GroupResponseDto> joinGroup(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        GroupResponseDto group = groupService.joinGroup(id, currentUser);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Map<String, String>> leaveGroup(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        groupService.leaveGroup(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Successfully left group"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<GroupMemberDto>> getGroupMembers(@PathVariable Long id) {
        List<GroupMemberDto> members = groupService.getGroupMembers(id);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupMemberDto> addMember(@PathVariable Long id,
                                                    @Valid @RequestBody AddMemberRequest request,
                                                    @AuthenticationPrincipal UserPrincipal currentUser) {
        GroupMemberDto member = groupService.addMember(id, request, currentUser);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(@PathVariable Long id,
                                                            @PathVariable Long userId,
                                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        groupService.removeMember(id, userId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Member successfully removed"));
    }
}
