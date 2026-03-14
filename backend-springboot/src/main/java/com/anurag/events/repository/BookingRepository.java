package com.anurag.events.repository;

import com.anurag.events.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByEventProposalId(Long eventProposalId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.venue.id = :venueId
          AND b.startDatetime < :endDt
          AND b.endDatetime > :startDt
    """)
    List<Booking> findConflicts(
            @Param("venueId") Long venueId,
            @Param("startDt") OffsetDateTime startDt,
            @Param("endDt") OffsetDateTime endDt
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.venue.id = :venueId
          AND b.startDatetime < :endDt
          AND b.endDatetime > :startDt
          AND b.eventProposal.id <> :excludeProposalId
    """)
    List<Booking> findConflictsExcluding(
            @Param("venueId") Long venueId,
            @Param("startDt") OffsetDateTime startDt,
            @Param("endDt") OffsetDateTime endDt,
            @Param("excludeProposalId") Long excludeProposalId
    );
}
