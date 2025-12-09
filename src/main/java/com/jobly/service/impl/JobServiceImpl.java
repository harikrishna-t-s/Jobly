package com.jobly.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobly.dto.job.JobRequest;
import com.jobly.dto.job.JobResponse;
import com.jobly.exception.ResourceNotFoundException;
import com.jobly.model.Company;
import com.jobly.model.Job;
import com.jobly.model.Role;
import com.jobly.model.User;
import com.jobly.model.enums.EmploymentType;
import com.jobly.model.enums.JobStatus;
import com.jobly.repository.JobRepository;
import com.jobly.service.CompanyService;
import com.jobly.service.JobService;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyService companyService;

    public JobServiceImpl(JobRepository jobRepository, CompanyService companyService) {
        this.jobRepository = jobRepository;
        this.companyService = companyService;
    }

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request, User poster) {
        Company company = companyService.getCompany(request.getCompanyId());

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setEmploymentType(EmploymentType.valueOf(request.getEmploymentType()));
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setStatus(JobStatus.OPEN);
        job.setCompany(company);
        job.setPostedBy(poster);

        Job saved = jobRepository.save(job);
        return toResponse(saved);
    }

    @Override
    public List<JobResponse> listOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> listJobsForUser(User user) {
        return jobRepository.findByPostedBy(user)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Job getJobEntity(Long id) {
        return jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @Override
    public JobResponse getJob(Long id) {
        Job job = getJobEntity(id);
        return toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long id, JobRequest request, User requester) {
        Job job = getJobEntity(id);
        ensureCanManage(requester, job);

        if (request.getCompanyId() != null) {
            Company company = companyService.getCompany(request.getCompanyId());
            job.setCompany(company);
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setEmploymentType(EmploymentType.valueOf(request.getEmploymentType()));
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());

        Job updated = jobRepository.save(job);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteJob(Long id, User requester) {
        Job job = getJobEntity(id);
        ensureCanManage(requester, job);
        jobRepository.delete(job);
    }

    private void ensureCanManage(User requester, Job job) {
        if (!canManageJob(requester, job.getId())) {
            throw new IllegalArgumentException("You cannot modify this job");
        }
    }

    @Override
    public boolean canManageJob(User requester, Long jobId) {
        Job job = getJobEntity(jobId);
        boolean isPoster = job.getPostedBy() != null && job.getPostedBy().getId().equals(requester.getId());
        boolean isSuperAdmin = requester.getRoles().stream()
            .anyMatch(role -> role.getName() == Role.RoleName.ROLE_SUPER_ADMIN);
        return isPoster || isSuperAdmin;
    }

    private JobResponse toResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setEmploymentType(job.getEmploymentType().name());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setStatus(job.getStatus().name());
        if (job.getCompany() != null) {
            response.setCompanyId(job.getCompany().getId());
            response.setCompanyName(job.getCompany().getName());
        }
        if (job.getPostedBy() != null) {
            response.setPostedBy(job.getPostedBy().getFullName());
            response.setPostedByEmail(job.getPostedBy().getEmail());
        }
        response.setCreatedAt(job.getCreatedAt());
        return response;
    }
}
