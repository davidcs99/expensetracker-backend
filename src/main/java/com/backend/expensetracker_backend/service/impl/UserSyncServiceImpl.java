package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.entity.SubscriptionType;
import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.repository.UserRepository;
import com.backend.expensetracker_backend.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncServiceImpl implements UserSyncService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User getOrCreateUser(String auth0Id, String email, String name, String role) {
        return userRepository.findByAuth0Id(auth0Id)
                .orElseGet(() -> {
                    log.info("Creando nuevo usuario: auth0Id={}, email={}", auth0Id, email);

                    User newUser = new User();
                    newUser.setAuth0Id(auth0Id);
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setSubscription(mapRoleToSubscription(role));
                    newUser.setCreatedAt(LocalDateTime.now());

                    return userRepository.save(newUser);
                });
    }

    @Override
    @Transactional  // ← QUITA el readOnly = true
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new IllegalStateException("No hay usuario autenticado");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String auth0Id = jwt.getSubject(); // "sub" claim
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        // Obtener rol desde el claim personalizado
        String role = jwt.getClaimAsStringList("https://securityApp.com/roles")
                .stream()
                .findFirst()
                .orElse("UserFree");

        return getOrCreateUser(auth0Id, email, name, role);
    }

    private SubscriptionType mapRoleToSubscription(String role) {
        if (role == null) {
            return SubscriptionType.FREE;
        }

        return switch (role.toLowerCase()) {
            case "userpremium" -> SubscriptionType.PREMIUM;
            case "userfree" -> SubscriptionType.FREE;
            default -> SubscriptionType.FREE;
        };
    }
}