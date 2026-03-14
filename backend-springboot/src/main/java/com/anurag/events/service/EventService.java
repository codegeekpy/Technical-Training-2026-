package com.anurag.events.service;

import com.anurag.events.dto.request.EventProposalRequest;
import com.anurag.events.dto.request.ReviewRequest;
import com.anurag.events.dto.response.EventProposalResponse;
import com.anurag.events.dto.response.UserResponse;
import com.anurag.events.dto.response.VenueResponse;
import com.anurag.events.entity.EventProposal;
import com.anurag.events.entity.Notification.NotificationType;
import com.anurag.events.entity.User;
import com.anurag.events.entity.Venue;
import com.anurag.events.repository.EventProposalRepository;
import com.anurag.events.repository.UserRepository;
import com.anurag.events.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventProposalRepository eventRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public EventProposalResponse proposeEvent(EventProposalRequest request, Long userId) {
        OffsetDateTime start = request.getStart_datetime().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime end = request.getEnd_datetime().atOffset(OffsetDateTime.now().getOffset());

        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
        if (start.isBefore(OffsetDateTime.now().minusDays(1))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event cannot be scheduled in the past");
        }

        boolean isAvailable = bookingService.checkVenueAvailability(request.getVenue_id(), start, end, null);
        if (!isAvailable) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Venue is already booked for the selected time slot. Please choose a different time or venue.");
        }

        User organizer = userRepository.findById(userId).orElseThrow();
        Venue venue = venueRepository.findById(request.getVenue_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        EventProposal proposal = EventProposal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .organizer(organizer)
                .venue(venue)
                .facultyIncharge(request.getFaculty_incharge())
                .expectedParticipants(request.getExpected_participants())
                .startDatetime(start)
                .endDatetime(end)
                .eventType(request.getEvent_type())
                .status(EventProposal.ProposalStatus.pending)
                .build();

        proposal = eventRepository.save(proposal);

        notificationService.createNotification(organizer.getId(), "Your event proposal '" + proposal.getTitle() + "' has been submitted and is pending coordinator review.", NotificationType.proposal_submitted, proposal);

        List<User> coordinators = notificationService.notifyCoordinators("New event proposal '" + proposal.getTitle() + "' by " + organizer.getFullName() + " requires your review.", NotificationType.coordinator_review, proposal);

        emailService.sendProposalSubmitted(organizer.getEmail(), organizer.getFullName(), proposal.getTitle(), proposal.getId());
        for (User c : coordinators) {
            emailService.sendCoordinatorReviewRequest(c.getEmail(), c.getFullName(), proposal.getTitle(), organizer.getFullName(), proposal.getId());
        }

        return mapToResponse(proposal);
    }

    public List<EventProposalResponse> getMyProposals(Long userId) {
        return eventRepository.findByOrganizerIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EventProposalResponse> getPendingProposals(User user) {
        if (user.getRole() == User.UserRole.coordinator) {
            return eventRepository.findByStatusOrderByCreatedAtAsc(EventProposal.ProposalStatus.pending)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else if (user.getRole() == User.UserRole.dean) {
            return eventRepository.findByStatusOrderByCreatedAtAsc(EventProposal.ProposalStatus.coordinator_approved)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }
        return eventRepository.findByStatusInOrderByCreatedAtAsc(List.of(EventProposal.ProposalStatus.pending, EventProposal.ProposalStatus.coordinator_approved))
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EventProposalResponse> getAllProposals(String status) {
        List<EventProposal> proposals;
        if (status != null && !status.isEmpty()) {
            proposals = eventRepository.findByStatusOrderByCreatedAtDesc(EventProposal.ProposalStatus.valueOf(status));
        } else {
            proposals = eventRepository.findAllOrderByCreatedAtDesc();
        }
        return proposals.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public EventProposalResponse getProposal(Long proposalId, User currentUser) {
        EventProposal proposal = eventRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));

        if (currentUser.getRole() == User.UserRole.student && !proposal.getOrganizer().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return mapToResponse(proposal);
    }

    @Transactional
    public EventProposalResponse coordinatorApprove(Long proposalId, ReviewRequest review, User currentUser) {
        EventProposal proposal = eventRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));

        if (proposal.getStatus() != EventProposal.ProposalStatus.pending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposal is not in pending state (current: " + proposal.getStatus() + ")");
        }

        if (!bookingService.checkVenueAvailability(proposal.getVenue().getId(), proposal.getStartDatetime(), proposal.getEndDatetime(), proposalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Venue conflict detected at approval time.");
        }

        proposal.setStatus(EventProposal.ProposalStatus.coordinator_approved);
        proposal.setCoordinator(currentUser);
        proposal.setCoordinatorRemarks(review.getRemarks());
        proposal.setCoordinatorReviewedAt(OffsetDateTime.now());
        eventRepository.save(proposal);

        notificationService.createNotification(proposal.getOrganizer().getId(), "Your proposal '" + proposal.getTitle() + "' was approved by the coordinator and is now awaiting Dean review.", NotificationType.coordinator_review, proposal);
        notificationService.notifyDeans("Event proposal '" + proposal.getTitle() + "' by " + proposal.getOrganizer().getFullName() + " requires Dean approval.", NotificationType.dean_review, proposal);

        return mapToResponse(proposal);
    }

    @Transactional
    public EventProposalResponse coordinatorReject(Long proposalId, ReviewRequest review, User currentUser) {
        EventProposal proposal = eventRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));

        if (proposal.getStatus() != EventProposal.ProposalStatus.pending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposal is not in pending state");
        }

        proposal.setStatus(EventProposal.ProposalStatus.rejected);
        proposal.setCoordinator(currentUser);
        proposal.setCoordinatorRemarks(review.getRemarks());
        proposal.setCoordinatorReviewedAt(OffsetDateTime.now());
        eventRepository.save(proposal);

        String remarks = review.getRemarks() != null ? review.getRemarks() : "N/A";
        notificationService.createNotification(proposal.getOrganizer().getId(), "Your proposal '" + proposal.getTitle() + "' was not approved by the coordinator. Remarks: " + remarks, NotificationType.proposal_rejected, proposal);
        emailService.sendProposalRejected(proposal.getOrganizer().getEmail(), proposal.getOrganizer().getFullName(), proposal.getTitle(), proposal.getId(), review.getRemarks());

        return mapToResponse(proposal);
    }

    @Transactional
    public EventProposalResponse deanApprove(Long proposalId, ReviewRequest review, User currentUser) {
        EventProposal proposal = eventRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));

        if (proposal.getStatus() != EventProposal.ProposalStatus.coordinator_approved) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposal must be coordinator-approved first");
        }

        if (!bookingService.checkVenueAvailability(proposal.getVenue().getId(), proposal.getStartDatetime(), proposal.getEndDatetime(), proposalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Venue conflict detected at final approval time.");
        }

        proposal.setStatus(EventProposal.ProposalStatus.approved);
        proposal.setDean(currentUser);
        proposal.setDeanRemarks(review.getRemarks());
        proposal.setDeanReviewedAt(OffsetDateTime.now());
        eventRepository.save(proposal);

        bookingService.createBooking(proposal.getVenue(), proposal);

        notificationService.createNotification(proposal.getOrganizer().getId(), "🎉 Your event '" + proposal.getTitle() + "' has been fully approved! Venue is confirmed.", NotificationType.proposal_approved, proposal);
        emailService.sendProposalApproved(proposal.getOrganizer().getEmail(), proposal.getOrganizer().getFullName(), proposal.getTitle(), proposal.getId(), review.getRemarks());

        return mapToResponse(proposal);
    }

    @Transactional
    public EventProposalResponse deanReject(Long proposalId, ReviewRequest review, User currentUser) {
        EventProposal proposal = eventRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));

        if (proposal.getStatus() != EventProposal.ProposalStatus.coordinator_approved) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposal must be coordinator-approved first");
        }

        proposal.setStatus(EventProposal.ProposalStatus.rejected);
        proposal.setDean(currentUser);
        proposal.setDeanRemarks(review.getRemarks());
        proposal.setDeanReviewedAt(OffsetDateTime.now());
        eventRepository.save(proposal);

        String remarks = review.getRemarks() != null ? review.getRemarks() : "N/A";
        notificationService.createNotification(proposal.getOrganizer().getId(), "Your proposal '" + proposal.getTitle() + "' was declined by the Dean. Remarks: " + remarks, NotificationType.proposal_rejected, proposal);
        emailService.sendProposalRejected(proposal.getOrganizer().getEmail(), proposal.getOrganizer().getFullName(), proposal.getTitle(), proposal.getId(), review.getRemarks());

        return mapToResponse(proposal);
    }

    public EventProposalResponse mapToResponse(EventProposal p) {
        User organizer = p.getOrganizer();
        Venue venue = p.getVenue();

        UserResponse userResponse = UserResponse.builder()
                .id(organizer.getId())
                .full_name(organizer.getFullName())
                .email(organizer.getEmail())
                .role(organizer.getRole())
                .department(organizer.getDepartment())
                .is_active(organizer.isActive())
                .created_at(organizer.getCreatedAt())
                .build();

        VenueResponse venueResponse = VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .venue_type(venue.getVenueType())
                .capacity(venue.getCapacity())
                .location(venue.getLocation())
                .description(venue.getDescription())
                .is_active(venue.isActive())
                .build();

        return EventProposalResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .organizer(userResponse)
                .venue(venueResponse)
                .faculty_incharge(p.getFacultyIncharge())
                .expected_participants(p.getExpectedParticipants())
                .start_datetime(p.getStartDatetime())
                .end_datetime(p.getEndDatetime())
                .event_type(p.getEventType())
                .status(p.getStatus())
                .coordinator_remarks(p.getCoordinatorRemarks())
                .coordinator_reviewed_at(p.getCoordinatorReviewedAt())
                .dean_remarks(p.getDeanRemarks())
                .dean_reviewed_at(p.getDeanReviewedAt())
                .created_at(p.getCreatedAt())
                .build();
    }
}
