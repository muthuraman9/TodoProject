# AGENTS.md — Context for AI Coding Agents

This file describes the project context, conventions, and constraints for any AI coding agent working on this repository.

## Project

A small full-stack personal todo app. Spring Boot backend + React frontend + SQLite database. Runs locally only. Not intended for production.

## Task Requirements (from the exercise brief)

- Login and logout with two pre-created demo accounts
- No registration, password reset, or email verification
- Users can view, add, rename, mark complete/incomplete, and delete **their own** todos
- A todo only needs a `title` and `completed` status
- Users must **not** be able to view or modify another user's todos, **including via direct API calls**
- Persistence across restarts
- Clear error messages for invalid login, empty titles, failed requests
- Use an established library for authentication and password hashing
- **Never** store plaintext passwords
- Enforce authentication and ownership on the backend, not just by hiding frontend controls
- Two automated tests required: one successful operation, one cross-user denial

## Tech Stack

| Layer | Choice |
|-------|--------|
| Backend | Java 17, Spring Boot 4.x, Spring Security, Spring Data JPA |
| Database | SQLite (file-based, `todo.db`) |
| Frontend | React 18, Vite, axios |
| Build | Maven (backend), npm (frontend) |

## Conventions

### Backend

- **Layered architecture**: `Controller → Service → Repository`
  - Controllers handle HTTP only
  - Services hold business logic and transactions
  - Repositories handle data access
- **Ownership checks**  fetch the todo by ID, then verify todo.getOwner().getId().equals(currentUser.getId()). If it doesn't match, throw AccessDeniedException
- **Exceptions**: custom exceptions live in `exception/`, handled globally in `advice/GlobalExceptionHandler`
- **Return 404, not 403**, when a user requests a resource they don't own (avoids leaking existence)
- **Passwords**: BCrypt only, via Spring Security's `PasswordEncoder`
- **Entities are not exposed as DTOs** (acceptable for this small project; use DTOs in a bigger app)
- Package layout:
com.example.todoapp/
├── config/ # @Configuration classes (Security, DataSource, DataInitializer)
├── controller/ # REST controllers
├── service/ # business logic
├── repository/ # Spring Data JPA repos
├── model/ # JPA entities
├── exception/ # custom exceptions
└── advice/ # @RestControllerAdvice

### Frontend

- Plain JavaScript (no TypeScript)
- Vite for dev server and bundling
- `axios` with `withCredentials: true` for session-cookie auth
- Keep components small; no state management library needed
- Basic CSS is fine — visual polish is not a priority

### General

- Java package name: `com.example.todoapp`
- Don't add Docker, CI/CD, or deployment configs — not required
- Don't add features not in the task brief (registration, sharing, admin, etc.)
- Keep tests focused: exactly two integration tests required

## What to Avoid

- ❌ Storing passwords in plaintext
- ❌ Using `findById()` without an ownership check in user-facing endpoints
- ❌ Returning different status codes for "not found" vs "not yours" (leaks existence)
- ❌ Adding dependencies not needed for the task
- ❌ Over-engineering (Docker, Kubernetes, message queues, microservices)
- ❌ Touching production-like concerns (rate limiting, monitoring, etc.)

## Useful Commands

```bash
# Backend
cd todo-app
./mvnw spring-boot:run           # run app
./mvnw test                       # run tests
./mvnw clean package              # build jar

# Frontend
cd todo-frontend
npm install                       # install deps
npm run dev                       # dev server at :5173
npm run build                     # production build