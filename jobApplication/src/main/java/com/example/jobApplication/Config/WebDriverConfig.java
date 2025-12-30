package com.example.jobApplication.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

@Configuration
public class WebDriverConfig {

    @Bean(destroyMethod = "quit")
    public WebDriver chromeDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + System.getenv("USERPROFILE") + "\\linkedin_profile");

        options.addArguments("--window-size=1280,1024");

        return new ChromeDriver(options);
    }
}
