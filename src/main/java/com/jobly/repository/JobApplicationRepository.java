package com.jobly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobly.model.JobApplication;
import com.jobly.model.User;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCandidate(User candidate);

    List<JobApplication> findByJobId(Long jobId);
}
