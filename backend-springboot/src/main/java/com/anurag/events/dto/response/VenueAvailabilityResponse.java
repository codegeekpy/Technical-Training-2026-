package com.anurag.events.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class VenueAvailabilityResponse {
    private Long venue_id;
    private String venue_name;
    @com.fasterxml.jackson.annotation.JsonProperty("is_available")
    private boolean isAvailable;
    private List<ConflictingEvent> conflicting_events;

    @Data
    @Builder
    public static class ConflictingEvent {
        private Long proposal_id;
        private String event_title;
        private String organizer;
        private OffsetDateTime start;
        private OffsetDateTime end;
    }
}
