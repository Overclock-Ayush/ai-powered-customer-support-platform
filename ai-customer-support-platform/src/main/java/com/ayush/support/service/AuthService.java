package com.ayush.support.service;

import com.ayush.support.api.AuthDtos;
import com.ayush.support.domain.Role;
import com.ayush.support.domain.User;
import com.ayush.support.repository.UserRepository;
import com.ayush.support.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        return tokenFor(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return tokenFor(user);
    }

    private AuthDtos.AuthResponse tokenFor(User user) {
        return new AuthDtos.AuthResponse(
                jwtService.generateToken(user.getEmail(), user.getRole().name()),
                user.getEmail(),
                user.getRole().name());
    }
}
