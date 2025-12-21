package com.example.jobApplication.Services;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import com.example.jobApplication.Repository.JobData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpenAIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String OPENAI_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-4.1-mini";

    @Value("${openai.api.key}")
    private String API_KEY;

    @SuppressWarnings("null")
    public List<JobData> getBestJobs(List<JobData> jobs) throws Exception {

        String prompt = buildPrompt(jobs);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "input", prompt,
                "temperature", 0.2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, request, String.class);

        String aiText = extractAiText(response.getBody());
        List<JobData> scoredJobs = parseJobScores(aiText);
        System.out.println("Scored Jobs from AI: " + scoredJobs.size());
        return scoredJobs;
    }

    private String extractAiText(String json) throws Exception {
        JsonNode root = mapper.readTree(json);

        return root
                .path("output")
                .get(0)
                .path("content")
                .get(0)
                .path("text")
                .asText();
    }

    private List<JobData> parseJobScores(String aiText) throws Exception {
        return mapper.readValue(
                aiText,
                new TypeReference<List<JobData>>() {
                });
    }

    private String buildPrompt(List<JobData> jobs) {

        StringBuilder sb = new StringBuilder();

        sb.append(getpromptRules());
        sb.append(getResumeText()).append("\n\n");
        sb.append("Job Listings:\n");

        for (int i = 0; i < jobs.size(); i++) {
            JobData j = jobs.get(i);
            sb.append(i + 1).append(". ")
                    .append("Title: ").append(j.getTitle()).append(" | ")
                    .append("Company: ").append(j.getCompany()).append(" | ")
                    .append("Location: ").append(j.getLocation()).append("\n")
                    .append("Description: ").append(j.getDescription().replaceAll("\\s+", " ")).append("\n\n");
        }
        return sb.toString();
    }

    private final String getResumeText() {
        return "Name: Kaushal Falke\n" +
                "Location: Pune, Maharashtra\n" +
                "Contact:\n" +
                "- Phone: +91 7499069229\n" +
                "- Email: kaushalfalke01@gmail.com\n" +
                "- LinkedIn: linkedin.com/in/kaushalfalke\n" +
                "- GitHub: github.com/kaushalFalke13\n\n" +

                "Summary:\n" +
                "Full Stack Java Developer proficient in Java, Spring Boot, Microservices, Kafka, Redis, and REST APIs. "
                +
                "Built end-to-end web applications using Spring Boot, React, and MySQL with a focus on scalability, " +
                "performance, and reliability. Experienced in authentication workflows and backend optimization. " +
                "Strong foundation in problem-solving and data structures.\n\n" +

                "Education:\n" +
                "Masters in Computer Application (2024 – 2026)\n" +
                "PVG College of Science, Pune, Maharashtra\n" +
                "- Specialized in software development, web technologies, and data structures\n" +
                "- Built projects using Java, Spring Boot, HTML, CSS, and MySQL\n\n" +

                "Bachelor of Computer Science (2021 – 2024)\n" +
                "PVG College of Science, Pune, Maharashtra\n" +
                "- Studied C, C++, Java, Python, Algorithms, Computer Networks, and Operating Systems\n" +
                "- Developed mini-projects involving file handling, web development, and desktop applications\n\n" +

                "Projects:\n" +
                "Scalable Microservices-Based E-Commerce Application\n" +
                "Tech Stack: Spring Boot, Microservices, Spring Security, Kafka, Redis\n" +
                "- Designed a scalable microservices architecture with independently deployable services\n" +
                "- Implemented asynchronous communication using Kafka to improve throughput\n" +
                "- Secured APIs using Spring Security and JWT authentication\n" +
                "- Integrated Redis caching to reduce product API response time by 40%\n" +
                "- Wrote unit tests using JUnit and Mockito with 70% code coverage\n\n" +

                "AI-Powered Job Search Optimization Engine\n" +
                "Tech Stack: Spring Boot, Selenium, Web Scraping, OpenAI API\n" +
                "- Built a backend service to scrape job data with pagination and dynamic content handling\n" +
                "- Automated job extraction using Selenium, reducing manual effort by 75%\n" +
                "- Implemented a relevance-skill matching engine, improving recommendation quality by 45%\n\n" +

                "Technical Skills:\n" +
                "Languages: Java, C++, C, JavaScript, HTML, CSS\n" +
                "Backend & Frameworks: Spring Boot, Spring Security, REST APIs, Microservices, Hibernate, Kafka, Redis\n"
                +
                "Frontend: React, HTML, CSS, JavaScript\n" +
                "Databases: MySQL, PostgreSQL, MongoDB\n" +
                "Testing: JUnit, Mockito, Unit Testing\n" +
                "Tools: Git, Docker, IntelliJ IDEA, VS Code, Postman\n\n" +

                "Additional Information:\n" +
                "- Solved 150+ problems on LeetCode (Data Structures & Algorithms)\n" +
                "- Languages Spoken: English, Hindi, Marathi";

    }

    private final String getpromptRules() {
        return " You are a job matching assistant.\r\n" + //
                "\r\n" + //
                "                Your task:\r\n" + //
                "                Evaluate how well each job matches the candidate profile.\r\n" + //
                "                Score each job from 0 to 10 based ONLY on the provided information.\r\n" + //
                "\r\n" + //
                "                Candidate Profile:\r\n" + //
                "                - Skills: Java, Spring Boot, REST APIs, Microservices, SQL, Kafka, Redis\r\n" + //
                "                - Experience Level: Fresher / Entry-level\r\n" + //
                "                - Preferred Roles: Backend Developer, Java Developer\r\n" + //
                "\r\n" + //
                "                Scoring Rules:\r\n" + //
                "                - Strong Java + Spring Boot backend roles → higher score\r\n" + //
                "                - Entry-level or junior roles → higher score\r\n" + //
                "                - Roles requiring heavy frontend, DevOps, or senior experience → lower score\r\n" + //
                "                - Do NOT assume missing requirements\r\n" + //
                "                - Do NOT hallucinate skills or experience\r\n" + //
                "\r\n" + //
                "                Output Requirements:\r\n" + //
                "                - Select the TOP 20 jobs with the highest scores\r\n" + //
                "                - Return STRICT JSON ONLY (no extra text)\r\n" + //
                "                - Use the exact format below\r\n" + //
                "\r\n" + //
                "                JSON Format:\r\n" + //
                "                [\r\n" + //
                "                  {\r\n" + //
                "                    \"jobNumber\": 1,\r\n" + //
                "                    \"jobTitle\": \"...\",\r\n" + //
                "                    \"company\": \"...\",\r\n" + //
                "                    \"score\": 8.5,\r\n" + //
                "                    \"reason\": \"Short, clear justification\"\r\n" + //
                "                  }\r\n" + //
                "                ]\r\n" + //
                "\r\n" + //
                "                Candidate Resume:\r\n" + //
                "";
    }

}