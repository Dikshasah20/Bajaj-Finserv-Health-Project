# Bajaj Finserv Health – Qualifier 1

Spring Boot app that on startup:
- Registers to generate a webhook (POST) with your name, regNo, email
- Receives a webhook URL and JWT access token
- Selects SQL problem link based on last two digits of regNo (odd → Q1, even → Q2)
- Submits your final SQL query to the webhook with JWT in Authorization header (raw token, no Bearer)

## Build & Run

Prereqs: Java 17, Maven 3.9+

1) Edit `src/main/resources/application.properties`:
```
app.name=Your Name
app.reg-no=REG12347
app.email=you@example.com
app.sql-solution=SELECT ...
```

2) Build and run
```
mvn spring-boot:run
```

## Implementation Details
- Registration POST:
  - URL: `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA`
  - Body: `{ "name": "...", "regNo": "...", "email": "..." }`
  - Response: `{ "webhook": "...", "accessToken": "..." }`
- Submission POST:
  - URL: `webhook` returned above (or `https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA` in the instructions)
  - Headers: `Authorization: <accessToken>`, `Content-Type: application/json`
  - Body: `{ "finalQuery": "YOUR_SQL_QUERY" }`
- No controllers; flow runs via `CommandLineRunner` on startup.
- Problem link selection is parity-based on last two digits in `regNo`.
