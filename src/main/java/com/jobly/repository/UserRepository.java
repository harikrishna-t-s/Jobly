package com.jobly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jobly.model.Role.RoleName;
import com.jobly.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByRolesName(RoleName roleName);

    List<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
