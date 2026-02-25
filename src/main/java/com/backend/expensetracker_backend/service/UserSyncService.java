package com.backend.expensetracker_backend.service;

import com.backend.expensetracker_backend.entity.User;

public interface UserSyncService {

    User getOrCreateUser(String auth0Id, String email, String name, String role);

    User getCurrentUser();
}