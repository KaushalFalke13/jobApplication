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
import java.util.concurrent.TimeoutException;

@Service
public class LinkedInService {
    private final List<String> jobsKeywords = List.of("java", "spring boot", "microservices");
    private final ObjectMapper mapper = new ObjectMapper();
    private final File cookieFile = new File("linkedin_cookies.json");

    private void scrollElementToCenter(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", element);
    }

    private String getDataByElement(WebElement jobCard, By s) {
        try {
            WebElement el = jobCard.findElement(s);
            String t = el.getText();
            if (t != null && !t.isBlank())
                return t;
        } catch (Exception ignored) {
            System.out.println("Element not found");
        }
        return "";
    }

    private String getURL(WebElement jobCard) {
        String jobUrl = "";
        try {
            WebElement anchor = jobCard.findElement(By.cssSelector(
                    "a.job-card-container__link, a[href*='/jobs/view/']"));
            jobUrl = anchor.getAttribute("href");
        } catch (Exception e) {
            try {
                jobUrl = jobCard.findElement(By.cssSelector("a")).getAttribute("href");
            } catch (Exception ignored) {
            }
        }
        return jobUrl;
    }

    public ArrayList<JobData> extractJobDataFromLinkedIn(WebDriver driver) throws TimeoutException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        ArrayList<JobData> jobsList = new ArrayList<>();
        String baseUrl = createURL();

        for (int i = 0; i < 5; i++) {
            int start = i * 25;
            String pageUrl = baseUrl + "&start=" + start;
            driver.get(pageUrl);

            wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.cssSelector(".scaffold-layout__list")));

            loadAllJobCards(driver, wait);

            List<WebElement> jobCards = driver
                    .findElements(By.cssSelector(".scaffold-layout__list-item"));

            System.out.println("Found " + jobCards.size() + " job cards on page " + (i + 1));

            for (int j = 0; j < jobCards.size(); j++) {
                try {
                    WebElement jobCard = jobCards.get(j);

                    String jobUrl = getURL(jobCard);
                    if (jobUrl == null || jobUrl.isBlank()) {
                        System.out.println("❌ Could not find job URL for card index " + j);
                        continue;
                    }

                    String title = getDataByElement(jobCard, By.tagName("strong"));
                    System.out.println("➡ Title: " + title);

                    String company = getDataByElement(jobCard, By.className("GjFeOYQyNDedVmjThBuNySfGRlrupvgzQ"));
                    System.out.println("➡ Company: " + company);

                    // find job location from the class name and there is a span tag inside it
                    String jobLocation = getDataByElement(jobCard,
                            By.className("TqPYegPEzYXJKvArrSLvTidVpmVOKfHHvzQEhfQ"));
                    System.out.println("➡ Location: " + jobLocation);

                    boolean isEasyApply = jobCard.getText().toLowerCase().contains("easy apply");
                    System.out.println("➡ Easy Apply: " + isEasyApply);

                    scrollElementToCenter(driver, jobCard);
                    Thread.sleep(2000); // allow lazy load

                    jobCard.click();
                    wait.until(ExpectedConditions
                            .presenceOfElementLocated(
                                    By.className("MIQrplfwigEnmEXkKCfrQCCpmYLvSIsHOeIvxDk")));

                    String description = driver.findElement(By.className("MIQrplfwigEnmEXkKCfrQCCpmYLvSIsHOeIvxDk"))
                            .getText();
                    System.out.println("➡ Description length: " + description.length());

                    String postedTime = "";

                    List<WebElement> strongElements = driver.findElements(By.tagName("strong"));
                    for (WebElement strong : strongElements) {
                        String text = strong.getText().trim();
                        if (text.contains("ago")) {
                            postedTime = text.replace("Reposted", "").trim();
                            break;
                        }
                    }
                    System.out.println("➡ Posted: " + postedTime);

                    jobsList.add(
                            new JobData(title, company, jobLocation, postedTime, jobUrl, isEasyApply, description));

                } catch (Exception e) {
                    System.out.println("❌ Error extracting job data at card index " + j + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return jobsList;

    }

    private void loadAllJobCards(WebDriver driver, WebDriverWait wait) {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement leftPanel = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".MIQrplfwigEnmEXkKCfrQCCpmYLvSIsHOeIvxDk")));

        int previousCount = 0;

        while (true) {
            List<WebElement> cards = driver.findElements(
                    By.cssSelector(".scaffold-layout__list-item"));

            int currentCount = cards.size();
            System.out.println("Loaded job cards: " + currentCount);

            if (currentCount == previousCount) {
                break;
            }

            previousCount = currentCount;

            js.executeScript(
                    "arguments[0].scrollTop = arguments[0].scrollHeight;",
                    leftPanel);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private String createURL() {
        String jobTitle = "software engineer";
        String location = "India";

        return "https://www.linkedin.com/jobs/search/?keywords=" +
                jobTitle.replace(" ", "%20") +
                "&location=" + location.replace(" ", "%20") +
                "&f_TPR=r86400" + // Past 24 hours
                "&f_E=2"; // Entry level
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
