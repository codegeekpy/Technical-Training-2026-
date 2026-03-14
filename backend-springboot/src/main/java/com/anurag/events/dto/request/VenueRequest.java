package com.anurag.events.dto.request;

import com.anurag.events.entity.Venue.VenueType;
import lombok.Data;

@Data
public class VenueRequest {
    private String name;
    private VenueType venue_type;
    private Integer capacity;
    private String location;
    private String description;
    private Boolean is_active;
}
