package com.anurag.events.initializer;

import com.anurag.events.entity.User;
import com.anurag.events.entity.Venue;
import com.anurag.events.repository.UserRepository;
import com.anurag.events.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedVenues();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users...");
            User admin = User.builder()
                    .fullName("System Admin")
                    .email("admin@anurag.edu.in")
                    .hashedPassword(passwordEncoder.encode("admin@123"))
                    .role(User.UserRole.admin)
                    .department("Administration")
                    .isActive(true)
                    .build();

            User coordinator = User.builder()
                    .fullName("Dr. Ramesh Kumar")
                    .email("coordinator@anurag.edu.in")
                    .hashedPassword(passwordEncoder.encode("coord@123"))
                    .role(User.UserRole.coordinator)
                    .department("Event Management")
                    .isActive(true)
                    .build();

            User dean = User.builder()
                    .fullName("Prof. Sunita Sharma")
                    .email("dean@anurag.edu.in")
                    .hashedPassword(passwordEncoder.encode("dean@123"))
                    .role(User.UserRole.dean)
                    .department("Dean's Office")
                    .isActive(true)
                    .build();

            User student = User.builder()
                    .fullName("Arjun Reddy")
                    .email("student@anurag.edu.in")
                    .hashedPassword(passwordEncoder.encode("student@123"))
                    .role(User.UserRole.student)
                    .department("Computer Science")
                    .isActive(true)
                    .build();

            userRepository.saveAll(Arrays.asList(admin, coordinator, dean, student));
            log.info("Users seeded successfully.");
        }
    }

    private void seedVenues() {
        if (venueRepository.count() == 0) {
            log.info("Seeding initial venues...");
            Venue v1 = Venue.builder().name("Sri Venkateswara Auditorium").venueType(Venue.VenueType.auditorium).capacity(500).location("Main Block, Ground Floor").description("Main university auditorium with full AV setup").isActive(true).build();
            Venue v2 = Venue.builder().name("Seminar Hall A").venueType(Venue.VenueType.seminar_hall).capacity(150).location("Academic Block 1, 2nd Floor").description("Seminar hall with projector and mic").isActive(true).build();
            Venue v3 = Venue.builder().name("Seminar Hall B").venueType(Venue.VenueType.seminar_hall).capacity(150).location("Academic Block 2, 1st Floor").description("Seminar hall with projector").isActive(true).build();
            Venue v4 = Venue.builder().name("Computer Lab 301").venueType(Venue.VenueType.lab).capacity(60).location("IT Block, 3rd Floor").description("Computer lab with 60 workstations").isActive(true).build();
            Venue v5 = Venue.builder().name("Computer Lab 302").venueType(Venue.VenueType.lab).capacity(60).location("IT Block, 3rd Floor").description("Computer lab with 60 workstations").isActive(true).build();
            Venue v6 = Venue.builder().name("Conference Room").venueType(Venue.VenueType.conference_room).capacity(30).location("Admin Block, 1st Floor").description("Board room with video conferencing").isActive(true).build();
            Venue v7 = Venue.builder().name("Open Ground").venueType(Venue.VenueType.open_ground).capacity(2000).location("Campus Grounds").description("Open space for large events").isActive(true).build();

            venueRepository.saveAll(Arrays.asList(v1, v2, v3, v4, v5, v6, v7));
            log.info("Venues seeded successfully.");
        }
    }
}
