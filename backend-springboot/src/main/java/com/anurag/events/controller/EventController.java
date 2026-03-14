package com.anurag.events.controller;

import com.anurag.events.dto.request.EventProposalRequest;
import com.anurag.events.dto.request.ReviewRequest;
import com.anurag.events.dto.response.EventProposalResponse;
import com.anurag.events.entity.User;
import com.anurag.events.repository.UserRepository;
import com.anurag.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails details) {
        return userRepository.findById(Long.parseLong(details.getUsername())).orElseThrow();
    }

    @PostMapping("/propose")
    @ResponseStatus(HttpStatus.CREATED)
    public EventProposalResponse proposeEvent(@RequestBody EventProposalRequest payload, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return eventService.proposeEvent(payload, userId);
    }

    @GetMapping("/my")
    public List<EventProposalResponse> myProposals(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return eventService.getMyProposals(userId);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'DEAN', 'ADMIN')")
    public List<EventProposalResponse> pendingProposals(@AuthenticationPrincipal UserDetails userDetails) {
        return eventService.getPendingProposals(getCurrentUser(userDetails));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'DEAN', 'ADMIN')")
    public List<EventProposalResponse> allProposals(@RequestParam(required = false) String status) {
        return eventService.getAllProposals(status);
    }

    @GetMapping("/{proposalId}")
    public EventProposalResponse getProposal(@PathVariable Long proposalId, @AuthenticationPrincipal UserDetails userDetails) {
        return eventService.getProposal(proposalId, getCurrentUser(userDetails));
    }

    @PatchMapping("/{proposalId}/coordinator-approve")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public EventProposalResponse coordinatorApprove(@PathVariable Long proposalId, @RequestBody ReviewRequest review, @AuthenticationPrincipal UserDetails userDetails) {
        return eventService.coordinatorApprove(proposalId, review, getCurrentUser(userDetails));
    }

    @PatchMapping("/{proposalId}/coordinator-reject")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public EventProposalResponse coordinatorReject(@PathVariable Long proposalId, @RequestBody ReviewRequest review, @AuthenticationPrincipal UserDetails userDetails) {
        return eventService.coordinatorReject(proposalId, review, getCurrentUser(userDetails));
    }

    @PatchMapping("/{proposalId}/dean-approve")
    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    public EventProposalResponse deanApprove(@PathVariable Long proposalId, @RequestBody ReviewRequest review, @AuthenticationPrincipal UserDetails userDetails) {
        return eventService.deanApprove(proposalId, review, getCurrentUser(userDetails));
    }

    @PatchMapping("/{proposalId}/dean-reject")
    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    public EventProposalResponse deanReject(@PathVariable Long proposalId, @RequestBody ReviewRequest review, @AuthenticationPrincipal UserDetails userDetails) {
        return eventService.deanReject(proposalId, review, getCurrentUser(userDetails));
    }
}
