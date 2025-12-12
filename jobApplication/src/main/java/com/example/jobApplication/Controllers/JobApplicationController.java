package com.example.jobApplication.Controllers;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.jobApplication.Repository.JobData;
import com.example.jobApplication.Services.LinkedInService;

@RestController
public class JobApplicationController {

    private final LinkedInService linkedInService;
    private final WebDriver driver;

    public JobApplicationController(LinkedInService linkedInService, WebDriver driver) {
        this.linkedInService = linkedInService;
        this.driver = driver;
    }

    @GetMapping("/runJobApplicationAutomation")
    public String runJobApplicationAutomation() {

        try {
            System.out.println("checking for login.");

            linkedInService.ensureLoggedIn(driver);

            System.out.println("Logged in successfully.");
            List<JobData> jobsList = linkedInService.extractJobDataFromLinkedIn(driver);
            System.out.println("Jobs data Extracted: " + jobsList.size());
            // List<JobData> filteredJobs = linkedInService.filterJobs(jobsList);
            // if (!filteredJobs.isEmpty()) {
            // System.out.println(filteredJobs.get(0).toString());
            // }
            return "Data Extracted";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Process failed";
    }
}
