package com.example.jobApplication.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;

@Service
public class linkedInHelper {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File cookieFile = new File("linkedin_cookies.json");

    private Map<String, Object> cookieToMap(Cookie c) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", c.getName());
        m.put("value", c.getValue());
        m.put("domain", c.getDomain());
        m.put("path", c.getPath());
        m.put("expiry", c.getExpiry() == null ? null : c.getExpiry().getTime());
        m.put("secure", c.isSecure());
        m.put("httpOnly", c.isHttpOnly());
        return m;
    }

    private Cookie mapToCookie(Map<String, Object> m) {
        String name = (String) m.get("name");
        String value = (String) m.get("value");
        String domain = (String) m.get("domain");
        String path = (String) m.get("path");
        Long expiryMillis = m.get("expiry") == null ? null : ((Number) m.get("expiry")).longValue();
        Date expiry = expiryMillis == null ? null : new Date(expiryMillis);
        boolean secure = m.get("secure") != null && (Boolean) m.get("secure");
        boolean httpOnly = m.get("httpOnly") != null && (Boolean) m.get("httpOnly");

        Cookie.Builder builder = new Cookie.Builder(name, value)
                .domain(domain)
                .path(path);

        if (expiry != null)
            builder.expiresOn(expiry);
        if (secure)
            builder.isSecure(true);
        if (httpOnly)
            builder.isHttpOnly(true);

        return builder.build();
    }

    public void saveCookies(WebDriver driver) {
        Set<Cookie> cookies = driver.manage().getCookies();
        List<Map<String, Object>> json = new ArrayList<>();
        for (Cookie c : cookies) {
            json.add(cookieToMap(c));
        }
        try {
            mapper.writeValue(cookieFile, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save cookies", e);
        }
    }

    public void loadCookies(WebDriver driver) {
        Cookie session = driver.manage().getCookieNamed("li_at");

        if (session != null) {
            System.out.println("Cookie exists!");
        }
        try {
            // Must be on LinkedIn domain before adding cookies
            driver.get("https://www.linkedin.com");
            List<Map<String, Object>> json = mapper.readValue(cookieFile,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            for (Map<String, Object> m : json) {
                Cookie c = mapToCookie(m);
                try {
                    driver.manage().addCookie(c);
                } catch (Exception addEx) {
                    // some cookies (e.g. HTTPOnly, Secure) may be rejected — ignore quietly
                }
            }
            // navigate again to apply restored cookies
            driver.get("https://www.linkedin.com/feed/");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load cookies", e);
        }
    }

    public void ensureLoggedIn(WebDriver driver) {
        driver.get("https://www.linkedin.com/feed/");
        if (driver.getCurrentUrl().contains("/login")) {
            System.out.println("Not logged in: please complete login + 2FA in the opened browser.");
        } else {
            System.out.println("Already logged in (session active).");
        }
    }

}
