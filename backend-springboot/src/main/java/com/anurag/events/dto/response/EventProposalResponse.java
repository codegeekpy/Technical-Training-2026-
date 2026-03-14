package com.anurag.events.dto.response;

import com.anurag.events.entity.EventProposal.ProposalStatus;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class EventProposalResponse {
    private Long id;
    private String title;
    private String description;
    private UserResponse organizer;
    private VenueResponse venue;
    private String faculty_incharge;
    private Integer expected_participants;
    private OffsetDateTime start_datetime;
    private OffsetDateTime end_datetime;
    private String event_type;
    private ProposalStatus status;
    private String coordinator_remarks;
    private OffsetDateTime coordinator_reviewed_at;
    private String dean_remarks;
    private OffsetDateTime dean_reviewed_at;
    private OffsetDateTime created_at;
}
