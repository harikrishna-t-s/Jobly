package com.jobly.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jobly.dto.job.JobRequest;
import com.jobly.dto.job.JobResponse;
import com.jobly.model.User;
import com.jobly.security.CustomUserDetails;
import com.jobly.service.ApplicationService;
import com.jobly.service.CompanyService;
import com.jobly.service.JobService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping({"/", "/jobs"})
@Tag(name = "Jobs", description = "Job browsing and management endpoints")
public class JobController {

    private final JobService jobService;
    private final CompanyService companyService;
    private final ApplicationService applicationService;

    public JobController(JobService jobService, CompanyService companyService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.companyService = companyService;
        this.applicationService = applicationService;
    }

    @GetMapping
    @Operation(summary = "List open jobs")
    public String listJobs(Model model) {
        List<JobResponse> jobs = jobService.listOpenJobs();
        model.addAttribute("jobs", jobs);
        return "jobs/list";
    }

    @GetMapping("/mine")
    public String myJobs(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        List<JobResponse> jobs = jobService.listJobsForUser(currentUser.getUser());
        model.addAttribute("jobs", jobs);
        return "jobs/manage";
    }

    @GetMapping("/new")
    public String newJobForm(Model model) {
        model.addAttribute("jobRequest", new JobRequest());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "jobs/new";
    }

    @PostMapping
    @Operation(summary = "Create a job")
    public String createJob(@Valid @ModelAttribute("jobRequest") JobRequest jobRequest,
                            BindingResult bindingResult,
                            Model model,
                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("companies", companyService.getAllCompanies());
            return "jobs/new";
        }
        User poster = currentUser.getUser();
        jobService.createJob(jobRequest, poster);
        return "redirect:/jobs";
    }

    @GetMapping("/{id}")
    @Operation(summary = "View job details")
    public String jobDetail(@PathVariable Long id,
                            Model model,
                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        JobResponse job = jobService.getJob(id);
        model.addAttribute("job", job);
        if (currentUser != null) {
            boolean canManage = jobService.canManageJob(currentUser.getUser(), id);
            model.addAttribute("canManage", canManage);
        }
        return "jobs/detail";
    }

    @GetMapping("/{id}/edit")
    public String editJob(@PathVariable Long id,
                          Model model,
                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        JobResponse job = jobService.getJob(id);
        if (!jobService.canManageJob(currentUser.getUser(), id)) {
            return "redirect:/jobs/" + id;
        }
        JobRequest request = new JobRequest();
        request.setTitle(job.getTitle());
        request.setDescription(job.getDescription());
        request.setLocation(job.getLocation());
        request.setEmploymentType(job.getEmploymentType());
        request.setSalaryMin(job.getSalaryMin());
        request.setSalaryMax(job.getSalaryMax());
        request.setCompanyId(job.getCompanyId());
        model.addAttribute("jobRequest", request);
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("jobId", id);
        return "jobs/edit";
    }

    @PostMapping("/{id}")
    public String updateJob(@PathVariable Long id,
                            @Valid @ModelAttribute("jobRequest") JobRequest jobRequest,
                            BindingResult bindingResult,
                            Model model,
                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("companies", companyService.getAllCompanies());
            model.addAttribute("jobId", id);
            return "jobs/edit";
        }
        jobService.updateJob(id, jobRequest, currentUser.getUser());
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteJob(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        jobService.deleteJob(id, currentUser.getUser());
        return "redirect:/jobs/mine";
    }

    @GetMapping("/{id}/applications")
    public String jobApplications(@PathVariable Long id,
                                  Model model,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!jobService.canManageJob(currentUser.getUser(), id)) {
            return "redirect:/jobs/" + id;
        }
        model.addAttribute("job", jobService.getJob(id));
        model.addAttribute("applications", applicationService.getApplicationsForJob(id));
        model.addAttribute("statuses", com.jobly.model.enums.ApplicationStatus.values());
        return "applications/job-list";
    }
}
