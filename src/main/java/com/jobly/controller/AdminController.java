package com.jobly.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobly.model.Role.RoleName;
import com.jobly.service.CompanyService;
import com.jobly.service.JobService;
import com.jobly.service.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final JobService jobService;
    private final CompanyService companyService;
    private final UserService userService;

    public AdminController(JobService jobService,
                           CompanyService companyService,
                           UserService userService) {
        this.jobService = jobService;
        this.companyService = companyService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("jobs", jobService.listOpenJobs());
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("recentUsers", userService.getRecentUsers(5));
        model.addAttribute("candidateCount", userService.countByRole(RoleName.ROLE_CANDIDATE));
        model.addAttribute("companyCount", userService.countByRole(RoleName.ROLE_COMPANY));
        model.addAttribute("hiringManagerCount", userService.countByRole(RoleName.ROLE_HIRING_MANAGER));
        model.addAttribute("superAdminCount", userService.countByRole(RoleName.ROLE_SUPER_ADMIN));
        return "admin/dashboard";
    }

    @PostMapping("/users/{userId}/toggle")
    public String toggleUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.toggleUserEnabled(userId);
        redirectAttributes.addFlashAttribute("successMessage", "User status updated.");
        return "redirect:/admin/dashboard";
    }
}
