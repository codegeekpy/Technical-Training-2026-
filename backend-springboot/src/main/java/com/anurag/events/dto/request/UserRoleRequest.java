package com.anurag.events.dto.request;

import com.anurag.events.entity.User.UserRole;
import lombok.Data;

@Data
public class UserRoleRequest {
    private UserRole role;
}
