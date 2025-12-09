package com.jobly.service;

import java.util.List;

import com.jobly.model.Company;
import com.jobly.model.User;

public interface CompanyService {

    Company createCompanyForUser(User owner, String name, String website, String description);

    Company getCompany(Long id);

    List<Company> getAllCompanies();
}
