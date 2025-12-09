package com.jobly.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jobly.model.Role;
import com.jobly.model.Role.RoleName;
import com.jobly.model.User;
import com.jobly.repository.RoleRepository;
import com.jobly.repository.UserRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jobly.admin.email}")
    private String adminEmail;

    @Value("${jobly.admin.password}")
    private String adminPassword;

    @Value("${jobly.admin.full-name}")
    private String adminFullName;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        seedAdmin();
    }

    private void seedRoles() {
        Arrays.stream(RoleName.values()).forEach(roleName ->
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                return roleRepository.save(role);
            })
        );
    }

    private void seedAdmin() {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }

        userRepository.findByEmail(adminEmail).orElseGet(() -> {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN missing"));

            User admin = new User();
            admin.setFullName(adminFullName != null ? adminFullName : "Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Collections.singleton(adminRole));
            admin.setEnabled(true);
            return userRepository.save(admin);
        });
    }
}
