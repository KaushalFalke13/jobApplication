package com.example.jobApplication.Services;

import com.example.jobApplication.Repository.JobData;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
public class LinkedInService {

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

    private void moveMouseAndClick(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element)
                .pause(Duration.ofMillis(random.nextInt(500) + 300))
                .moveByOffset(random.nextInt(8) - 4, random.nextInt(8) - 4)
                .pause(Duration.ofMillis(random.nextInt(300) + 200))
                .click()
                .perform();
    }

    private void scrollElementToCenter(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    private void humanScroll(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        Point p = element.getLocation();
        Dimension d = element.getSize();

        int targetX = p.getX() + d.getWidth() / 2;
        int targetY = p.getY() + d.getHeight() / 2;

        int steps = random.nextInt(10) + 10;

        for (int i = 0; i < steps; i++) {
            int x = targetX + random.nextInt(20) - 10;
            int y = targetY + random.nextInt(20) - 10;
            actions.moveByOffset(x / steps, y / steps)
                    .pause(Duration.ofMillis(random.nextInt(30) + 20));
        }

        actions.click().perform();
    }

    /* -------------------- CORE LOGIC -------------------- */

    private String createURL() {

        String encodedTitle = "Java%20Developer";
        String encodedLocation = "Pune%2C%20Maharashtra%2C%20India";
        String experience = "2";

        return "https://www.linkedin.com/jobs/search/?" +
                "keywords=" + encodedTitle +
                "&location=" + encodedLocation +
                "&f_TPR=r86400" +
                "&f_E=" + experience;
    }

    private void loadAllJobCards(WebDriver driver, WebDriverWait wait) {
        WebElement list = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".scaffold-layout__list")));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        int previousCount = 25;

        while (true) {
            List<WebElement> cards = driver.findElements(
                    By.cssSelector(".scaffold-layout__list-item"));

            if (cards.size() == previousCount)
                break;

            js.executeScript(
                    "arguments[0].scrollTop = arguments[0].scrollHeight;", list);
            randomDelay(1500, 3500);
        }
    }

    public ArrayList<JobData> extractJobsDataFromLinkedIn(WebDriver driver)
            throws TimeoutException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        ArrayList<JobData> jobsList = new ArrayList<>();
        String baseUrl = createURL();

        int maxJobsThisSession = random.nextInt(11) + 45;

        for (int page = 0; page < 2; page++) {
            driver.get(baseUrl + "&start=" + (page * 25));
            randomDelay(4000, 7000);

            loadAllJobCards(driver, wait);

            List<WebElement> cards = driver.findElements(JOB_CARD_LOCATOR);

            for (WebElement card : cards) {

                if (jobsList.size() >= maxJobsThisSession)
                    return jobsList;

                // Random skip (human behavior)
                if (random.nextInt(10) < 2)
                    continue;

                try {
                    scrollElementToCenter(driver, card);
                    randomDelay(800, 2000);
                    moveMouseAndClick(driver, card);
                    WebElement descEl = wait.until(
                            ExpectedConditions.presenceOfElementLocated(JOB_DETAILS_LOCATOR));

                    humanScroll(driver, descEl);

                    String description = descEl.getText();
                    readDelay(description);

                    String title = card.findElement(
                            By.xpath(".//a[contains(@href,'/jobs/view')]//strong"))
                            .getText();

                    String company = card.findElement(
                            By.xpath(".//div[contains(@class,'entity-lockup__subtitle')]//span"))
                            .getText();

                    String location = card.findElement(
                            By.xpath(".//ul[contains(@class,'job-card-container__metadata-wrapper')]//span"))
                            .getText();

                    boolean easyApply = card.getText().toLowerCase().contains("easy apply");

                    String postedTime = driver.findElements(
                            By.xpath("//span[contains(text(),'ago')]"))
                            .stream().findFirst()
                            .map(WebElement::getText).orElse("");

                    String link = card.findElement(
                            By.xpath(".//a[contains(@href, '/jobs/view')]")).getAttribute("href");

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

                    if (random.nextBoolean()) {
                        driver.navigate().back();
                        randomDelay(2000, 4000);
                    }

                } catch (Exception ignored) {
                }
            }
        }
        return jobsList;
    }

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