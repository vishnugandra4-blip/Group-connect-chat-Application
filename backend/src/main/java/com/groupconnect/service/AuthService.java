package com.groupconnect.service;

import com.groupconnect.dto.auth.AuthResponse;
import com.groupconnect.dto.auth.LoginRequest;
import com.groupconnect.dto.auth.RegisterRequest;
import com.groupconnect.dto.auth.UserSummaryDto;
import com.groupconnect.exception.BadRequestException;
import com.groupconnect.model.User;
import com.groupconnect.repository.UserRepository;
import com.groupconnect.security.JwtTokenProvider;
import com.groupconnect.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : request.getUsername().trim());

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUsername(savedUser.getUsername(), savedUser.getId());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim();

        // Find user to get canonical username if an email was supplied
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new BadRequestException("Invalid username/email or password"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName()
        );
    }

    public UserSummaryDto getCurrentUser(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("User profile not found"));

        return new UserSummaryDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName());
    }
}
