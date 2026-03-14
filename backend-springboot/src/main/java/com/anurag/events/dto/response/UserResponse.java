package com.anurag.events.dto.response;

import com.anurag.events.entity.User.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String full_name;
    private String email;
    private UserRole role;
    private String department;
    private boolean is_active;
    private OffsetDateTime created_at;
}
