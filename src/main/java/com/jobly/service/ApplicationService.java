package com.jobly.service;

import java.util.List;

import com.jobly.dto.application.ApplicationRequest;
import com.jobly.model.JobApplication;
import com.jobly.model.User;
import com.jobly.model.enums.ApplicationStatus;

public interface ApplicationService {

    JobApplication applyToJob(ApplicationRequest request, User candidate);

    List<JobApplication> getApplicationsForUser(User user);

    List<JobApplication> getApplicationsForJob(Long jobId);

    JobApplication updateApplicationStatus(Long applicationId, ApplicationStatus status, User actor);
}
