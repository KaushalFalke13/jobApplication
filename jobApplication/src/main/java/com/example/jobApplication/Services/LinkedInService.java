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
    // private final String username = "fhalkekaushal13@gmail.com";
    // private final String password = "kaushal@13022003";
    private final List<String> jobsKeywords = List.of("java", "spring boot", "microservices");
    private final ObjectMapper mapper = new ObjectMapper();
    private final File cookieFile = new File("linkedin_cookies.json");

    private String getDataByElement(WebElement jobCard, By s) {
        try {
            WebElement el = jobCard.findElement(s);
            String t = el.getText();
            if (t != null && !t.isBlank())
                return t;
        } catch (Exception ignored) {
        }
        return "";
    }

    public ArrayList<JobData> extractJobDataFromLinkedIn(WebDriver driver) throws TimeoutException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        ArrayList<JobData> jobsList = new ArrayList<>();

        String baseUrl = createURL();

        // iterate pages (example loop with 1 page as original)
        for (int i = 0; i < 1; i++) {
            int start = i * 25;
            String pageUrl = baseUrl + "&start=" + start;
            driver.get(pageUrl);

            wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.cssSelector("ul.jobs-search__results-list, .scaffold-layout__list")));

            List<WebElement> jobCards = driver
                    .findElements(By.cssSelector(".scaffold-layout__list-item, .jobs-search-results__list-item"));

            System.out.println("Found " + jobCards.size() + " job cards on page " + (i + 1));

            for (int j = 0; j < 1; j++) {
                try {
                    WebElement jobCard = jobCards.get(j);

                    // ((JavascriptExecutor)
                    // driver).executeScript("arguments[0].scrollIntoView({block:'center'});",
                    // jobCard);

                    // helper to read text with fallbacks
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
                    String jobUrl = getURL(jobCard);
                    if (jobUrl == null || jobUrl.isBlank()) {
                        System.out.println("❌ Could not find job URL for card index " + j);
                        continue;
                    }

                    String title = getDataByElement(jobCard, By.cssSelector("job-card-list__title"));
                    System.out.println("➡ Title: " + title);

                    String company = getDataByElement(jobCard, By.className("GjFeOYQyNDedVmjThBuNySfGRlrupvgzQ"));
                    System.out.println("➡ Company: " + company);

                    // find job location from the class name and there is a span tag inside it
                    String jobLocation = getDataByElement(jobCard,
                            By.className("TqPYegPEzYXJKvArrSLvTidVpmVOKfHHvzQEhfQ"));

                    // boolean isEasyApply = jobCard.getText().toLowerCase().contains("easy apply");

                    // --- Open jobUrl in a new tab to avoid stale elements ---
                    // String originalHandle = driver.getWindowHandle();
                    // ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],
                    // '_blank');", jobUrl);

                    // switch to the newly opened tab
                    // Set<String> handles = driver.getWindowHandles();
                    // String newHandle = handles.stream().filter(h ->
                    // !h.equals(originalHandle)).findFirst().orElse(null);
                    // if (newHandle == null) {
                    // System.out.println("❌ Could not open new tab for URL: " + jobUrl);
                    // continue;
                    // }
                    // driver.switchTo().window(newHandle);

                    // // wait for job detail content
                    // WebElement detailRoot = null;
                    // try {
                    // By[] detailSelectors = new By[] {
                    // By.id("job-details"),
                    // By.cssSelector(".jobs-description__content"),
                    // By.cssSelector(".jobs-unified-top-card__content"),
                    // By.cssSelector(".jobs-unified-top-card")
                    // };
                    // for (By sel : detailSelectors) {
                    // try {
                    // detailRoot = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
                    // if (detailRoot != null) break;
                    // } catch (Exception ignored) {}
                    // }
                    // } catch (Exception e) {
                    // // will try fallback to body text
                    // }

                    // String description = "";
                    // if (detailRoot != null) {
                    // try {
                    // description = detailRoot.getText();
                    // } catch (Exception e) {
                    // description = driver.findElement(By.tagName("body")).getText();
                    // }
                    // } else {
                    // // fallback
                    // description = driver.findElement(By.tagName("body")).getText();
                    // }

                    // String shortDescription = description.length() > 150 ?
                    // description.substring(0, 150) + "..." : description;
                    // System.out.println("➡ Description (preview): " + shortDescription);

                    // // posted time (try a few selectors)
                    // String postedTime = "";
                    // try {
                    // postedTime =
                    // driver.findElement(By.cssSelector(".jobs-unified-top-card__posted-date,
                    // .posted-time-ago, span[data-test-posted-date]")).getText();
                    // } catch (Exception e1) {
                    // try {
                    // postedTime =
                    // driver.findElement(By.xpath("//span[contains(text(),'ago')]")).getText();
                    // } catch (Exception ignored) {}
                    // }
                    // System.out.println("➡ Posted: " + postedTime);

                    // // add to list
                    // jobsList.add(new JobData(title, company, jobLocation, postedTime, jobUrl,
                    // isEasyApply, description));

                    // // close detail tab and go back to original
                    // driver.close();
                    // driver.switchTo().window(originalHandle);

                    // small explicit wait to ensure results list presence before continuing
                    // wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.jobs-search__results-list,
                    // .scaffold-layout__list")));

                    // Optionally: re-find jobCards to avoid using stale references
                    // jobCards = driver.findElements(By.cssSelector(".scaffold-layout__list-item,
                    // .jobs-search-results__list-item"));

                } catch (Exception e) {
                    System.out.println("❌ Error extracting job data at card index " + j + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return jobsList;
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

    private String getURL(WebElement jobCard) {
        String jobUrl = "";
        try {
            WebElement anchor = jobCard.findElement(By.cssSelector(
                    "a.job-card-container__link, a[href*='/jobs/view/']"));
            jobUrl = anchor.getAttribute("href");
            System.out.println("1 -" + jobUrl);
        } catch (Exception e) {
            try {
                jobUrl = jobCard.findElement(By.cssSelector("a")).getAttribute("href");
                System.out.println("2 - " + jobUrl);
            } catch (Exception ignored) {
            }
        }
        return jobUrl;
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
