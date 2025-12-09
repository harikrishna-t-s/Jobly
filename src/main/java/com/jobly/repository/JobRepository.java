package com.jobly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobly.model.Job;
import com.jobly.model.User;
import com.jobly.model.enums.JobStatus;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);

    List<Job> findByPostedBy(User user);
}
