# Camunda 7 Demo - Real Estate Financing

## Overview

This repository contains a Spring Boot + Camunda 7 demo that showcases a real estate financing journey. The demo
includes three flows:

- **Credit comparison**: a user enters financing parameters, compares offers from multiple banks, selects one, submits
  an application, and signs the contract.
- **Auto-credit**: a streamlined flow that fetches the cheapest external offer and generates a ready-to-sign PDF
  contract.
- **DMN-credit**: a DMN-driven credit assessment flow that checks credibility and creates a contract PDF when approved.

## Features

- Camunda 7 BPMN processes for credit comparison, auto-credit, and DMN-credit.
- DMN-based credibility decisioning for the DMN-credit flow.
- Thymeleaf UI for entering parameters and interacting with user tasks.
- External offer lookup for the auto-credit flow.
- PDF generation for the auto-credit contract.

## Requirements

- Java 21+
- Maven 3.9+

## Running the application

```bash
mvn -U clean spring-boot:run
```

Once the application is running:

- Credit comparison UI: http://localhost:8080/credit
- Auto-credit UI: http://localhost:8080/autocredit
- DMN-credit UI: http://localhost:8080/dmncredit

## Process diagrams (BPMN)

The BPMN models are located in `src/main/resources/processes/`:

- **Credit application** (`src/main/resources/processes/credit/credit-application.bpmn`)
    - Starts when the user submits credit parameters.
    - Calls the credit comparison sub-process to fetch offers.
    - User selects a bank and submits the application.
    - A service task reviews the application and routes to approval/rejection.
    - On approval, the bank sends a contract and the user signs it.
    - Ends with either a concluded contract or a cancellation.

- **Credit comparison** (`src/main/resources/processes/credit/credit-comparison.bpmn`)
    - Fan-out parallel gateway requests conditions from Banks A, B, and C.
    - Results are joined and collected into a list of offers.
    - Ends after offers are aggregated.

- **Auto-credit** (`src/main/resources/processes/autocredit/autocredit.bpmn`)
    - User enters parameters.
    - Service task retrieves the cheapest external offer.
    - Service task creates a PDF contract.
    - Ends with a concluded contract.

- **DMN-credit** (`src/main/resources/processes/dmncredit/dmncredit.bpmn`)
    - User enters parameters.
    - Business rule task evaluates a DMN decision for credit credibility.
    - Exclusive gateway routes to approval or rejection.
    - On approval, a contract PDF is created and the process ends as concluded.
    - On rejection, the process ends without a contract.

## UI templates and assets

Thymeleaf templates and static assets live under `src/main/resources/`:

- Credit comparison UI: `src/main/resources/templates/credit.html`
- Auto-credit UI: `src/main/resources/templates/autocredit.html`
- DMN-credit UI: `src/main/resources/templates/dmncredit.html`
- Shared styling: `src/main/resources/static/css/credit.css`

## Configuration

See `src/main/resources/application.yaml` for configurable settings such as:

- Camunda REST/webapp credentials
- External auto-credit API endpoints
- PDF output directory

## Useful endpoints

- Camunda Webapps: http://localhost:8080/camunda/app/welcome/default/#!/login
    - Login: `camunda` / `admin`
- H2 console: http://localhost:8080/h2-console
    - JDBC URL: `jdbc:h2:mem:camunda`
