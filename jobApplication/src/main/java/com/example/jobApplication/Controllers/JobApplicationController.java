package com.example.jobApplication.Controllers;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.jobApplication.Repository.JobData;
import com.example.jobApplication.Services.LinkedInJobService;
import com.example.jobApplication.Services.OpenAIService;
import com.example.jobApplication.Services.linkedInHelper;

@RestController
public class JobApplicationController {

    private final LinkedInJobService linkedInService;
    private final WebDriver driver;
    private final linkedInHelper linkedInHelper;
    // private final OpenAIService openAIService;

    public JobApplicationController(
            LinkedInJobService linkedInService,
            WebDriver driver,
            linkedInHelper linkedInHelper,
            OpenAIService openAIService) {

        this.linkedInService = linkedInService;
        this.driver = driver;
        this.linkedInHelper = linkedInHelper;
        // this.openAIService = openAIService;
    }

    @GetMapping("/collectLinkedInJobsData")
    public ResponseEntity<List<JobData>> collectLinkedInJobsData() {
        try {
            linkedInHelper.ensureLoggedIn(driver);

            List<JobData> jobsList = linkedInService.extractJobsDataFromLinkedIn(driver);
            System.out.println("Total Jobs Extracted: " + jobsList.size());

            List<JobData> filteredJobs = linkedInService.filterBestJobs(jobsList);
            System.out.println("Filtered Best Jobs: " + filteredJobs.size());

            // List<JobData> bestJobs = openAIService.getBestJobs(filteredJobs);
            // System.out.println("Top Jobs: " + sortedBestJobs.size());

            // ✅ RETURN ACTUAL DATA
            return ResponseEntity.ok(filteredJobs);

        } catch (Exception e) {
            e.printStackTrace();

            // ✅ NO CASTING
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping("/collectLinkedInPostData")
    public ResponseEntity<String> collectLinkedInPostData() {
        try {
            linkedInHelper.ensureLoggedIn(driver);
            return ResponseEntity.ok("Post data extracted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to extract post data");
        }
    }
}
