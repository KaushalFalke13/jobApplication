package com.example.jobApplication.Services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import com.example.jobApplication.Repository.LinkedInJobPost;

@Service
public class LinkedInPostService {

    private final Random random = new Random();
    private int maxPosts = 10;

    private String createPostSearchUrl(String keyword) {
        return "https://www.linkedin.com/search/results/content/?" +
                "keywords=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8) +
                "&datePosted=%22past-24h%22" +
                "&origin=FACETED_SEARCH";
    }

    private void randomDelay(int min, int max) {
        try {
            Thread.sleep(random.nextInt(max - min + 1) + min);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<LinkedInJobPost> extractJobPosts(WebDriver driver) {

        List<LinkedInJobPost> jobPosts = new ArrayList<>();
        Set<Integer> processedHashes = new HashSet<>();

        driver.get(createPostSearchUrl("freshers Java Developer"));
        randomDelay(3000, 6000);

        JavascriptExecutor js = (JavascriptExecutor) driver;

        while (jobPosts.size() < maxPosts) {

            List<WebElement> posts = driver.findElements(
                    By.cssSelector("div.update-components-text"));

            for (WebElement post : posts) {

                if (jobPosts.size() >= maxPosts)
                    break;

                String content;
                try {
                    content = post.getText();
                } catch (Exception e) {
                    continue;
                }

                int hash = content.hashCode();
                if (processedHashes.contains(hash))
                    continue;
                processedHashes.add(hash);

                if (!isJobOpeningPost(content))
                    continue;

                js.executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", post);
                randomDelay(2000, 5000);

                LinkedInJobPost jobPost = LinkedInJobPost.builder()
                        .postContent(content)
                        .role(extractRole(content))
                        .skills(extractSkills(content))
                        .location(extractLocation(content))
                        .email(extractEmails(content))
                        .source("LinkedIn Post")
                        .build();

                jobPosts.add(jobPost);
                randomDelay(1500, 3000);
            }

            // force LinkedIn to load more posts
            js.executeScript("window.scrollBy(0, 1200);");
            randomDelay(2000, 4000);
        }

        return jobPosts;
    }

    private boolean isJobOpeningPost(String text) {
        String t = text.toLowerCase();

        return t.contains("hiring") ||
                t.contains("we are hiring") ||
                t.contains("job opening") ||
                t.contains("vacancy") ||
                t.contains("looking for") ||
                t.contains("open position") ||
                t.contains("immediate joiner");
    }

    private String extractRole(String text) {
        String t = text.toLowerCase();

        Map<String, List<String>> roleKeywords = Map.of(
                "Java Developer", List.of("java developer", "spring boot", "hibernate", "jpa"),
                "Backend Developer", List.of("backend developer", "rest api", "microservices"),
                "Web Developer", List.of("web developer", "html", "css", "javascript"),
                "Frontend Developer", List.of("frontend developer", "react", "angular", "vue"),
                "Full Stack Developer", List.of("full stack", "frontend", "backend"));

        String bestRole = "Not Specified";
        int maxScore = 0;

        for (var entry : roleKeywords.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (t.contains(keyword))
                    score++;
            }

            if (score > maxScore) {
                maxScore = score;
                bestRole = entry.getKey();
            }
        }

        return maxScore >= 2 ? bestRole : "Not Specified";
    }

    private String extractSkills(String text) {
        List<String> skills = new ArrayList<>();
        String t = text.toLowerCase();

        if (t.contains("java"))
            skills.add("Java");
        if (t.contains("spring boot"))
            skills.add("Spring Boot");
        if (t.contains("sql"))
            skills.add("SQL");
        if (t.contains("hibernate"))
            skills.add("Hibernate");
        if (t.contains("react"))
            skills.add("React");

        return String.join(", ", skills);
    }

    private String extractLocation(String text) {
        String t = text.toLowerCase();

        if (t.contains("pune"))
            return "Pune";
        if (t.contains("bangalore"))
            return "Bangalore";
        if (t.contains("remote"))
            return "Remote";

        return "Not Mentioned";
    }

    private List<String> extractEmails(String text) {
        List<String> emails = new ArrayList<>();

        if (text == null || text.isEmpty())
            return emails;

        String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            emails.add(matcher.group());
        }

        return emails;
    }

}