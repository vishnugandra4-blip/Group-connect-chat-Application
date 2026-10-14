package com.groupconnect.service;

import com.groupconnect.dto.chat.ChatMessageDto;
import com.groupconnect.dto.group.AddMemberRequest;
import com.groupconnect.dto.group.CreateGroupRequest;
import com.groupconnect.dto.group.GroupMemberDto;
import com.groupconnect.dto.group.GroupResponseDto;
import com.groupconnect.exception.BadRequestException;
import com.groupconnect.exception.ResourceNotFoundException;
import com.groupconnect.exception.UnauthorizedException;
import com.groupconnect.model.ChatGroup;
import com.groupconnect.model.GroupMember;
import com.groupconnect.model.Role;
import com.groupconnect.model.User;
import com.groupconnect.repository.GroupMemberRepository;
import com.groupconnect.repository.GroupRepository;
import com.groupconnect.repository.UserRepository;
import com.groupconnect.security.UserPrincipal;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        UserRepository userRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public GroupResponseDto createGroup(CreateGroupRequest request, UserPrincipal currentUser) {
        User creator = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        ChatGroup group = new ChatGroup(request.getName().trim(), request.getDescription(), creator);
        ChatGroup savedGroup = groupRepository.save(group);

        // Creator automatically joins as ADMIN
        GroupMember adminMember = new GroupMember(savedGroup, creator, Role.ADMIN);
        groupMemberRepository.save(adminMember);

        return mapToGroupResponseDto(savedGroup, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> getAllGroups(UserPrincipal currentUser) {
        List<ChatGroup> groups = groupRepository.findAllByOrderByCreatedAtDesc();
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        return groups.stream()
                .map(group -> mapToGroupResponseDto(group, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> getMyGroups(UserPrincipal currentUser) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(currentUser.getId());

        return memberships.stream()
                .map(member -> mapToGroupResponseDto(member.getGroup(), currentUser.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponseDto getGroupById(Long groupId, UserPrincipal currentUser) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        return mapToGroupResponseDto(group, currentUserId);
    }

    @Transactional
    public GroupResponseDto joinGroup(Long groupId, UserPrincipal currentUser) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new BadRequestException("You are already a member of this group");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        GroupMember newMember = new GroupMember(group, user, Role.MEMBER);
        groupMemberRepository.save(newMember);

        // Broadcast join system notification to STOMP subscribers
        ChatMessageDto joinNotification = new ChatMessageDto(
                null,
                groupId,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getFullName() + " joined the group",
                "JOIN",
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/group/" + groupId, joinNotification);

        return mapToGroupResponseDto(group, currentUser.getId());
    }

    @Transactional
    public void leaveGroup(Long groupId, UserPrincipal currentUser) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member of this group"));

        groupMemberRepository.delete(member);

        // Broadcast leave system notification to STOMP subscribers
        ChatMessageDto leaveNotification = new ChatMessageDto(
                null,
                groupId,
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getFullName(),
                currentUser.getFullName() + " left the group",
                "LEAVE",
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/group/" + groupId, leaveNotification);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberDto> getGroupMembers(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group", "id", groupId);
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .map(this::mapToGroupMemberDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupMemberDto addMember(Long groupId, AddMemberRequest request, UserPrincipal currentUser) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // Ensure requester is an admin or member of the group
        GroupMember requesterMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You must be a member of this group to invite others"));

        String identifier = request.getUsernameOrEmail().trim();
        User targetUser = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username or email: " + identifier));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUser.getId())) {
            throw new BadRequestException("User is already a member of this group");
        }

        Role assignedRole = (request.getRole() != null) ? request.getRole() : Role.MEMBER;
        GroupMember newMember = new GroupMember(group, targetUser, assignedRole);
        GroupMember savedMember = groupMemberRepository.save(newMember);

        // Broadcast join system notification to STOMP subscribers
        ChatMessageDto joinNotification = new ChatMessageDto(
                null,
                groupId,
                targetUser.getId(),
                targetUser.getUsername(),
                targetUser.getFullName(),
                targetUser.getFullName() + " was added to the group",
                "JOIN",
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/group/" + groupId, joinNotification);

        return mapToGroupMemberDto(savedMember);
    }

    @Transactional
    public void removeMember(Long groupId, Long memberUserId, UserPrincipal currentUser) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        GroupMember requesterMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Only ADMIN or the member themselves can remove
        if (requesterMembership.getRole() != Role.ADMIN && !currentUser.getId().equals(memberUserId)) {
            throw new UnauthorizedException("Only group admins can remove other members");
        }

        GroupMember targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));

        groupMemberRepository.delete(targetMember);
    }

    private GroupResponseDto mapToGroupResponseDto(ChatGroup group, Long currentUserId) {
        long memberCount = groupMemberRepository.countByGroupId(group.getId());
        boolean isMember = false;
        boolean isAdmin = false;

        if (currentUserId != null) {
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(group.getId(), currentUserId);
            if (membership.isPresent()) {
                isMember = true;
                isAdmin = (membership.get().getRole() == Role.ADMIN);
            }
        }

        return new GroupResponseDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy().getId(),
                group.getCreatedBy().getUsername(),
                memberCount,
                group.getCreatedAt(),
                isMember,
                isAdmin
        );
    }

    private GroupMemberDto mapToGroupMemberDto(GroupMember member) {
        return new GroupMemberDto(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getFullName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
