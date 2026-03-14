package com.anurag.events.service;

import com.anurag.events.entity.Booking;
import com.anurag.events.entity.EventProposal;
import com.anurag.events.entity.Venue;
import com.anurag.events.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    /**
     * True means available (no conflicts). False means unavailable (has conflicts).
     */
    public boolean checkVenueAvailability(Long venueId, OffsetDateTime startDt, OffsetDateTime endDt, Long excludeProposalId) {
        List<Booking> conflicts;
        if (excludeProposalId != null) {
            conflicts = bookingRepository.findConflictsExcluding(venueId, startDt, endDt, excludeProposalId);
        } else {
            conflicts = bookingRepository.findConflicts(venueId, startDt, endDt);
        }
        return conflicts.isEmpty();
    }

    public List<Booking> getConflicts(Long venueId, OffsetDateTime startDt, OffsetDateTime endDt, Long excludeProposalId) {
         if (excludeProposalId != null) {
            return bookingRepository.findConflictsExcluding(venueId, startDt, endDt, excludeProposalId);
        } else {
            return bookingRepository.findConflicts(venueId, startDt, endDt);
        }
    }

    @Transactional
    public Booking createBooking(Venue venue, EventProposal eventProposal) {
        Booking booking = Booking.builder()
                .venue(venue)
                .eventProposal(eventProposal)
                .startDatetime(eventProposal.getStartDatetime())
                .endDatetime(eventProposal.getEndDatetime())
                .build();
        return bookingRepository.save(booking);
    }
}
