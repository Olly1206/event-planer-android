Spring Boot Backend — Architecture & Implementation Plan
How Spring Boot Works (the basics)
Spring Boot is a Java framework that lets you build a REST API server. Your Android app will send HTTP requests (GET, POST, PUT, DELETE) to this server, and the server responds with JSON data. The server connects to your MySQL database to read/write data.

Spring Boot uses a layered architecture — each layer has one job and talks only to the layer next to it:

Android App
    ↕ HTTP (JSON)
┌─────────────────────────────┐
│  Controller Layer           │  ← Receives HTTP requests, sends responses
├─────────────────────────────┤
│  Service Layer              │  ← Business logic lives here
├─────────────────────────────┤
│  Repository Layer           │  ← Talks to the database
├─────────────────────────────┤
│  Entity Layer               │  ← Java classes that map to DB tables
└─────────────────────────────┘
    ↕ SQL
MySQL Database

Folder Structure (What we will create)

src/main/java/event_planer/project/
├── entity/
│   ├── User.java
│   ├── Event.java
│   ├── EventType.java
│   ├── EventOption.java
│   └── EventParticipant.java (join)
├── repository/
│   ├── UserRepository.java
│   ├── EventRepository.java
│   ├── EventTypeRepository.java
│   └── EventOptionRepository.java
├── dto/
│   ├── UserDTO.java
│   ├── CreateEventRequest.java
│   └── EventResponse.java
├── service/
│   ├── UserService.java
│   └── EventService.java
├── controller/
│   ├── UserController.java
│   └── EventController.java
└── security/
    ├── SecurityConfig.java
    └── JwtUtil.java

6-Step Implementation Plan
Step 1 — Entities (Java ↔ DB mapping)
These are Java classes annotated with @Entity. Each class maps to one DB table, and each field maps to a column. Spring/Hibernate reads these and knows how to write/read from MySQL automatically.

Create User.java, Event.java, EventType.java, EventOption.java
Add JPA annotations: @Entity, @Table, @Column, @Id, @ManyToOne, @ManyToMany
Step 2 — Repositories (database access)
These are interfaces that extend JpaRepository. You get findAll(), findById(), save(), delete() and more for free — Spring generates the SQL automatically. You can also add custom queries like findByUsername().

Create one repository per entity
No implementation needed — Spring handles it
Step 3 — DTOs (Data Transfer Objects)
DTOs are simple Java classes that define exactly what JSON data is sent/received over the API. They prevent exposing sensitive fields (e.g., password) directly and let you shape the API response separately from the DB model.

CreateEventRequest — what the Android app sends when creating an event
EventResponse — what the server sends back
UserDTO — user data without the password field
Step 4 — Services (business logic)
Service classes contain the actual application logic. They call repositories to get data, apply rules (e.g., "only the event owner can delete it"), and return results to the controller.

UserService — register, find user, update profile
EventService — create event, list events, add participant, add options
Step 5 — Controllers (REST API endpoints)
Controllers are the entry points for HTTP requests. Each method is mapped to a URL and HTTP method. They receive input, call the service, and return a response.

Example endpoints we'll create:

Method	URL	Action
POST	/api/users/register	Register new user
POST	/api/users/login	Login, get JWT token
GET	/api/events	List all events for logged-in user
POST	/api/events	Create new event
GET	/api/events/{id}	Get single event
PUT	/api/events/{id}	Update event
DELETE	/api/events/{id}	Delete event
POST	/api/events/{id}/participants	Add participant
GET	/api/event-types	List event types
GET	/api/event-options	List event options
Step 6 — Security (JWT Authentication)
Since the Android app needs to know who is logged in, we use JWT (JSON Web Token). When a user logs in, the server returns a token. The Android app sends this token with every subsequent request. Spring Security checks the token and grants/denies access automatically.

Add Spring Security + JWT dependency
SecurityConfig.java — which endpoints are public vs. protected
JwtUtil.java — generates and validates tokens
Suggested Build Order
Entities first (foundation everything else depends on)
Repositories (needed by Services)
DTOs (needed by Controllers)
Services (needed by Controllers)
Controllers (pull everything together)
Security last (added on top of working endpoints)
Ready to start? We'll begin with Step 1 — creating the Entity classes. I'll generate all the Java files directly into your Spring Boot project.