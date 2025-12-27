package com.example.jobApplication.Repository;

import java.time.LocalDateTime;
import com.example.jobApplication.Services.Enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;
    private String company;
    private String location;
    private String description;
    private String skills;
    private String emails;
    private String source; // LinkedIn / Naukri / Internshala
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    private LocalDateTime discoveredAt;
    private LocalDateTime lastUpdatedAt;
    private String title, posted, jobUrl;
    private boolean isEasyApply;
    private int score;
}
