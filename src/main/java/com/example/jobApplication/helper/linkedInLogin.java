package com.example.jobApplication.helper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class linkedInLogin {
    private static String username = "fhalkekaushal13@gmail.com";
    private static String password = "kaushal@13022003";

    public static void loginLinkedIn(WebDriver driver){
        try {
            driver.get("https://www.linkedin.com/login");

            driver.manage().window().maximize();

            WebElement emailField = driver.findElement(By.id("username"));
            emailField.sendKeys(username);  

            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys(password);  

            WebElement signInButton = driver.findElement(By.xpath("//button[@type='submit']"));
            signInButton.click();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
