package com.login.LoginPage.Security;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiSchedulerService {

    private final RestTemplate restTemplate = new RestTemplate();

    // fixedRate = 30000 milliseconds (30 seconds)
    @Scheduled(fixedRate = 30000)
    public void callTestApi() {
        try {
        	System.out.println("Scheduled Task started " );
            String url = "https://restobarapp.onrender.com/testAPI";
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Scheduled Task: API Response - " + response);
        } catch (Exception e) {
            System.err.println("Scheduled Task: Failed to call API - " + e.getMessage());
        }
    }
}