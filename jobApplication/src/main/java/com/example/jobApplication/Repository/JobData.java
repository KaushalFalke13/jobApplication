package com.example.jobApplication.Repository;

// import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

// @Entity
@Getter
@Setter
public class JobData {
   
    
    String title, company, location, posted, jobUrl, description;
    boolean isEasyApply;
    int id , score;

public JobData(String title, String company, String location, String posted, String jobUrl, boolean isEasyApply, String description) {
                this.title = title;
                this.company = company;
                this.location = location;
                this.posted = posted;
                this.jobUrl = jobUrl;
                this.isEasyApply = isEasyApply;
                this.description = description;
            }
}
