package com.anurag.events.service;

import com.anurag.events.dto.request.LoginRequest;
import com.anurag.events.dto.request.UserRegisterRequest;
import com.anurag.events.dto.response.TokenResponse;
import com.anurag.events.dto.response.UserResponse;
import com.anurag.events.entity.User;
import com.anurag.events.repository.UserRepository;
import com.anurag.events.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFull_name())
                .email(request.getEmail())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.UserRole.student)
                .department(request.getDepartment())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .full_name(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .is_active(user.isActive())
                .created_at(user.getCreatedAt())
                .build();
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        if (!user.getEmail().endsWith("@anurag.edu.in")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access restricted to university accounts");
        }

        // Technically authenticationManager.authenticate won't work perfectly fine if we load by ID only,
        // but we updated UserDetailsServiceImpl to handle email fallback for this case
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .full_name(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .is_active(user.isActive())
                .created_at(user.getCreatedAt())
                .build();

        return TokenResponse.builder()
                .access_token(token)
                .token_type("bearer")
                .user(userResponse)
                .build();
    }

    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.builder()
                .id(user.getId())
                .full_name(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .is_active(user.isActive())
                .created_at(user.getCreatedAt())
                .build();
    }
}
