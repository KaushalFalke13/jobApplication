package com.example.jobApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.jobApplication.helper.ExtractJobData;
import com.example.jobApplication.helper.linkedInLogin;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@SpringBootApplication
public class JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);

        try {
            WebDriverManager.chromedriver().setup();
            WebDriver driver = new ChromeDriver();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            linkedInLogin.loginLinkedIn(driver);
            ExtractJobData.extractJobDataFromLinkedIn(driver);
                

            // Write job data to Excel after scraping all pages
            try {
                org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Jobs");
                // Header row
                org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
                String[] headers = {"Title", "Company", "Location", "Posted", "Job URL", "Easy Apply", "Description"};
                for (int h = 0; h < headers.length; h++) {
                    header.createCell(h).setCellValue(headers[h]);
                }
                // Data rows
                for (int r = 0; r < jobsList.size(); r++) {
                    JobData job = jobsList.get(r);
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(r + 1);
                    row.createCell(0).setCellValue(job.title);
                    row.createCell(1).setCellValue(job.company);
                    row.createCell(2).setCellValue(job.location);
                    row.createCell(3).setCellValue(job.posted);
                    row.createCell(4).setCellValue(job.jobUrl);
                    row.createCell(5).setCellValue(job.isEasyApply ? "Yes" : "No");
                    row.createCell(6).setCellValue(job.description);
                }
                // Autosize columns
                for (int c = 0; c < headers.length; c++) {
                    sheet.autoSizeColumn(c);
                }
                // Write to file
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream("jobs.xlsx")) {
                    workbook.write(fos);
                }
                workbook.close();
            } catch (Exception e) {
                System.out.println("❌ Failed to write Excel file: " + e.getMessage());
            }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // finally {
        // Close browser
        // driver.quit();
        // }
    }