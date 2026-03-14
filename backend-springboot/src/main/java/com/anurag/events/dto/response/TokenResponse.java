package com.anurag.events.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String access_token;
    private String token_type;
    private UserResponse user;
}
