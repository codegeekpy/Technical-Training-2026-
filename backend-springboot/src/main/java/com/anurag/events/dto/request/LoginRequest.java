package com.anurag.events.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
