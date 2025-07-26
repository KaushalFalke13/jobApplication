package com.example.jobApplication.helper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.jobApplication.Repository.JobData;

public class ExtractJobData {

  public static ArrayList<JobData> extractJobDataFromLinkedIn(WebDriver driver) {

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    ArrayList<JobData> jobsList = new java.util.ArrayList<>();

    // 1. Navigate to the LinkedIn job search page
    String jobTitle = "software engineer";
    String location = "India";
    int totalPages = 5;

    String baseUrl = "https://www.linkedin.com/jobs/search/?keywords=" +
        jobTitle.replace(" ", "%20") +
        "&location=" + location.replace(" ", "%20") +
        "&f_TPR=r86400" + // Past 24 hours
        "&f_E=2"; // Entry level

    // 2. Loop through paginated job pages
    for (int i = 0; i < totalPages; i++) {
    int start = i * 25;
    String pageUrl = baseUrl + "&start=" + start;

    driver.get(pageUrl);

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".fIPvHriRZGzoNhZfdzYSlfTgbEvyrECFataA")));
    List<WebElement> jobCards = driver.findElements(By.cssSelector(".scaffold-layout__list-item"));

    // 3. Extract job data from the listings
    for (int j = 0; j < jobCards.size(); j++) {
      try {
        WebElement jobCard = jobCards.get(j);
        System.out.println("Extracting job data from Linkedin");

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",jobCard);
        @SuppressWarnings("deprecation")
        String jobUrl = jobCard.findElement(By.cssSelector("a")).getAttribute("href");
        String title = jobCard.findElement(By.cssSelector("strong")).getText();
        String company = jobCard.findElement(By.cssSelector(".NgIUdWClqOnDhETtFxHHsQgSUSBvQupbhIY")).getText();
        String jobLocation = jobCard.findElement(By.cssSelector(".gRgaVAJMHJamugASuBgzSlUePLuetFgtvc span")).getText();
        boolean isEasyApply = jobCard.getText().contains("Easy Apply");
        jobCard.click();
        Thread.sleep(2000);
        String postedTime = driver.findElement(By.cssSelector(".tvm__text--positive strong span")).getText();

        WebElement descriptionBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#job-details")));
        String description = descriptionBox.getText();
        jobsList.add(new JobData(title, company, jobLocation, postedTime, jobUrl,isEasyApply, description));

      } catch (Exception e) {
        System.out.println("Error extracting job data");
      }
    }
    }
    return jobsList;

  }

}