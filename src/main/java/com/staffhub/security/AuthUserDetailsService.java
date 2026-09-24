package com.staffhub.security;

import com.staffhub.repository.AuthRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

    public AuthUserDetailsService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AuthRepository.AuthUserRecord account =
                authRepository.findByEmail(email);

        if (account == null) {
            throw new UsernameNotFoundException(
                    "Invalid email or password"
            );
        }

        if (!account.enabled()) {
            throw new UsernameNotFoundException(
                    "This account is disabled"
            );
        }

        String role = account.employee().getRole();

        if (role == null || role.isBlank()) {
            role = "Employee";
        }

        return User.builder()
                .username(account.email())
                .password(account.passwordHash())
                .roles(normalizeRole(role))
                .build();
    }

    private String normalizeRole(String role) {

        return role
                .trim()
                .replaceAll("\\s+", "_")
                .toUpperCase();
    }
}