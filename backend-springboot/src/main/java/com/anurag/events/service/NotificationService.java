package com.anurag.events.service;

import com.anurag.events.entity.EventProposal;
import com.anurag.events.entity.Notification;
import com.anurag.events.entity.Notification.NotificationType;
import com.anurag.events.entity.User;
import com.anurag.events.repository.NotificationRepository;
import com.anurag.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(Long userId, String message, NotificationType type, EventProposal proposal) {
        User user = userRepository.getReferenceById(userId);
        Notification notification = Notification.builder()
                .user(user)
                .eventProposal(proposal)
                .message(message)
                .notificationType(type)
                .build();
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<User> notifyCoordinators(String message, NotificationType type, EventProposal proposal) {
        List<User> coordinators = userRepository.findByRole(User.UserRole.coordinator);
        for (User coordinator : coordinators) {
            createNotification(coordinator.getId(), message, type, proposal);
        }
        return coordinators;
    }

    @Transactional
    public void notifyDeans(String message, NotificationType type, EventProposal proposal) {
        List<User> deans = userRepository.findByRole(User.UserRole.dean);
        for (User dean : deans) {
            createNotification(dean.getId(), message, type, proposal);
        }
    }
}
