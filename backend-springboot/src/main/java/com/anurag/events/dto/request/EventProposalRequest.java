package com.anurag.events.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventProposalRequest {
    private String title;
    private String description;
    private Long venue_id;
    private String faculty_incharge;
    private Integer expected_participants;
    private LocalDateTime start_datetime;
    private LocalDateTime end_datetime;
    private String event_type;
}
