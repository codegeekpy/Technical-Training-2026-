package com.anurag.events.controller;

import com.anurag.events.dto.request.VenueRequest;
import com.anurag.events.dto.response.VenueAvailabilityResponse;
import com.anurag.events.dto.response.VenueResponse;
import com.anurag.events.entity.Booking;
import com.anurag.events.entity.EventProposal;
import com.anurag.events.entity.Venue;
import com.anurag.events.repository.VenueRepository;
import com.anurag.events.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueRepository venueRepository;
    private final BookingService bookingService;

    @GetMapping("/") // Match FastAPI exact routing
    public List<VenueResponse> listVenues(@RequestParam(defaultValue = "true") boolean active_only) {
        List<Venue> venues = active_only ? venueRepository.findByIsActiveTrue() : venueRepository.findAll();
        return venues.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @GetMapping("/{venueId}")
    public VenueResponse getVenue(@PathVariable Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));
        return mapToResponse(venue);
    }

    @GetMapping("/{venueId}/availability")
    public VenueAvailabilityResponse checkAvailability(
            @PathVariable Long venueId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime start_datetime,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime end_datetime,
            @RequestParam(required = false) Long exclude_proposal_id) {

        java.time.OffsetDateTime start = start_datetime.atOffset(java.time.OffsetDateTime.now().getOffset());
        java.time.OffsetDateTime end = end_datetime.atOffset(java.time.OffsetDateTime.now().getOffset());

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start_datetime must be before end_datetime");
        }

        List<Booking> conflicts = bookingService.getConflicts(venueId, start, end, exclude_proposal_id);
        List<VenueAvailabilityResponse.ConflictingEvent> conflictingEvents = new ArrayList<>();

        for (Booking b : conflicts) {
            EventProposal prop = b.getEventProposal();
            conflictingEvents.add(VenueAvailabilityResponse.ConflictingEvent.builder()
                    .proposal_id(prop.getId())
                    .event_title(prop.getTitle())
                    .organizer(prop.getOrganizer().getFullName())
                    .start(b.getStartDatetime())
                    .end(b.getEndDatetime())
                    .build());
        }

        return VenueAvailabilityResponse.builder()
                .venue_id(venueId)
                .venue_name(venue.getName())
                .isAvailable(conflictingEvents.isEmpty())
                .conflicting_events(conflictingEvents)
                .build();
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public VenueResponse createVenue(@RequestBody VenueRequest payload) {
        Venue venue = Venue.builder()
                .name(payload.getName())
                .venueType(payload.getVenue_type())
                .capacity(payload.getCapacity())
                .location(payload.getLocation())
                .description(payload.getDescription())
                .isActive(true)
                .build();
        venue = venueRepository.save(venue);
        return mapToResponse(venue);
    }

    @PatchMapping("/{venueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public VenueResponse updateVenue(@PathVariable Long venueId, @RequestBody VenueRequest payload) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        if (payload.getName() != null) venue.setName(payload.getName());
        if (payload.getVenue_type() != null) venue.setVenueType(payload.getVenue_type());
        if (payload.getCapacity() != null) venue.setCapacity(payload.getCapacity());
        if (payload.getLocation() != null) venue.setLocation(payload.getLocation());
        if (payload.getDescription() != null) venue.setDescription(payload.getDescription());
        if (payload.getIs_active() != null) venue.setActive(payload.getIs_active());

        venue = venueRepository.save(venue);
        return mapToResponse(venue);
    }

    private VenueResponse mapToResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .venue_type(venue.getVenueType())
                .capacity(venue.getCapacity())
                .location(venue.getLocation())
                .description(venue.getDescription())
                .is_active(venue.isActive())
                .build();
    }
}
