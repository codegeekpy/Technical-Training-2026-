package com.anurag.events.dto.response;

import com.anurag.events.entity.Venue.VenueType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VenueResponse {
    private Long id;
    private String name;
    private VenueType venue_type;
    private Integer capacity;
    private String location;
    private String description;
    private boolean is_active;
}
