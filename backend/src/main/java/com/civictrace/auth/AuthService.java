package com.civictrace.auth;

import com.civictrace.common.api.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists.");
        }
        User user = new User(request.name().trim(), email, passwordEncoder.encode(request.password()), Role.RESIDENT);
        return userRepository.save(user);
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
