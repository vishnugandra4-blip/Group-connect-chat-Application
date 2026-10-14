package com.groupconnect.service;

import com.groupconnect.dto.chat.ChatMessageDto;
import com.groupconnect.dto.chat.SendMessageRequest;
import com.groupconnect.exception.ResourceNotFoundException;
import com.groupconnect.exception.UnauthorizedException;
import com.groupconnect.model.ChatGroup;
import com.groupconnect.model.Message;
import com.groupconnect.model.User;
import com.groupconnect.repository.GroupMemberRepository;
import com.groupconnect.repository.GroupRepository;
import com.groupconnect.repository.MessageRepository;
import com.groupconnect.repository.UserRepository;
import com.groupconnect.security.UserPrincipal;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageRepository messageRepository,
                          GroupRepository groupRepository,
                          GroupMemberRepository groupMemberRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public ChatMessageDto sendMessage(Long groupId, SendMessageRequest request, UserPrincipal currentUser) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // Ensure user is an authorized member of the group
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId());
        if (!isMember) {
            throw new UnauthorizedException("You must join this group before sending messages");
        }

        User sender = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Message message = new Message(group, sender, request.getContent().trim());
        Message savedMessage = messageRepository.save(message);

        ChatMessageDto messageDto = mapToChatMessageDto(savedMessage);

        // Broadcast to WebSocket subscribers on /topic/group/{groupId}
        messagingTemplate.convertAndSend("/topic/group/" + groupId, messageDto);

        return messageDto;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getGroupMessages(Long groupId, UserPrincipal currentUser) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group", "id", groupId);
        }

        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId());
        if (!isMember) {
            throw new UnauthorizedException("You must be a member of this group to view messages");
        }

        List<Message> messages = messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);

        return messages.stream()
                .map(this::mapToChatMessageDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDto mapToChatMessageDto(Message message) {
        return new ChatMessageDto(
                message.getId(),
                message.getGroup().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getSender().getFullName(),
                message.getContent(),
                "CHAT",
                message.getCreatedAt()
        );
    }
}
