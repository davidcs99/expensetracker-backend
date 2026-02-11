package com.backend.expensetracker_backend.service;

import com.backend.expensetracker_backend.entity.SubscriptionType;
import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(String auth0Id, String email, String name, String role) {

        // Buscar si ya existe el usuario
        Optional<User> existingUser = userRepository.findByAuth0Id(auth0Id);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Si no existe, crear un nuevo usuario
        User newUser = new User();
        newUser.setAuth0Id(auth0Id);
        newUser.setEmail(email);
        newUser.setName(name);

        // Determinar el tipo de suscripción según el rol de Auth0
        SubscriptionType subscription = determineSubscription(role);
        newUser.setSubscription(subscription);

        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(newUser);
    }

    private SubscriptionType determineSubscription(String role) {
        if (role != null && role.equalsIgnoreCase("premium")) {
            return SubscriptionType.PREMIUM;
        }
        return SubscriptionType.FREE;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByAuth0Id(String auth0Id) {
        return userRepository.findByAuth0Id(auth0Id);
    }
}