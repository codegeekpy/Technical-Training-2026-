package com.anurag.events.controller;

import com.anurag.events.dto.response.NotificationResponse;
import com.anurag.events.entity.Notification;
import com.anurag.events.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/me")
    public List<NotificationResponse> myNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(50)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable Long notificationId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Notification notif = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        notif.setRead(true);
        notif = notificationRepository.save(notif);
        return mapToResponse(notif);
    }

    @PatchMapping("/mark-all-read")
    @Transactional
    public java.util.Map<String, String> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationRepository.markAllReadByUserId(userId);
        return java.util.Map.of("message", "All notifications marked as read");
    }

    private NotificationResponse mapToResponse(Notification n) {
        Long eventId = n.getEventProposal() != null ? n.getEventProposal().getId() : null;
        return NotificationResponse.builder()
                .id(n.getId())
                .user_id(n.getUser().getId())
                .event_proposal_id(eventId)
                .notification_type(n.getNotificationType())
                .message(n.getMessage())
                .is_read(n.isRead())
                .created_at(n.getCreatedAt())
                .build();
    }
}
