package com.anurag.events.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "venues")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "venue_type", nullable = false, length = 50)
    private VenueType venueType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    private List<EventProposal> proposals;

    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    public enum VenueType {
        auditorium, seminar_hall, lab, conference_room, open_ground
    }
}
