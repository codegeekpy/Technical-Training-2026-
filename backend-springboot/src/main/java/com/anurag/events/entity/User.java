package com.anurag.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role = UserRole.student;

    @Column(length = 100)
    private String department;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
    private List<EventProposal> proposals;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Notification> notifications;

    public enum UserRole {
        student, coordinator, dean, admin
    }
}
