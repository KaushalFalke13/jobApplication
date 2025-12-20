package com.example.jobApplication.Config;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class EnvDebugger {

    @PostConstruct
    public void debug() {
        String key = System.getenv("OPENAI_API_KEY");
        System.out.println("ENV OPENAI_API_KEY = " + key);
    }
}
