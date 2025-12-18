package com.example.jobApplication.Controllers;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.jobApplication.Repository.JobData;
import com.example.jobApplication.Services.LinkedInService;
import com.example.jobApplication.Services.OpenAIService;
import com.example.jobApplication.Services.linkedInHelper;

@RestController
public class JobApplicationController {

    private final LinkedInService linkedInService;
    private final WebDriver driver;
    private final linkedInHelper linkedInHelper;
    private final OpenAIService openAIService;

    public JobApplicationController(LinkedInService linkedInService, WebDriver driver, linkedInHelper linkedInHelper,
            OpenAIService openAIService) {
        this.linkedInService = linkedInService;
        this.driver = driver;
        this.linkedInHelper = linkedInHelper;
        this.openAIService = openAIService;
    }

    @GetMapping("/collectLinkedInJobsData")
    public ResponseEntity<?> collectLinkedInJobsData() {
        try {
            linkedInHelper.ensureLoggedIn(driver);

            List<JobData> jobsList = linkedInService.extractJobsDataFromLinkedIn(driver);
            List<JobData> filterBestJobs = linkedInService.filterBestJobs(jobsList);
            openAIService.getBestJobs(filterBestJobs);
            return ResponseEntity.ok("Data Extracted");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ResponseEntity<?>) ResponseEntity.badRequest();
    }

    @GetMapping("/collectLinkedInPostData")
    public ResponseEntity<?> collectLinkedInPostData() {
        try {
            linkedInHelper.ensureLoggedIn(driver);
            // linkedInService.collectLinkedInPostData();
            return ResponseEntity.ok("Data Extracted");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ResponseEntity<?>) ResponseEntity.badRequest();
    }

}
