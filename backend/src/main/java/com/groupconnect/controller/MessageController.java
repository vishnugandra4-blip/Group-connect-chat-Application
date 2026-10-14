package com.groupconnect.controller;

import com.groupconnect.dto.chat.ChatMessageDto;
import com.groupconnect.dto.chat.SendMessageRequest;
import com.groupconnect.security.UserPrincipal;
import com.groupconnect.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageDto>> getGroupMessages(@PathVariable Long groupId,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ChatMessageDto> messages = messageService.getGroupMessages(groupId, currentUser);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<ChatMessageDto> sendMessage(@PathVariable Long groupId,
                                                      @Valid @RequestBody SendMessageRequest request,
                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        ChatMessageDto message = messageService.sendMessage(groupId, request, currentUser);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
}
