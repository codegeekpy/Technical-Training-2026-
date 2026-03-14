package com.anurag.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "event_proposals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "faculty_incharge", nullable = false, length = 255)
    private String facultyIncharge;

    @Column(name = "expected_participants", nullable = false)
    private Integer expectedParticipants;

    @Column(name = "start_datetime", nullable = false)
    private OffsetDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private OffsetDateTime endDatetime;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProposalStatus status = ProposalStatus.pending;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinator_id")
    private User coordinator;

    @Column(name = "coordinator_remarks", columnDefinition = "TEXT")
    private String coordinatorRemarks;

    @Column(name = "coordinator_reviewed_at")
    private OffsetDateTime coordinatorReviewedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dean_id")
    private User dean;

    @Column(name = "dean_remarks", columnDefinition = "TEXT")
    private String deanRemarks;

    @Column(name = "dean_reviewed_at")
    private OffsetDateTime deanReviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToOne(mappedBy = "eventProposal", fetch = FetchType.LAZY)
    private Booking booking;

    public enum ProposalStatus {
        draft, pending, coordinator_approved, approved, rejected
    }
}
