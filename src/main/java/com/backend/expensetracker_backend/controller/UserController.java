package com.backend.expensetracker_backend.controller;

import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSyncService userSyncService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        User user = userSyncService.getCurrentUser();

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "auth0Id", user.getAuth0Id(),
                "email", user.getEmail(),
                "name", user.getName() != null ? user.getName() : "",
                "subscription", user.getSubscription().name()
        ));
    }
}