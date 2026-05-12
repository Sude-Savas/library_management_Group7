# Library Management System — Testing Assignment

A Spring Boot REST API for managing a library's book borrowing system.
This project is designed as a **software testing course assignment** covering:

- **Unit Testing** with JUnit 5 + Mockito
- **Integration Testing** with Spring Boot + TestContainers (PostgreSQL)
- **API (End-to-End) Testing** with TestRestTemplate + TestContainers
- **CI/CD** with GitHub Actions + Maven

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for TestContainers — integration & API tests)
- Git

## Getting Started

```bash
# Clone the repository
git clone <repo-url>
cd library-management

# Run the application locally (uses H2 in-memory database)
mvn spring-boot:run

# Run unit tests only (no Docker needed)
mvn test

# Run all tests including integration (Docker required)
mvn verify
```

The application will start at `http://localhost:8080`.
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:librarydb`)

---

## Project Architecture

```
┌──────────────────────────────────────────────────────┐
│                   REST Controllers                    │
│          (BookController, MemberController,           │
│                 BorrowController)                     │
├──────────────────────────────────────────────────────┤
│                    Services                           │
│     (BookService, MemberService, BorrowService)      │
│         ↑ Business rules enforced here ↑             │
├──────────────────────────────────────────────────────┤
│                  Repositories                         │
│  (BookRepository, MemberRepository,                  │
│              BorrowRecordRepository)                  │
├──────────────────────────────────────────────────────┤
│              PostgreSQL / H2 Database                 │
└──────────────────────────────────────────────────────┘
```

## API Endpoints

### Books
| Method | Endpoint                   | Description              |
|--------|----------------------------|--------------------------|
| GET    | /api/books                 | List all books           |
| GET    | /api/books/{id}            | Get book by ID           |
| GET    | /api/books/isbn/{isbn}     | Get book by ISBN         |
| POST   | /api/books                 | Create a new book        |
| PUT    | /api/books/{id}            | Update a book            |
| DELETE | /api/books/{id}            | Delete a book            |
| GET    | /api/books/search?keyword= | Search by title/author   |
| GET    | /api/books/genre/{genre}   | Filter by genre          |
| GET    | /api/books/available       | List available books     |

### Members
| Method | Endpoint              | Description              |
|--------|-----------------------|--------------------------|
| GET    | /api/members          | List all members         |
| GET    | /api/members/{id}     | Get member by ID         |
| POST   | /api/members          | Create a new member      |
| PUT    | /api/members/{id}     | Update a member          |
| DELETE | /api/members/{id}     | Deactivate a member      |
| GET    | /api/members/active   | List active members      |

### Borrowing
| Method | Endpoint                           | Description                |
|--------|------------------------------------|----------------------------|
| POST   | /api/borrows                       | Borrow a book              |
| POST   | /api/borrows/{id}/return           | Return a book              |
| GET    | /api/borrows/member/{memberId}     | Member's borrow history    |
| GET    | /api/borrows/member/{memberId}/active | Member's active borrows |
| GET    | /api/borrows/overdue               | List overdue records       |

## Business Rules

1. **Borrowing limits** depend on membership type:
   - STUDENT: max 2 books
   - STANDARD: max 3 books
   - PREMIUM: max 5 books
2. A member **cannot borrow the same book twice** simultaneously
3. **Inactive members** cannot borrow books
4. Books with **0 available copies** cannot be borrowed
5. **Late fees**: 1.50 TL per day after the 14-day due date
6. **ISBN must be unique** across all books

---

## Testing Assignment

### Your Task

The project includes example tests (filled in) and **TODO stubs** for you to implement.
There are three test levels, each in its own package:

### 1. Unit Tests (`src/test/java/.../unit/`)

**What:** Test individual classes in isolation, mocking all dependencies.

| File | What to test |
|------|-------------|
| `BorrowRecordTest.java` | `isOverdue()` logic, constructor defaults |
| `BorrowServiceTest.java` | Borrow limit, duplicate borrow, inactive member, copy count |

**Run:** `mvn test`

**Key concepts:**
- `@ExtendWith(MockitoExtension.class)` — enables Mockito
- `@Mock` — creates mock objects
- `@InjectMocks` — injects mocks into the service
- `when(...).thenReturn(...)` — stub behavior
- `verify(...)` — assert interactions
- `assertThrows(...)` — verify exceptions

### 2. Integration Tests (`src/test/java/.../integration/`)

**What:** Test repository queries against a **real PostgreSQL** database via TestContainers.

| File | What to test |
|------|-------------|
| `BookRepositoryIT.java` | Genre filtering, author search, unique ISBN constraint, deletion |

**Run:** `mvn verify` (requires Docker)

**Key concepts:**
- Extend `AbstractIntegrationTest` to get PostgreSQL container
- `@DataJpaTest` — loads only JPA components
- `@AutoConfigureTestDatabase(replace = NONE)` — use TestContainers DB
- `@DynamicPropertySource` — inject container connection details
- Tests run against **real SQL**, not mock data

### 3. API Tests (`src/test/java/.../api/`)

**What:** Start the **full application** and make **real HTTP requests** against it.

| File | What to test |
|------|-------------|
| `LibraryApiIT.java` | Borrow limit API error, no copies API error, member CRUD, search |

**Run:** `mvn verify` (requires Docker)

**Key concepts:**
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` — starts real server
- `TestRestTemplate` — makes actual HTTP calls
- Tests verify HTTP status codes, response bodies, and side effects
- Full stack: Controller → Service → Repository → Database

### Test Naming Convention

- `*Test.java` → Unit tests (run by **Surefire** plugin in `mvn test`)
- `*IT.java` → Integration/API tests (run by **Failsafe** plugin in `mvn verify`)

---

## CI/CD Pipeline (`.github/workflows/ci.yml`)

The GitHub Actions pipeline has 3 jobs:

1. **unit-tests** — Runs `mvn test` (fast, no Docker)
2. **integration-tests** — Runs `mvn verify` (uses Docker for TestContainers)
   - Only runs if unit tests pass
3. **build** — Packages the JAR (only if all tests pass)

### How to set it up
1. Push the project to a GitHub repository
2. The workflow triggers automatically on push/PR to `main` or `develop`
3. Check the Actions tab to see test results

---

## Grading Rubric (Suggested)

| Category | Points | Criteria |
|----------|--------|----------|
| Unit Tests — BorrowRecord | 15 | All `isOverdue()` and constructor tests pass |
| Unit Tests — BorrowService | 20 | All borrowBook/returnBook edge cases covered with mocks |
| Integration Tests — Repository | 20 | Genre, author, unique constraint, deletion tests pass |
| API Tests — Error cases | 25 | Borrow limit, no copies, 404s tested via HTTP |
| API Tests — Member & Search | 10 | Member CRUD and search endpoint tested |
| CI/CD | 10 | Pipeline runs green with all tests |

---

## Tips

- Start with unit tests — they're the fastest feedback loop
- For integration tests, always `deleteAll()` in `@BeforeEach` to isolate tests
- For API tests, use `Map.class` to deserialize responses when you just need to check fields
- Use `assertThat()` (AssertJ) for readable assertions
- Use `@DisplayName` for human-readable test names
- Check the example tests for patterns before writing your own

