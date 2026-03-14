package com.anurag.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_proposal_id")
    private EventProposal eventProposal;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 50)
    private NotificationType notificationType = NotificationType.general;

    @Column(nullable = false, length = 500)
    private String message;

    @Builder.Default
    @Column(name = "is_read")
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public enum NotificationType {
        proposal_submitted, proposal_approved, proposal_rejected,
        coordinator_review, dean_review, general
    }
}
