package com.anurag.events.security;

import com.anurag.events.entity.User;
import com.anurag.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        try {
            Long id = Long.parseLong(userIdStr);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

            if (!user.isActive()) {
                throw new UsernameNotFoundException("User account is inactive");
            }

            return org.springframework.security.core.userdetails.User.builder()
                    .username(String.valueOf(user.getId()))
                    .password(user.getHashedPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase())))
                    .build();

        } catch (NumberFormatException e) {
            // Fallback: If for some reason an email is passed (e.g., during login AuthenticationManager call)
            User user = userRepository.findByEmail(userIdStr)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userIdStr));

            if (!user.isActive()) {
                throw new UsernameNotFoundException("User account is inactive");
            }

            return org.springframework.security.core.userdetails.User.builder()
                    .username(String.valueOf(user.getId()))
                    .password(user.getHashedPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase())))
                    .build();
        }
    }
}
