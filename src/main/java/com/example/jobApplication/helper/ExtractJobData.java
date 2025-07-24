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

public class ExtractJobData {

  public static ArrayList<JobData> extractJobDataFromLinkedIn(WebDriver driver ){

      ArrayList<JobData> jobsList = new java.util.ArrayList<>();

    // 1. Navigate to the LinkedIn job search page
    String jobTitle = "software engineer";
            String location = "India";
            int totalPages = 5; // Adjust this number for more pages (each = 25 jobs)

            String baseUrl = "https://www.linkedin.com/jobs/search/?keywords=" +
                    jobTitle.replace(" ", "%20") +
                    "&location=" + location.replace(" ", "%20") +
                    "&f_TPR=r86400" + // Past 24 hours
                    "&f_E=2"; // Entry level

  // try {
  //       WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  //       WebElement dismissBtn;
        
  //       if (dismissBtn.isDisplayed()) {
  //           dismissBtn.click(); 
  //           System.out.println("✅ Popup closed.");
  //           Thread.sleep(1000); // wait for UI to stabilize
  //       }  
  //    }catch (Exception e) {
  //       // No popup appeared — all good
  //   } 
    
  
  // 2. Wait for the page to load and display job listings
  WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".jobs-search-results__list-item")));


// 3. Loop through paginated job pages
  for (int i = 0; i < totalPages; i++) {
      int start = i * 25;
      String pageUrl = baseUrl + "&start=" + start;

      driver.get(pageUrl);

      wait.until(
              ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.jobs-search__results-list")));

      List<WebElement> jobCards = driver.findElements(By.cssSelector("ul.jobs-search__results-list li"));

    // 3. Extract job data from the listings
     for (int j = 0; j < jobCards.size(); j++) {
  try {
      WebElement jobCard = jobCards.get(j);

      ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", jobCard);
      jobCard.click();
      Thread.sleep(2000); 

      @SuppressWarnings("deprecation")
      String jobUrl = jobCard.findElement(By.cssSelector("a")).getAttribute("href");
      String title = jobCard.findElement(By.cssSelector("a.job-card-list__title")).getText();
      String company = jobCard.findElement(By.cssSelector("a.job-card-container__company-name"))
              .getText();
      String jobLocation = jobCard
              .findElement(By.cssSelector("ul.job-card-container__metadata-wrapper li")).getText();
      String posted = jobCard.findElement(By.cssSelector("time")).getText();
      boolean isEasyApply = jobCard.getText().toLowerCase().contains("easy apply");

      // Wait and get the full job description
      WebElement descriptionBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
              By.cssSelector("div.jobs-description-content__text")));

      String description = descriptionBox.getText();

      jobsList.add(new JobData(title, company, jobLocation, posted, jobUrl, isEasyApply, description));

                    } catch (Exception e) {
                        System.out.println("❌ Failed to extract job. Skipping.");
                }           
        }
     }
  }


  class JobData {
            String title, company, location, posted, jobUrl, description;
            boolean isEasyApply;

JobData(String title, String company, String location, String posted, String jobUrl, boolean isEasyApply, String description) {
                this.title = title;
                this.company = company;
                this.location = location;
                this.posted = posted;
                this.jobUrl = jobUrl;
                this.isEasyApply = isEasyApply;
                this.description = description;
            }
        }

}