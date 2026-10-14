package com.groupconnect.controller;

import com.groupconnect.dto.chat.ChatMessageDto;
import com.groupconnect.dto.chat.SendMessageRequest;
import com.groupconnect.exception.UnauthorizedException;
import com.groupconnect.model.User;
import com.groupconnect.repository.UserRepository;
import com.groupconnect.security.UserPrincipal;
import com.groupconnect.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final MessageService messageService;
    private final UserRepository userRepository;

    public ChatWebSocketController(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.send/{groupId}")
    public void handleGroupMessage(@DestinationVariable Long groupId,
                                   @Payload @Valid SendMessageRequest request,
                                   Principal principal) {
        if (principal == null) {
            logger.error("Rejecting unauthenticated WebSocket message to group {}", groupId);
            throw new UnauthorizedException("Unauthenticated WebSocket session");
        }

        UserPrincipal userPrincipal = extractUserPrincipal(principal);
        ChatMessageDto sentMessage = messageService.sendMessage(groupId, request, userPrincipal);
        logger.info("Broadcasted STOMP message id={} to group={}", sentMessage.getId(), groupId);
    }

    private UserPrincipal extractUserPrincipal(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof UserPrincipal up) {
                return up;
            }
        }

        // Fallback: load by username
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found: " + username));

        return UserPrincipal.create(user);
    }
}
