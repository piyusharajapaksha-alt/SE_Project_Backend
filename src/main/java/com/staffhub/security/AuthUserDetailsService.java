package com.staffhub.security;

import com.staffhub.repository.AuthRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService
        implements UserDetailsService {

    private final AuthRepository authRepository;

    public AuthUserDetailsService(
            AuthRepository authRepository) {

        this.authRepository =
                authRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        AuthRepository.AuthUserRecord account =
                authRepository.findByEmail(
                        username);

        if (account == null) {

            throw new UsernameNotFoundException(
                    "User account not found");
        }

        if (!account.enabled()) {

            throw new UsernameNotFoundException(
                    "User account is disabled");
        }

        return User
                .withUsername(account.email())
                .password(account.passwordHash())
                .disabled(!account.enabled())
                .authorities("ROLE_USER")
                .build();
    }
}