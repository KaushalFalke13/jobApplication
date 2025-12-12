package com.example.jobApplication.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.jobApplication.Repository.JobData;
import com.fasterxml.jackson.core.type.TypeReference;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.Duration;
import java.util.*;

@Service
public class LinkedInService {
    // private final String username = "fhalkekaushal13@gmail.com";
    // private final String password = "kaushal@13022003";
    private final List<String> jobsKeywords = List.of("java", "spring boot", "microservices");
    private final ObjectMapper mapper = new ObjectMapper();
    private final File cookieFile = new File("linkedin_cookies.json");


 public ArrayList<JobData> extractJobDataFromLinkedIn(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        ArrayList<JobData> jobsList = new java.util.ArrayList<>();

        // 1. Navigate to the LinkedIn job search page
        String jobTitle = "software engineer";
        String location = "India";
        // int totalPages = 5;

        String baseUrl = "https://www.linkedin.com/jobs/search/?keywords=" +
                jobTitle.replace(" ", "%20") +
                "&location=" + location.replace(" ", "%20") +
                "&f_TPR=r86400" + // Past 24 hours
                "&f_E=2"; // Entry level

        // 2. Loop through paginated job pages
        for (int i = 0; i < 1; i++) {
            int start = i * 25;
            String pageUrl = baseUrl + "&start=" + start;

            driver.get(pageUrl);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("job-details")));
            List<WebElement> jobCards = driver.findElements(By.cssSelector(".scaffold-layout__list-item"));
            System.out.println("Found " + jobCards.size() + " job cards on page " + (i + 1));
            // 3. Extract job data from the listings
            for (int j = 0; j < jobCards.size(); j++) {
                try {
                    WebElement jobCard = jobCards.get(j);
                    System.out.println("\n============================");
                    System.out.println("Extracting job #" + (j + 1));
                    System.out.println("============================");

                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});",
                            jobCard);

                    // helper for fallback text
                    java.util.function.Function<By[], String> readText = (By[] selectors) -> {
                        for (By s : selectors) {
                            try {
                                WebElement el = jobCard.findElement(s);
                                String t = el.getText();
                                if (t != null && !t.isBlank())
                                    return t.trim();
                            } catch (Exception ignored) {
                            }
                        }
                        return "";
                    };

                    // --- JOB URL ---
                    String jobUrl = "";
                    try {

                        jobUrl = jobCard.findElement(By.cssSelector("a.job-card-list__title")).getAttribute("href");
                    } catch (Exception e) {
                        try {
                            jobUrl = jobCard.findElement(By.cssSelector("a")).getAttribute("href");
                        } catch (Exception ignored) {
                        }
                    }
                    if (jobUrl == null || jobUrl.isBlank())
                        continue;

                    // --- TITLE ---
                    String title = readText.apply(new By[] {
                            By.cssSelector("h3.base-search-card__title"),
                            By.cssSelector("a.job-card-list__title"),
                            By.cssSelector("h3")
                    });

                    // --- COMPANY ---
                    String company = readText.apply(new By[] {
                            By.cssSelector(".base-search-card__subtitle"),
                            By.cssSelector(".job-card-container__company-name"),
                            By.cssSelector("h4")
                    });

                    // --- LOCATION ---
                    String jobLocation = readText.apply(new By[] {
                            By.cssSelector(".job-card-container__metadata-item"),
                            By.cssSelector(".job-search-card__location"),
                            By.cssSelector(".job-card-list__location")
                    });

                    // --- EASY APPLY ---
                    boolean isEasyApply = jobCard.getText().toLowerCase().contains("easy apply");

                    // PRINT BASIC INFO BEFORE CLICK
                    System.out.println("➡ Title:       " + title);
                    System.out.println("➡ Company:     " + company);
                    System.out.println("➡ Location:    " + jobLocation);
                    System.out.println("➡ Easy Apply:  " + isEasyApply);
                    System.out.println("➡ URL:         " + jobUrl);

                    // --- CLICK JOB TO OPEN DETAILS ---
                    jobCard.click();
                    Thread.sleep(1500);

                    // --- WAIT FOR JOB DETAILS PAGE ---
                    By[] detailSelectors = new By[] {
                            By.id("job-details"),
                            By.cssSelector(".jobs-description__content"),
                            By.cssSelector(".jobs-unified-top-card__content")
                    };

                    WebElement detailRoot = null;
                    for (By sel : detailSelectors) {
                        try {
                            detailRoot = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
                            if (detailRoot != null)
                                break;
                        } catch (Exception ignored) {
                        }
                    }

                    // --- DESCRIPTION ---
                    String description = "";
                    if (detailRoot != null) {
                        description = detailRoot.getText();
                    } else {
                        description = driver.findElement(By.tagName("body")).getText();
                    }

                    // print only first 150 characters for readability
                    String shortDescription = description.length() > 150 ? description.substring(0, 150) + "..."
                            : description;
                    System.out.println("➡ Description (preview): " + shortDescription);

                    // --- POSTED TIME ---
                    String postedTime = "";
                    try {
                        postedTime = driver.findElement(By.cssSelector(".jobs-unified-top-card__posted-date"))
                                .getText();
                    } catch (Exception e1) {
                        try {
                            postedTime = driver.findElement(By.xpath("//span[contains(text(),'ago')]")).getText();
                        } catch (Exception ignored) {
                        }
                    }

                    System.out.println("➡ Posted:      " + postedTime);

                    // --- ADD TO LIST ---
                    jobsList.add(
                            new JobData(title, company, jobLocation, postedTime, jobUrl, isEasyApply, description));

                    // --- GO BACK TO RESULTS ---
                    driver.navigate().back();
                    wait.until(ExpectedConditions
                            .presenceOfElementLocated(By.cssSelector("ul.jobs-search__results-list")));

                } catch (Exception e) {
                    System.out.println("❌ Error extracting job data: " + e.getMessage());
                }
            }

        }

        return jobsList;
    }












    private Map<String, Object> cookieToMap(Cookie c) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", c.getName());
        m.put("value", c.getValue());
        m.put("domain", c.getDomain());
        m.put("path", c.getPath());
        m.put("expiry", c.getExpiry() == null ? null : c.getExpiry().getTime());
        m.put("secure", c.isSecure());
        m.put("httpOnly", c.isHttpOnly());
        return m;
    }

    private Cookie mapToCookie(Map<String, Object> m) {
        String name = (String) m.get("name");
        String value = (String) m.get("value");
        String domain = (String) m.get("domain");
        String path = (String) m.get("path");
        Long expiryMillis = m.get("expiry") == null ? null : ((Number) m.get("expiry")).longValue();
        Date expiry = expiryMillis == null ? null : new Date(expiryMillis);
        boolean secure = m.get("secure") != null && (Boolean) m.get("secure");
        boolean httpOnly = m.get("httpOnly") != null && (Boolean) m.get("httpOnly");

        Cookie.Builder builder = new Cookie.Builder(name, value)
                .domain(domain)
                .path(path);

        if (expiry != null)
            builder.expiresOn(expiry);
        if (secure)
            builder.isSecure(true);
        if (httpOnly)
            builder.isHttpOnly(true);

        return builder.build();
    }

    public void saveCookies(WebDriver driver) {
        Set<Cookie> cookies = driver.manage().getCookies();
        List<Map<String, Object>> json = new ArrayList<>();
        for (Cookie c : cookies) {
            json.add(cookieToMap(c));
        }
        try {
            mapper.writeValue(cookieFile, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save cookies", e);
        }
    }

    // Call this at the start of a session to restore cookies
    public void loadCookies(WebDriver driver) {
        Cookie session = driver.manage().getCookieNamed("li_at");

        if (session != null) {
            System.out.println("Cookie exists!");
        }
        try {
            // Must be on LinkedIn domain before adding cookies
            driver.get("https://www.linkedin.com");
            List<Map<String, Object>> json = mapper.readValue(cookieFile,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            for (Map<String, Object> m : json) {
                Cookie c = mapToCookie(m);
                try {
                    driver.manage().addCookie(c);
                } catch (Exception addEx) {
                    // some cookies (e.g. HTTPOnly, Secure) may be rejected — ignore quietly
                }
            }
            // navigate again to apply restored cookies
            driver.get("https://www.linkedin.com/feed/");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load cookies", e);
        }
    }

    public void ensureLoggedIn(WebDriver driver) {
        driver.get("https://www.linkedin.com/feed/");
        if (driver.getCurrentUrl().contains("/login")) {
            System.out.println("Not logged in: please complete login + 2FA in the opened browser.");
        } else {
            System.out.println("Already logged in (session active).");
        }
    }

   
    public List<JobData> filterJobs(List<JobData> jobsList) {
        for (JobData jobData : jobsList) {
            for (String keyword : jobsKeywords) {
                if (jobData.getDescription().contains(keyword)) {
                    jobData.setScore(jobData.getScore() + 1);
                }
            }
        }

        jobsList.stream().filter(j -> j.getScore() > 1)
                .sorted((j1, j2) -> Integer.compare(j2.getScore(), j1.getScore()));
        return jobsList;
    }

}
