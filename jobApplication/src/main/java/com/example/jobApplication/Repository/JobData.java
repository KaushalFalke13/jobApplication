package com.example.jobApplication.Repository;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
// import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobData {

    private String title, company, location, posted, jobUrl, description;
    private boolean isEasyApply;
    private int id, score;
    // @CreationTimestamp
    private LocalDateTime createdAt;
}
