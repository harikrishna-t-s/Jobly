package com.jobly.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jobly.dto.application.ApplicationRequest;
import com.jobly.model.User;
import com.jobly.model.enums.ApplicationStatus;
import com.jobly.security.CustomUserDetails;
import com.jobly.service.ApplicationService;
import com.jobly.service.JobService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping
@Tag(name = "Applications", description = "Job application submission and management")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobService jobService;

    public ApplicationController(ApplicationService applicationService, JobService jobService) {
        this.applicationService = applicationService;
        this.jobService = jobService;
    }

    @GetMapping("/jobs/{jobId}/apply")
    @Operation(summary = "Render application form")
    public String applicationForm(@PathVariable Long jobId, Model model) {
        model.addAttribute("applicationRequest", new ApplicationRequest());
        model.addAttribute("job", jobService.getJob(jobId));
        return "applications/apply";
    }

    @PostMapping("/jobs/{jobId}/apply")
    @Operation(summary = "Submit a job application")
    public String submitApplication(@PathVariable Long jobId,
                                    @Valid @ModelAttribute("applicationRequest") ApplicationRequest applicationRequest,
                                    BindingResult bindingResult,
                                    Model model,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("job", jobService.getJob(jobId));
            return "applications/apply";
        }
        applicationRequest.setJobId(jobId);
        User candidate = currentUser.getUser();
        applicationService.applyToJob(applicationRequest, candidate);
        return "redirect:/jobs/" + jobId;
    }

    @GetMapping("/applications/mine")
    @Operation(summary = "List applications for current candidate")
    public String myApplications(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("applications", applicationService.getApplicationsForUser(currentUser.getUser()));
        model.addAttribute("statuses", ApplicationStatus.values());
        return "applications/mine";
    }

    @PostMapping("/applications/{applicationId}/status")
    @Operation(summary = "Update application status")
    public String updateStatus(@PathVariable Long applicationId,
                               @RequestParam ApplicationStatus status,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        ApplicationStatus safeStatus = status != null ? status : ApplicationStatus.SUBMITTED;
        var updated = applicationService.updateApplicationStatus(applicationId, safeStatus, currentUser.getUser());
        Long jobId = updated.getJob().getId();
        return "redirect:/jobs/" + jobId + "/applications";
    }
}
