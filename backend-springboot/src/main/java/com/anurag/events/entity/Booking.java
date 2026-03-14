package com.anurag.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_proposal_id", nullable = false, unique = true)
    private EventProposal eventProposal;

    @Column(name = "start_datetime", nullable = false)
    private OffsetDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private OffsetDateTime endDatetime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
