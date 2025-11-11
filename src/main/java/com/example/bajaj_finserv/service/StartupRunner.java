package com.example.bajaj_finserv.service;

import com.example.bajaj_finserv.model.SolutionRequest;
import com.example.bajaj_finserv.model.WebhookRequest;
import com.example.bajaj_finserv.model.WebhookResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StartupRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;

    public StartupRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("--- [TASK STARTED] ---");

        try {
            System.out.println("1. Generating Webhook...");

            WebhookRequest webhookRequest = new WebhookRequest();
           
            webhookRequest.setName("Prakruthi GP");
            webhookRequest.setRegNo("PES1UG22EC204"); 
            webhookRequest.setEmail("pes1202203666@pesu.pes.edu");

            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            WebhookResponse response = restTemplate.postForObject(
                    generateUrl,
                    webhookRequest,
                    WebhookResponse.class
            );

            if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
                System.err.println("Error: Did not receive valid webhook or access token.");
                return;
            }

            String webhookUrl = response.getWebhook();
            String accessToken = response.getAccessToken();
            System.out.println("   > Success. Received webhook and token.");

            System.out.println("2. Preparing SQL Solution...");

            String myFinalSqlQuery = "SELECT\n" +
                    "    e1.EMP_ID,\n" +
                    "    e1.FIRST_NAME,\n" +
                    "    e1.LAST_NAME,\n" +
                    "    d.DEPARTMENT_NAME,\n" +
                    "    COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT\n" +
                    "FROM\n" +
                    "    EMPLOYEE e1\n" +
                    "JOIN\n" +
                    "    DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID\n" +
                    "LEFT JOIN\n" +
                    "    EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e1.DOB < e2.DOB\n" +
                    "GROUP BY\n" +
                    "    e1.EMP_ID,\n" +
                    "    e1.FIRST_NAME,\n" +
                    "    e1.LAST_NAME,\n" +
                    "    d.DEPARTMENT_NAME\n" +
                    "ORDER BY\n" +
                    "    e1.EMP_ID DESC;";


            System.out.println("3. Submitting Solution...");

            SolutionRequest solutionRequest = new SolutionRequest();
            solutionRequest.setFinalQuery(myFinalSqlQuery);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken); // Set the JWT

            HttpEntity<SolutionRequest> entity = new HttpEntity<>(solutionRequest, headers);

            restTemplate.exchange(
                    webhookUrl,    
                    HttpMethod.POST,
                    entity,
                    String.class   
            );

            System.out.println("   > Success! Solution submitted.");
            System.out.println("--- [TASK FINISHED] ---");

        } catch (Exception e) {
            System.err.println("!!! An error occurred during the process: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
