package com.example.jobApplication.Entity;

import java.time.LocalDateTime;
import com.example.jobApplication.Services.Enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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

    @Column(length = 2048)
    private String jobUrl;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String role;
    private String company;
    private String location;
    private String skills;
    private String emails;
    private String source; // LinkedIn / Naukri / Internshala
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    private LocalDateTime discoveredAt;
    private LocalDateTime lastUpdatedAt;
    private String title, posted;
    private boolean isEasyApply;
    private int score;
}
