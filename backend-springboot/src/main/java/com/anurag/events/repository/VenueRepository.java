package com.anurag.events.repository;

import com.anurag.events.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByIsActiveTrue();
}
