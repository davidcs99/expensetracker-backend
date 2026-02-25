package com.backend.expensetracker_backend.controller;

import com.backend.expensetracker_backend.service.impl.AppConfigServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final AppConfigServiceImpl appConfigService;

    @GetMapping("/singleton-proof")
    public ResponseEntity<Map<String, Object>> getSingletonProof() {
        long instanceTime = appConfigService.getInstanceCreationTime();

        Map<String, Object> response = new HashMap<>();
        response.put("instanceId", instanceTime);
        response.put("currentTime", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}