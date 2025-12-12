package com.example.jobApplication.Controllers;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.jobApplication.Repository.JobData;
import com.example.jobApplication.Services.LinkedInService;

@Controller
public class JobApplicationController {

    private final LinkedInService linkedInService;
    private final WebDriver driver;

    public JobApplicationController(LinkedInService linkedInService, WebDriver driver) {
        this.linkedInService = linkedInService;
        this.driver = driver;
    }

    @GetMapping("/runJobApplicationAutomation")
    public void runJobApplicationAutomation() {
        
        try {
            System.out.println("checking for login.");

            linkedInService.ensureLoggedIn(driver);

            System.out.println("Logged in successfully.");
            List<JobData> jobsList = linkedInService.extractJobDataFromLinkedIn(driver);
            List<JobData> filteredJobs = linkedInService.filterJobs(jobsList);
            if (!filteredJobs.isEmpty()) {
                System.out.println(filteredJobs.get(0).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
