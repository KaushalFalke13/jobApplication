package com.example.jobApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.jobApplication.Repository.JobData;
import com.example.jobApplication.helper.ExtractJobData;
import com.example.jobApplication.helper.filterJobData;
import com.example.jobApplication.helper.linkedInLogin;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@SpringBootApplication
public class JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);

        try {
            WebDriverManager.chromedriver().setup();
            WebDriver driver = new ChromeDriver();


            linkedInLogin.loginLinkedIn(driver);
            List<JobData> jobsList =ExtractJobData.extractJobDataFromLinkedIn(driver);
            List<JobData> filteredJobs = filterJobData.filterJobs(jobsList);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        // finally {
        // Close browser
        // driver.quit();
        // }
    }
}