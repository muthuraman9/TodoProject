# Personal Todo App

A small full-stack web application where users log in and manage their own private todos. Built as part of a full-stack exercise.

**Stack:** Spring Boot 4 + React (Vite) + SQLite + Spring Security

---

## Demo Accounts

| Username | Password |
|----------|----------|
| `muthu`  | `pass123` |
| `ravi`    | `pass123` |

Both users are created automatically on first startup. Log in as either one to see that they have separate, private todo lists.

---

## Prerequisites

- **Java 17** or newer (`java -version`)
- **Maven 3.8+** (bundled via the `mvnw` wrapper in the repo)
- **Node.js 18+** and **npm** (`node --version`, `npm --version`)

---

## Setup & Run

### 1. Backend (Spring Boot)

```bash
cd todo-app
./mvnw spring-boot:run
```

Or on Windows:

```cmd
cd todo-app
mvnw.cmd spring-boot:run
```

Backend runs at **http://localhost:8080**.

The SQLite database file `todo.db` is created automatically in the project root on first run. Delete it to reset all data.

### 2. Frontend (React)

Open a **second terminal**:

```bash
cd todo-frontend
npm install       # only needed the first time
npm run dev
```

Frontend runs at **http://localhost:5173**.

### 3. Open in browser

Go to **http://localhost:5173**, log in with one of the demo accounts.

---

## Run Tests

```bash
cd todo-app
./mvnw test
```

Two integration tests are included:

- `userCanCreateAndFetchOwnTodo` — verifies a successful todo creation and retrieval
- `userCannotDeleteAnotherUsersTodo` — verifies cross-user access is denied

---

## Design Choices

### 1. Backend enforces ownership, not just the frontend

Every todo endpoint looks up the todo by ID, then explicitly verifies ownership in the service layer. If the todo doesn't exist → 404 Not Found. If the todo exists but belongs to another user → 403 Forbidden. This distinction makes the API semantically correct and produces a stronger, more specific security signal in the automated test. The trade-off is that 403 leaks the existence of the todo ID to an attacker — acceptable here because the task's priority is a clearly testable cross-user denial.

This protection lives in the **service layer** (`TodoService`), so it's enforced regardless of which controller calls it.

### 2. Layered architecture

```
Controller → Service → Repository → Database
```

- **Controllers** only handle HTTP concerns: parsing requests, returning status codes
- **Services** hold business logic: ownership checks, input validation, transactions
- **Repositories** handle only data access

This keeps the ownership rule in **one place** rather than duplicated across endpoints.

### 3. BCrypt password hashing

Passwords are hashed using Spring Security's `BCryptPasswordEncoder`. Plaintext passwords never touch the database.

### 4. Session-based auth (not JWT)

For a locally-run app with two demo users, HTTP sessions are simpler and more appropriate than JWTs. Spring Security manages the session automatically; the React app sends the session cookie via `withCredentials: true`.

### 5. SQLite file persistence

Per the task, the database is SQLite. The file `todo.db` lives in the project root and is created by Hibernate on startup (`ddl-auto=update`). Data survives restarts.

### 6. `@JsonIgnore` on `Todo.owner`

`User` has a `List<Todo>` and `Todo` has a `User` — a bidirectional relationship. Without breaking the cycle, Jackson would recurse infinitely when serializing (caught by Jackson's 500-level safety limit and returned as a `HttpMessageNotWritableException`). We annotate `Todo.owner` with `@JsonIgnore` so the `owner` field is never serialized; the frontend doesn't need it anyway.

### 7. Package layout

- `config/` — Spring configuration (security, data source, seed data)
- `controller/` — REST endpoints
- `service/` — business logic
- `repository/` — Spring Data JPA interfaces
- `model/` — JPA entities
- `exception/` — custom exceptions
- `advice/` — global `@RestControllerAdvice` for exception → HTTP mapping

`DataInitializer` (which seeds the demo accounts) lives in `config/` because it's a `@Configuration` class. If the project grew to have multiple initializers, moving them to a dedicated `init/` package would be the next step.

### 8. Frontend

Plain React (no TypeScript) with Vite for fast dev-server reloads. Styling is minimal CSS — the task explicitly says visual polish is not important. Uses `axios` with `withCredentials: true` so session cookies flow between `localhost:5173` and `localhost:8080`.

---

## What I'd Do Differently in Production

- **Postgres instead of SQLite** — SQLite serializes writes and has no networking
- **JWT or OAuth2** for stateless, scalable auth
- **DTOs** instead of returning JPA entities directly (avoids accidental field leakage)
- **CSRF protection** re-enabled (currently disabled for REST simplicity)
- **HTTPS** and secure cookies
- **Rate limiting** on login to slow down brute-force attempts
- **Refresh tokens / session timeout** configuration
- **Frontend state management** (React Query or Redux) for cache invalidation
- **Proper icon library** (currently uses emoji for the password-eye toggle)
- **More comprehensive test suite**: unit tests for services, tests for edge cases (empty title, invalid JSON, etc.)

---

## Unfinished Work

- No user registration, password reset, or email verification (by design — task said not required)
- No "remember me" / long-lived sessions
- Frontend does not handle refresh-during-session gracefully across browser tabs (single tab works)
- Not deployed; runs locally only
- No Docker / CI — task said not required

---

## AI Agent Used

**Claude (Anthropic)** was used as the coding assistant throughout this project.

- **AGENTS.md** — the context file provided to the agent: [`AGENTS.md`](./AGENTS.md)
- **Full conversation** with the agent: [`CONVERSATION.md`](./CONVERSATION.md)
- **Example of code I reviewed and corrected**: see the "Code Review Example" section below

### Code Review Example

**Issue:** The AI's first version of `TodoController` called `todoRepository.findById(id)` directly and then checked `todo.getOwner().equals(currentUser)` in an `if` statement. This worked, but:

1. It fetched a todo owned by another user into memory (unnecessary and slightly risky)
2. The ownership check was duplicated in 3 endpoints

**My correction:** I refactored to push the ownership filter into the repository query itself:

```java
// AI's original (in controller):
Todo todo = todoRepository.findById(id).orElse(null);
if (todo == null || !todo.getOwner().getId().equals(currentUser.getId())) {
    return ResponseEntity.notFound().build();
}

// My correction (in TodoService):
Todo todo = todoRepository.findById(id)
    .orElseThrow(() -> new TodoNotFoundException(id));

if (!todo.getOwner().getId().equals(user.getId())) {
    throw new AccessDeniedException("You do not have permission to update this todo");
}
```

The custom `findByIdAndOwner` method pushes the ownership filter **into the SQL query**, so a foreign todo is never loaded, and the check lives in one place.

---

## License

None — this is a personal exercise.