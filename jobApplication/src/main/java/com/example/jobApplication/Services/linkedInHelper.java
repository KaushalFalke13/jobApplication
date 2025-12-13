package com.example.jobApplication.Services;

import com.example.jobApplication.Repository.JobData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.*;

@Service
public class linkedInHelper {

    private final List<String> jobsKeywords = List.of("java", "spring boot", "microservices");

    private final ObjectMapper mapper = new ObjectMapper();
    private final File cookieFile = new File("linkedin_cookies.json");

   

    private void scrollToCenterAndClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        element.click();
    }


    public ArrayList<JobData> extractJobDataFromLinkedIn(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        ArrayList<JobData> jobsList = new ArrayList<>();

        driver.get(createURL());

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.scaffold-layout__list")));

        By jobCardLocator = By.cssSelector(
                ".scaffold-layout__list-item, .jobs-search-results__list-item");

        // right panel locators (STABLE)
        By jobDetailsPanel = By.cssSelector("div.jobs-search__job-details");
        By titleLocator = By.cssSelector("h1");
        By companyLocator = By.cssSelector(
                "a.topcard__org-name-link, span.jobs-unified-top-card__company-name");
        By locationLocator = By.cssSelector(
                "span.jobs-unified-top-card__bullet");
        By descriptionLocator = By.cssSelector(
                "div.jobs-description-content");
        By postedTimeLocator = By.xpath(
                "//span[contains(text(),'ago') or contains(text(),'Reposted')]");

        int index = 0;

        while (true) {
            List<WebElement> jobCards = driver.findElements(jobCardLocator);

            if (index >= jobCards.size()) {
                break;
            }

            try {
                WebElement jobCard = jobCards.get(index);

                // scroll + click (LEFT PANEL ONLY)
                scrollToCenterAndClick(driver, jobCard);

                // wait for right panel refresh
                wait.until(ExpectedConditions.presenceOfElementLocated(jobDetailsPanel));

                // extract from RIGHT PANEL ONLY
                String title = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(titleLocator))
                        .getText();

                String company = driver.findElement(companyLocator).getText();
                String location = driver.findElement(locationLocator).getText();
                String description = driver.findElement(descriptionLocator).getText();

                String postedTime = "";
                try {
                    postedTime = driver.findElement(postedTimeLocator)
                            .getText()
                            .replace("Reposted", "")
                            .trim();
                } catch (NoSuchElementException ignored) {
                }

                String jobUrl = driver.getCurrentUrl();
                boolean isEasyApply = driver.getPageSource().toLowerCase().contains("easy apply");

                jobsList.add(new JobData(
                        title,
                        company,
                        location,
                        postedTime,
                        jobUrl,
                        isEasyApply,
                        description));

                index++;

            } catch (StaleElementReferenceException e) {
                // DOM recycled → retry same index
            } catch (Exception e) {
                System.out.println("❌ Error at index " + index + ": " + e.getMessage());
                index++;
            }
        }

        return jobsList;
    }

    /*
     * =========================
     * URL creation
     * =========================
     */

    private String createURL() {
        String jobTitle = "software engineer";
        String location = "India";

        return "https://www.linkedin.com/jobs/search/?keywords="
                + jobTitle.replace(" ", "%20")
                + "&location=" + location.replace(" ", "%20")
                + "&f_TPR=r86400" // past 24 hours
                + "&f_E=2"; // entry level
    }

    /*
     * =========================
     * Cookie handling
     * =========================
     */

    public void saveCookies(WebDriver driver) {
        try {
            List<Map<String, Object>> json = new ArrayList<>();
            for (Cookie c : driver.manage().getCookies()) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", c.getName());
                m.put("value", c.getValue());
                m.put("domain", c.getDomain());
                m.put("path", c.getPath());
                m.put("expiry", c.getExpiry() == null ? null : c.getExpiry().getTime());
                m.put("secure", c.isSecure());
                m.put("httpOnly", c.isHttpOnly());
                json.add(m);
            }
            mapper.writeValue(cookieFile, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save cookies", e);
        }
    }

    public void loadCookies(WebDriver driver) {
        try {
            driver.get("https://www.linkedin.com");
            List<Map<String, Object>> json = mapper.readValue(cookieFile, new TypeReference<>() {
            });
            for (Map<String, Object> m : json) {
                Cookie.Builder builder = new Cookie.Builder((String) m.get("name"), (String) m.get("value"))
                        .domain((String) m.get("domain"))
                        .path((String) m.get("path"));

                if (m.get("expiry") != null) {
                    builder.expiresOn(new Date(((Number) m.get("expiry")).longValue()));
                }
                if (Boolean.TRUE.equals(m.get("secure")))
                    builder.isSecure(true);
                if (Boolean.TRUE.equals(m.get("httpOnly")))
                    builder.isHttpOnly(true);

                try {
                    driver.manage().addCookie(builder.build());
                } catch (Exception ignored) {
                }
            }
            driver.get("https://www.linkedin.com/feed/");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load cookies", e);
        }
    }

    public void ensureLoggedIn(WebDriver driver) {
        driver.get("https://www.linkedin.com/feed/");
        if (driver.getCurrentUrl().contains("/login")) {
            System.out.println("⚠ Please log in manually (2FA) and restart.");
        } else {
            System.out.println("✅ Logged in");
        }
    }

    /*
     * =========================
     * Filtering
     * =========================
     */

    public List<JobData> filterJobs(List<JobData> jobsList) {
        for (JobData job : jobsList) {
            for (String keyword : jobsKeywords) {
                if (job.getDescription().toLowerCase().contains(keyword)) {
                    job.setScore(job.getScore() + 1);
                }
            }
        }

        jobsList.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return jobsList;
    }
}
