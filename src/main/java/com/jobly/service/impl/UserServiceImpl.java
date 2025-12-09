package com.jobly.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobly.dto.auth.RegisterRequest;
import com.jobly.exception.ResourceNotFoundException;
import com.jobly.model.Company;
import com.jobly.model.Role;
import com.jobly.model.Role.RoleName;
import com.jobly.model.User;
import com.jobly.repository.RoleRepository;
import com.jobly.repository.UserRepository;
import com.jobly.service.CompanyService;
import com.jobly.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           CompanyService companyService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyService = companyService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        RoleName roleName = RoleName.valueOf(request.getRole());
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(role));

        userRepository.save(user);

        if (roleName == RoleName.ROLE_COMPANY || roleName == RoleName.ROLE_HIRING_MANAGER) {
            Company company = companyService.createCompanyForUser(
                user,
                request.getCompanyName(),
                request.getCompanyWebsite(),
                request.getCompanyDescription()
            );
            company.setOwner(user);
        }

        return user;
    }

    @Override
    public List<User> getRecentUsers(int limit) {
        return userRepository.findAllByOrderByCreatedAtDesc(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Override
    public long countByRole(RoleName roleName) {
        return userRepository.countByRolesName(roleName);
    }

    @Override
    @Transactional
    public void toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}
