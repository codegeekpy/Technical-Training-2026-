package com.anurag.events.controller;

import com.anurag.events.dto.request.LoginRequest;
import com.anurag.events.dto.request.UserRegisterRequest;
import com.anurag.events.dto.response.TokenResponse;
import com.anurag.events.dto.response.UserResponse;
import com.anurag.events.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse loginForm(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return authService.login(req);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TokenResponse loginJson(@RequestBody LoginRequest jsonRequest) {
        return authService.login(jsonRequest);
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return authService.getMe(userId);
    }
}
