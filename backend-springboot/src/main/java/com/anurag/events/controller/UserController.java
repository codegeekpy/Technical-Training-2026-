package com.anurag.events.controller;

import com.anurag.events.dto.request.UserRoleRequest;
import com.anurag.events.dto.response.UserResponse;
import com.anurag.events.entity.User;
import com.anurag.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/")
    public List<UserResponse> listUsers() {
        // Technically findall doesn't order by default, but we can manage by querying or stream sort
        return userRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToResponse(user);
    }

    @PatchMapping("/{userId}/role")
    public UserResponse updateUserRole(
            @PathVariable Long userId,
            @RequestBody UserRoleRequest payload,
            @AuthenticationPrincipal UserDetails currentUser) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getId().toString().equals(currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change your own role");
        }

        user.setRole(payload.getRole());
        return mapToResponse(userRepository.save(user));
    }

    @PatchMapping("/{userId}/toggle-active")
    public UserResponse toggleUserActive(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getId().toString().equals(currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot deactivate yourself");
        }

        user.setActive(!user.isActive());
        return mapToResponse(userRepository.save(user));
    }

    private UserResponse mapToResponse(User user) {
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
