package com.jobly.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobly.dto.application.ApplicationRequest;
import com.jobly.exception.ResourceNotFoundException;
import com.jobly.model.Job;
import com.jobly.model.JobApplication;
import com.jobly.model.User;
import com.jobly.model.enums.ApplicationStatus;
import com.jobly.repository.JobApplicationRepository;
import com.jobly.service.ApplicationService;
import com.jobly.service.JobService;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobService jobService;

    public ApplicationServiceImpl(JobApplicationRepository jobApplicationRepository,
                                  JobService jobService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobService = jobService;
    }

    @Override
    @Transactional
    public JobApplication applyToJob(ApplicationRequest request, User candidate) {
        Job job = jobService.getJobEntity(request.getJobId());

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(request.getResumeUrl());
        application.setStatus(ApplicationStatus.SUBMITTED);

        return jobApplicationRepository.save(application);
    }

    @Override
    public List<JobApplication> getApplicationsForUser(User user) {
        return jobApplicationRepository.findByCandidate(user);
    }

    @Override
    public List<JobApplication> getApplicationsForJob(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId);
    }

    @Override
    @Transactional
    public JobApplication updateApplicationStatus(Long applicationId, ApplicationStatus status, User actor) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        Job job = application.getJob();
        if (!jobService.canManageJob(actor, job.getId())) {
            throw new IllegalArgumentException("You cannot update this application");
        }
        application.setStatus(status);
        return jobApplicationRepository.save(application);
    }
}
