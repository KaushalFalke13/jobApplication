package com.example.jobApplication.Services;

import com.example.jobApplication.Repository.JobData;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;

@Service
public class LinkedInJobService {

    private static final By JOB_CARD_LOCATOR = By.cssSelector("div[data-job-id]");
    private static final By JOB_DETAILS_LOCATOR = By.cssSelector("div.jobs-description__content");
    private final Random random = new Random();
    private static final Map<String, Integer> SKILL_WEIGHTS = Map.of(
            "java", 4,
            "spring boot", 5,
            "spring", 3,
            "microservices", 4,
            "hibernate", 2,
            "rest", 2,
            "Kafka", 2,
            "Redis", 2,
            "docker", 2,
            "sql", 2);

    /* -------------------- HUMAN UTILITIES -------------------- */

    private void randomDelay(int min, int max) {
        try {
            Thread.sleep(random.nextInt(max - min + 1) + min);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void readDelay(String text) {
        int words = text.split("\\s+").length;
        int delay = Math.min(8000, 1200 + words * 12);
        randomDelay(delay, delay + 1500);
    }

    private void safeClick(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        randomDelay(300, 600);

        try {
            new Actions(driver)
                    .moveToElement(element)
                    .pause(Duration.ofMillis(200))
                    .click()
                    .perform();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    /* -------------------- CORE LOGIC -------------------- */

    private String createURL(int page) {
        int start = (page - 1) * 25;

        return "https://www.linkedin.com/jobs/search/?" +
                "keywords=Java%20Developer" +
                "&location=Pune%2C%20Maharashtra%2C%20India" +
                "&f_TPR=r86400" +
                "&f_E=2" +
                "&start=" + start;
    }

    /**
     * Scroll job list until LinkedIn stops loading more cards
     */
    private void loadAllJobCards(WebDriver driver, WebDriverWait wait) {
        WebElement list = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".scaffold-layout__list")));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        int previousCount = 0;

        while (true) {
            List<WebElement> cards = driver.findElements(By.cssSelector(".scaffold-layout__list-item"));

            if (cards.size() == previousCount)
                break;

            previousCount = cards.size();

            js.executeScript(
                    "arguments[0].scrollTop = arguments[0].scrollHeight;", list);

            randomDelay(1500, 3000);
        }
    }

    /* -------------------- EXTRACTION -------------------- */

    public ArrayList<JobData> extractJobsDataFromLinkedIn(WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        ArrayList<JobData> jobsList = new ArrayList<>();

        int maxJobsThisSession = random.nextInt(11) + 45;
        for (int j = 0; j < 3; j++) {

            driver.get(createURL(j));
            randomDelay(4000, 7000);
            loadAllJobCards(driver, wait);

            for (int i = 0; i < driver.findElements(JOB_CARD_LOCATOR).size(); i++) {

                if (jobsList.size() >= maxJobsThisSession)
                    break;

                if (random.nextInt(10) < 2)
                    continue;

                try {
                    // Re-fetch card to avoid stale reference
                    WebElement card = driver.findElements(JOB_CARD_LOCATOR).get(i);

                    // Click card (updates right panel)
                    safeClick(driver, card);

                    // Wait for new description
                    WebElement descEl = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(JOB_DETAILS_LOCATOR));
                    String description = descEl.getText();
                    // System.out.println("Description -" + description.length());
                    readDelay(description);

                    String title = card.findElement(
                            By.xpath(".//a[contains(@href,'/jobs/view')]//span[@aria-hidden='true']//strong"))
                            .getText();
                    // System.out.println("title -" + title);

                    String company = card.findElement(
                            By.xpath(".//div[contains(@class,'entity-lockup__subtitle')]//span"))
                            .getText();
                    // System.out.println("company -" + company);

                    String location = card.findElement(
                            By.xpath(".//ul[contains(@class,'job-card-container__metadata-wrapper')]/li[1]//span"))
                            .getText();
                    // System.out.println("location -" + location);

                    boolean easyApply = card.getText().toLowerCase().contains("easy apply");

                    List<By> selectors = List.of(
                            By.xpath(".//strong/span[contains(text(),'ago')]"),
                            By.xpath(".//span[contains(text(),'ago')]"));
                    String postedTime = "";
                    for (By selector : selectors) {
                        try {
                            String Time = card.findElement(selector).getText();
                            if (Time != null && !Time.isBlank()) {
                                postedTime = Time;
                                break;
                            }
                        } catch (Exception e) {
                        }
                    }
                    // System.out.println("postedTime -" + postedTime);

                    String link = card.findElement(
                            By.xpath(".//a[contains(@href, '/jobs/view')]"))
                            .getAttribute("href");
                    // System.out.println("link -" + link.length());

                    JobData jobData = JobData.builder()
                            .title(title)
                            .company(company)
                            .location(location)
                            .posted(postedTime)
                            .jobUrl(link)
                            .isEasyApply(easyApply)
                            .description(description)
                            .build();
                    jobsList.add(jobData);

                    randomDelay(1200, 2500);

                } catch (StaleElementReferenceException ignored) {
                    // LinkedIn DOM refresh — just skip this card
                } catch (Exception e) {
                    System.out.println("Error extracting job: " + e.getMessage());
                }
            }
        }
        return jobsList;
    }

    /* -------------------- SCORING -------------------- */

    private int calculateScore(JobData job) {

        int score = 0;
        String title = job.getTitle().toLowerCase();
        String desc = job.getDescription().toLowerCase();

        for (var entry : SKILL_WEIGHTS.entrySet()) {
            if (title.contains(entry.getKey()) || desc.contains(entry.getKey())) {
                score += entry.getValue();
            }
        }

        if (job.isEasyApply())
            score += 3;
        if (desc.contains("5+ years") || desc.contains("5 years"))
            score -= 3;
        if (desc.contains("7+ years") || desc.contains("8+ years"))
            score -= 5;
        if (title.contains("senior") || title.contains("lead"))
            score -= 4;
        if (title.contains("principal") || title.contains("staff"))
            score -= 6;
        if (desc.contains("0-2 years") || desc.contains("freshers"))
            score += 3;

        return Math.max(score, 0);
    }

    public List<JobData> filterBestJobs(List<JobData> jobs) {

        jobs.forEach(job -> job.setScore(calculateScore(job)));

        return jobs.stream()
                .filter(job -> job.getScore() >= 7)
                .sorted(Comparator.comparingInt(JobData::getScore).reversed())
                .limit(30)
                .toList();
    }

}
