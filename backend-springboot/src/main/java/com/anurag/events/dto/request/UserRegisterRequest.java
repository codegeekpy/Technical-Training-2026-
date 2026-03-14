package com.anurag.events.dto.request;

import com.anurag.events.entity.User.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "Full name is required")
    private String full_name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String department;

    private UserRole role = UserRole.student;
}
