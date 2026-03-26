## Mocara AI Medication Onboarding System

A full-stack healthcare onboarding system integrating an Android client with a Spring Boot backend.
The platform guides patients through structured medication protocols while supporting interactive chat assistance and escalation detection for safety-critical scenarios.

This project demonstrates full-stack system design, combining mobile application architecture, backend API design, database modeling, and domain-driven backend structure.
![App Demo](docs/screenshotss/onboarding_backend.gif)
## System Architecture
The system consists of two primary components:

- Android Client – Patient-facing mobile application
- Spring Boot Backend – API layer and business logic
- PostgreSQL Database – Persistent storage
```text
                   ┌──────────────────────────┐
                   │        Android App       │
                   │  Kotlin + JetpackCompose │
                   │                          │
                   │  UI Layer                │
                   │  ViewModel (StateFlow)  │
                   │  Repository              │
                   │  Retrofit API Client     │
                   └─────────────┬────────────┘
                                 │
                                 │ HTTPS REST API
                                 ▼
                ┌──────────────────────────────────┐
                │        Spring Boot Backend       │
                │                                  │
                │  Controller Layer (REST API)     │
                │                                  │
                │  ChatController                  │
                │  ProtocolController              │
                │  SessionController               │
                │  EscalationController            │
                │                                  │
                │  Service Layer                   │
                │                                  │
                │  ChatService                     │
                │  ProtocolService                 │
                │  SessionService                  │
                │                                  │
                │  Domain Modules                  │
                │  Chat / Protocol / Session       │
                │                                  │
                │  Persistence Layer               │
                │  Spring Data JPA Repositories    │
                └───────────────┬──────────────────┘
                                │
                                ▼
                       ┌────────────────┐
                       │   PostgreSQL   │
                       │                │
                       │  sessions      │
                       │  protocol      │
                       │  protocol_step │
                       │  chat_message  │
                       │  escalation    │
                       └────────────────┘

```

## Backend Architecture

The backend follows a domain-modular architecture, where each domain encapsulates its own entities, repositories, and services.
```text
backend
 ├── api
 │   ├── controller
 │   └── dto
 │
 ├── chat
 │   ├── entity
 │   ├── mapper
 │   ├── repo
 │   └── service
 │
 ├── protocol
 │   ├── entity
 │   ├── mapper
 │   ├── repo
 │   └── service
 │
 ├── session
 │   ├── entity
 │   ├── mapper
 │   ├── repo
 │   └── service
 │
 └── common
     ├── enums
     └── config

```
Benefits of this architecture:

- Clear separation of business domains
- Easier scalability for new features
- Improved maintainability of backend services
## Android Architecture

The Android application follows Clean Architecture with MVVM.
```text
UI (Jetpack Compose)
        │
        ▼
ViewModel
        │
        ▼
Domain Layer
        │
        ▼
Repository
        │
        ▼
Retrofit API Client

```
This design ensures separation of concerns and testable application logic.
## Database Design

The backend models a structured medication onboarding workflow.
### protocol

Defines a medication onboarding protocol.
```text
| Column      | Description               |
| ----------- | ------------------------- |
| id          | step identifier           |
| protocol_id | associated protocol       |
| step_order  | step sequence             |
| step_type   | INFO / QUESTION / CONFIRM |
| content     | step content              |

```

### patient_session

Represents a patient's onboarding session.
```text
| Column       | Description                    |
| ------------ | ------------------------------ |
| id           | session id                     |
| protocol_id  | associated protocol            |
| patient_id   | patient identifier             |
| current_step | current step index             |
| status       | ACTIVE / COMPLETED / ESCALATED |
| created_at   | session creation               |

```
### session_response

Stores patient responses for each protocol step.
```text
| Column     | Description        |
| ---------- | ------------------ |
| id         | response id        |
| session_id | associated session |
| step_id    | protocol step      |
| response   | patient response   |
| created_at | timestamp          |

```
### chat_message

Stores chat interaction messages.
```text
| Column     | Description           |
| ---------- | --------------------- |
| id         | message id            |
| session_id | session reference     |
| sender     | PATIENT / AI / SYSTEM |
| message    | chat content          |
| timestamp  | message time          |

```
### escalation

Represents detected safety escalation events.
```text
| Column     | Description                    |
| ---------- | ------------------------------ |
| id         | escalation id                  |
| session_id | session reference              |
| level      | LOW / MEDIUM / HIGH / CRITICAL |
| reason     | escalation reason              |
| created_at | timestamp                      |

```

## Key Features
### Medication Onboarding Workflow
Patients are guided through structured medication protocols step by step.
### Session Management
Patient sessions track progress across onboarding steps.
### Chat Interaction
Patients can interact with a chat interface during onboarding.
### Escalation Detection
Potential safety risks trigger escalation events for further review.
## Technology Stack
### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway database migration
### Mobile
- Kotlin
- Jetpack Compose
- MVVM Architecture
- Retrofit REST client
##  Running the Backend
### Clone the repository
```bash
git clone https://github.com/Mingyueoo/mocara-ai-onboarding-system.git
cd mocara-backend
```
### Configure database
Update `application.yaml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mocara
    username: postgres
    password: password
```
### Start the backend
```bash
mvn spring-boot:run
```
## Example API
### Create a new session
```bash
POST /api/v1/sessions
```
Request
```json
{
  "protocolId": 1,
  "patientId": "12345"
}
```
Response
```json
{
  "sessionId": 12,
  "status": "ACTIVE"
}
```
### Send chat message
```bash
POST /api/v1/chat
```
Request
```json
{
  "sessionId": 12,
  "message": "I feel dizzy after taking this medication"
}
```
## Future Improvements
Potential extensions include:
- AI-powered medical chat assistant
- Authentication with Spring Security + JWT
- Docker deployment
- API documentation with OpenAPI / Swagger
- Event-driven architecture for escalation alerts
## Engineering Highlights
- Designed a modular backend architecture separating Chat, Protocol, and Session domains.
- Implemented RESTful APIs supporting onboarding workflows, chat interactions, and escalation detection.
- Modeled healthcare workflows with relational database schema design.
- Integrated Android client with backend services via Retrofit REST APIs.
- Built persistence layer using Spring Data JPA with entity-DTO mapping.
- Implemented database migrations using Flyway for consistent schema management.