package com.jobly.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobly.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
