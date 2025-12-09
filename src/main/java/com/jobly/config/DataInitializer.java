package com.jobly.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jobly.model.Company;
import com.jobly.model.Job;
import com.jobly.model.JobApplication;
import com.jobly.model.Role;
import com.jobly.model.Role.RoleName;
import com.jobly.model.User;
import com.jobly.model.enums.ApplicationStatus;
import com.jobly.model.enums.EmploymentType;
import com.jobly.model.enums.JobStatus;
import com.jobly.repository.CompanyRepository;
import com.jobly.repository.JobApplicationRepository;
import com.jobly.repository.JobRepository;
import com.jobly.repository.RoleRepository;
import com.jobly.repository.UserRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jobly.admin.email}")
    private String adminEmail;

    @Value("${jobly.admin.password}")
    private String adminPassword;

    @Value("${jobly.admin.full-name}")
    private String adminFullName;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           CompanyRepository companyRepository,
                           JobRepository jobRepository,
                           JobApplicationRepository jobApplicationRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        seedAdmin();
        seedDummyData();
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

    private void seedDummyData() {
        // Check if dummy data already exists
        if (userRepository.findByEmail("john.doe@example.com").isPresent()) {
            return; // Dummy data already seeded
        }

        Role candidateRole = roleRepository.findByName(RoleName.ROLE_CANDIDATE).orElseThrow();
        Role companyRole = roleRepository.findByName(RoleName.ROLE_COMPANY).orElseThrow();
        Role hiringManagerRole = roleRepository.findByName(RoleName.ROLE_HIRING_MANAGER).orElseThrow();

        // Create Candidates
        User candidate1 = createUser("John Doe", "john.doe@example.com", "+1 555 0101", candidateRole);
        User candidate2 = createUser("Jane Smith", "jane.smith@example.com", "+1 555 0102", candidateRole);
        User candidate3 = createUser("Mike Johnson", "mike.johnson@example.com", "+1 555 0103", candidateRole);

        // Create Company 1 with Hiring Manager
        User hiringManager1 = createUser("Sarah Williams", "sarah.williams@techcorp.com", "+1 555 0201", hiringManagerRole);
        Company techCorp = createCompany(
            "TechCorp Solutions",
            "Leading technology solutions provider specializing in cloud computing and AI",
            "https://techcorp.example.com",
            "123 Tech Street, San Francisco, CA 94105",
            hiringManager1
        );

        // Create Company 2 with Hiring Manager
        User hiringManager2 = createUser("David Brown", "david.brown@innovate.com", "+1 555 0202", hiringManagerRole);
        Company innovateLabs = createCompany(
            "Innovate Labs",
            "Innovative startup focused on mobile app development and user experience",
            "https://innovatelabs.example.com",
            "456 Innovation Ave, Austin, TX 78701",
            hiringManager2
        );

        // Create Jobs for TechCorp
        Job job1 = createJob(
            "Senior Java Developer",
            "We are looking for an experienced Java developer to join our backend team. " +
            "You will work on scalable microservices and cloud-native applications.",
            "San Francisco, CA",
            EmploymentType.FULL_TIME,
            120000,
            180000,
            JobStatus.OPEN,
            techCorp,
            hiringManager1
        );

        Job job2 = createJob(
            "DevOps Engineer",
            "Join our DevOps team to build and maintain CI/CD pipelines, manage cloud infrastructure, " +
            "and ensure high availability of our services.",
            "Remote",
            EmploymentType.FULL_TIME,
            100000,
            150000,
            JobStatus.OPEN,
            techCorp,
            hiringManager1
        );

        Job job3 = createJob(
            "Frontend Developer Intern",
            "Summer internship opportunity for students interested in React and modern web development. " +
            "Great learning experience with mentorship from senior developers.",
            "San Francisco, CA",
            EmploymentType.INTERN,
            25,
            35,
            JobStatus.OPEN,
            techCorp,
            hiringManager1
        );

        // Create Jobs for Innovate Labs
        Job job4 = createJob(
            "Mobile App Developer (iOS)",
            "We're seeking a talented iOS developer to build beautiful and performant mobile applications. " +
            "Experience with Swift and SwiftUI required.",
            "Austin, TX",
            EmploymentType.FULL_TIME,
            90000,
            130000,
            JobStatus.OPEN,
            innovateLabs,
            hiringManager2
        );

        Job job5 = createJob(
            "UX/UI Designer",
            "Creative designer needed to craft intuitive user experiences for our mobile and web applications. " +
            "Portfolio required.",
            "Austin, TX / Remote",
            EmploymentType.CONTRACT,
            80000,
            110000,
            JobStatus.DRAFT,
            innovateLabs,
            hiringManager2
        );

        // Create Job Applications
        createApplication(
            job1,
            candidate1,
            "I am very excited about this opportunity. With 5 years of Java experience and expertise in " +
            "Spring Boot and microservices, I believe I would be a great fit for your team.",
            ApplicationStatus.SUBMITTED
        );

        createApplication(
            job1,
            candidate2,
            "I have been following TechCorp's work in cloud computing and would love to contribute to your projects. " +
            "My background in distributed systems aligns well with this role.",
            ApplicationStatus.UNDER_REVIEW
        );

        createApplication(
            job2,
            candidate3,
            "As a DevOps engineer with 3 years of experience in AWS and Kubernetes, I am confident I can help " +
            "optimize your infrastructure and deployment processes.",
            ApplicationStatus.SUBMITTED
        );

        createApplication(
            job4,
            candidate2,
            "I have developed several iOS apps with Swift and have a strong passion for mobile development. " +
            "I would love to bring my skills to Innovate Labs.",
            ApplicationStatus.SHORTLISTED
        );
    }

    private User createUser(String fullName, String email, String phone, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setRoles(Collections.singleton(role));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Company createCompany(String name, String description, String website, String address, User owner) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        company.setWebsite(website);
        company.setAddress(address);
        company.setOwner(owner);
        return companyRepository.save(company);
    }

    private Job createJob(String title, String description, String location, EmploymentType employmentType,
                         Integer salaryMin, Integer salaryMax, JobStatus status, Company company, User postedBy) {
        Job job = new Job();
        job.setTitle(title);
        job.setDescription(description);
        job.setLocation(location);
        job.setEmploymentType(employmentType);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setStatus(status);
        job.setCompany(company);
        job.setPostedBy(postedBy);
        return jobRepository.save(job);
    }

    private void createApplication(Job job, User candidate, String coverLetter, ApplicationStatus status) {
        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setCoverLetter(coverLetter);
        application.setStatus(status);
        jobApplicationRepository.save(application);
    }
}
