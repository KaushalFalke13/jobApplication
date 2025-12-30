package com.example.jobApplication.helper;

import java.util.List;

import com.example.jobApplication.Repository.JobData;

public class filterJobData {
    static List<String> jobsKeywords = List.of("java", "spring boot", "microservices");

    public static List<JobData> filterJobs(List<JobData> jobsList) {
        for (JobData jobData : jobsList) {
            for(String keyword: jobsKeywords){
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
