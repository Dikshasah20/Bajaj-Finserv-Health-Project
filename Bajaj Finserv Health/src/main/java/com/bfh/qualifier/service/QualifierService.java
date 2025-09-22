package com.bfh.qualifier.service;

import com.bfh.qualifier.config.AppProperties;
import com.bfh.qualifier.dto.RegistrationRequest;
import com.bfh.qualifier.dto.RegistrationResponse;
import com.bfh.qualifier.dto.SqlSubmissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class QualifierService {
    private static final Logger log = LoggerFactory.getLogger(QualifierService.class);

    private final RestTemplate restTemplate;
    private final AppProperties props;

    public QualifierService(RestTemplate restTemplate, AppProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    public void runFlow() {
        validateConfig();

        RegistrationResponse registration = registerAndGetWebhook();
        if (registration == null || registration.getWebhook() == null || registration.getAccessToken() == null) {
            log.error("Invalid registration response; stopping.");
            return;
        }

        String problemLink = pickProblemLink(props.getRegNo());
        log.info("Assigned SQL problem link: {}", problemLink);

        submitSql(registration.getWebhook(), registration.getAccessToken());
    }

    private void validateConfig() {
        if (isBlank(props.getName()) || isBlank(props.getRegNo()) || isBlank(props.getEmail()) || isBlank(props.getSqlSolution())) {
            throw new IllegalStateException("Please configure app.name, app.reg-no, app.email, app.sql-solution in application.properties");
        }
    }

    private RegistrationResponse registerAndGetWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        RegistrationRequest request = new RegistrationRequest(props.getName(), props.getRegNo(), props.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegistrationRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<RegistrationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, RegistrationResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            log.error("Registration failed: status={} body={}", response.getStatusCode(), response.getBody());
        } catch (RestClientException ex) {
            log.error("Registration call failed", ex);
        }
        return null;
    }

    private void submitSql(String webhookUrl, String jwt) {
        SqlSubmissionRequest payload = new SqlSubmissionRequest(props.getSqlSolution());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Assessment requires raw JWT token in Authorization header (no Bearer prefix)
        headers.add(HttpHeaders.AUTHORIZATION, jwt);
        HttpEntity<SqlSubmissionRequest> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SQL submission successful. Response: {}", response.getBody());
            } else {
                log.error("SQL submission failed: status={} body={}", response.getStatusCode(), response.getBody());
            }
        } catch (RestClientException ex) {
            log.error("SQL submission call failed", ex);
        }
    }

    private String pickProblemLink(String regNo) {
        String lastTwo = extractLastTwoDigits(regNo);
        if (lastTwo == null) {
            log.warn("Could not parse last two digits from regNo={}, defaulting to Question 1", regNo);
            return "https://drive.google.com/file/d/1IeSI6l6KoSQAFfRihIT9tEDICtoz-G/view?usp=sharing";
        }
        int value = Integer.parseInt(lastTwo);
        boolean isOdd = value % 2 != 0;
        return isOdd ?
            "https://drive.google.com/file/d/1IeSI6l6KoSQAFfRihIT9tEDICtoz-G/view?usp=sharing" :
            "https://drive.google.com/file/d/143MR5cLFrlNEuHzzWJ5RHnEWuijuM9X/view?usp=sharing";
    }

    private String extractLastTwoDigits(String regNo) {
        if (regNo == null) return null;
        String digits = regNo.replaceAll("[^0-9]", "");
        if (digits.length() < 2) return null;
        return digits.substring(digits.length() - 2);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

