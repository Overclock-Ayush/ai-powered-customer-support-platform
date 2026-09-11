package com.ayush.support.service;

import com.ayush.support.domain.User;
import com.ayush.support.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) { this.userRepository = userRepository; }

    public User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
