# Project Guidelines and Instructions: Event Manager Helper

This file contains general instructions and coding standards for Gemini to follow within the **Event Manager Helper** project.

## Project Overview
Event Manager Helper is designed to assist organizers in planning and finalizing events.

## Technology Stack & Architecture
- **Language**: Java.
- **Architecture**: Microservice Architecture following RESTful core principles and a MySQL databse.
- **API Standards**: Strict adherence to OpenAPI specifications and specific API contracts/schemas.
- **Key Integrations**: Weather API, Google Maps API.

## Coding Standards & AI Automation
- **AI-Driven Development**: Heavily rely on AI for:
    - Generating and automating unit and integration tests.
    - Verifying correct API integrations against schemas.
    - Implementing automated, robust HTTP error handling logic.
- **Error Handling**: Standardize HTTP error responses across all services.

## Architecture Preferences
- Decoupled microservices communicating via REST.
- Clear separation of concerns between service logic and API controllers.

## UI/UX Rules
- **Design Philosophy**: Modern and simple.
- **User Interface**: Avoid overwhelming the user; use progressive disclosure.
- **Navigation**: Utilize secondary menus or hamburger menus to keep the main interface clean.
- **Consistency**: Maintain a cohesive look across all event planning modules.
