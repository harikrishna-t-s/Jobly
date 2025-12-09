package com.jobly.service;

import com.jobly.dto.auth.RegisterRequest;
import java.util.List;

import com.jobly.model.Role.RoleName;
import com.jobly.model.User;

public interface UserService {

    User registerUser(RegisterRequest request);

    List<User> getRecentUsers(int limit);

    long countByRole(RoleName roleName);

    void toggleUserEnabled(Long userId);
}
