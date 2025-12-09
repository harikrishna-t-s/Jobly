package com.jobly.service;

import java.util.List;

import com.jobly.dto.job.JobRequest;
import com.jobly.dto.job.JobResponse;
import com.jobly.model.Job;
import com.jobly.model.User;

public interface JobService {

    JobResponse createJob(JobRequest request, User poster);

    List<JobResponse> listOpenJobs();

    List<JobResponse> listJobsForUser(User user);

    Job getJobEntity(Long id);

    JobResponse getJob(Long id);

    JobResponse updateJob(Long id, JobRequest request, User requester);

    void deleteJob(Long id, User requester);

    boolean canManageJob(User requester, Long jobId);
}
