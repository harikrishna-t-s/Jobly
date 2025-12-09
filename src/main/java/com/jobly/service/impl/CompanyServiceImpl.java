package com.jobly.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobly.exception.ResourceNotFoundException;
import com.jobly.model.Company;
import com.jobly.model.User;
import com.jobly.repository.CompanyRepository;
import com.jobly.service.CompanyService;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public Company createCompanyForUser(User owner, String name, String website, String description) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Company company = new Company();
        company.setName(name);
        company.setWebsite(website);
        company.setDescription(description);
        company.setOwner(owner);
        return companyRepository.save(company);
    }

    @Override
    public Company getCompany(Long id) {
        return companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }
}
