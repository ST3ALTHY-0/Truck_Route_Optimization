package com.truckoptimization.common.security;

import java.security.SecureRandom;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.truckoptimization.dto.database.sql.features.user.AppUserEntity;
import com.truckoptimization.dto.database.sql.features.user.AppUserRepository;

@Service
public class UserAccountService implements UserDetailsService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        String normalizedUsername = username.trim();
        if (appUserRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        AppUserEntity user = new AppUserEntity();
        user.setUserId(generateUniqueUserId());
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("ROLE_USER");
        user.setEnabled(true);

        appUserRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUserEntity user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                java.util.List.of(new SimpleGrantedAuthority(user.getRole())));
    }

    private long generateUniqueUserId() {
        // Random positive long reserved for internal/backend tracking.
        long candidate = Math.abs(RANDOM.nextLong());
        while (candidate == 0L || appUserRepository.existsByUserId(candidate)) {
            candidate = Math.abs(RANDOM.nextLong());
        }
        return candidate;
    }
}
