package com.example.jobApplication.Repository;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LinkedInJobPost {
    private String role;
    private String postContent;
    private String skills;
    private String location;
    private String source;
    private List<String> email;

}
