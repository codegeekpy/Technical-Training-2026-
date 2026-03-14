package com.anurag.events.dto.request;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EventProposalRequest {
    private String title;
    private String description;
    private Long venue_id;
    private String faculty_incharge;
    private Integer expected_participants;
    private OffsetDateTime start_datetime;
    private OffsetDateTime end_datetime;
    private String event_type;
}
