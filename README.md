# Jobly — Spring Boot Job Posting Platform

> A clean, role-based job posting website built with Spring Boot, Thymeleaf, MySQL and Spring Security. Users can sign in as Candidates, Hiring Managers, Companies or Super Admins; Companies/Hiring Managers can post jobs; Candidates can view and apply; Super Admin manages the whole platform.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Key Features](#key-features)
3. [Architecture & Project Structure](#architecture--project-structure)
4. [Tech Stack & Dependencies](#tech-stack--dependencies)
5. [Data Model (high-level)](#data-model-high-level)
6. [Authentication & Authorization](#authentication--authorization)
7. [Security & Best Practices](#security--best-practices)
8. [Exception Handling, Validation & Logging](#exception-handling-validation--logging)
9. [Frontend (Thymeleaf) notes](#frontend-thymeleaf-notes)
10. [Configuration & Running Locally](#configuration--running-locally)
11. [Swagger / API Documentation](#swagger--api-documentation)
12. [Testing](#testing)
13. [Deployment](#deployment)
14. [Coding Standards & Best Practices](#coding-standards--best-practices)
15. [Contributing](#contributing)
16. [Roadmap / Next steps](#roadmap--next-steps)

---

## Project Overview

Jobly is a role-based job posting platform where:

* **Candidates** can browse and apply to jobs.
* **Hiring Managers / Companies** can create/manage job posts and view applicants.
* **Super Admin** oversees the platform and has top-level privileges.

The project is built using Spring Boot (back end), Thymeleaf (server-side templates), and MySQL (persistent storage). Spring Data JPA will be used to auto-generate database schemas on the first run.

## Key Features

* Role-based login & access control (Candidate, Company, Hiring Manager, Super Admin)
* Sign-in page that collects basic details and, depending on the role, asks for extended information
* CRUD for Job Posts (create, read, update, delete)
* Candidate job application flow
* Admin dashboard for managing users, companies and jobs
* MySQL persistence with JPA auto schema generation on first run
* Validation, centralised exception handling, and consistent logging
* Password encryption (BCrypt) and secure session handling
* Swagger/OpenAPI documentation for APIs
* Thymeleaf-based frontend — separate HTML and CSS files per page

## Architecture & Project Structure

The Java code should follow a layered architecture. Example packaging:

```
com.jobly
├─ JoblyApplication.java            # main entry point
├─ config                          # security, swagger, datasource configs
├─ controller                      # REST / MVC controllers (Thymeleaf views)
├─ dto                             # DTOs for requests/responses
├─ exception                       # custom exceptions & handlers
├─ model                           # JPA entities (User, Role, Company, Job, Application, etc.)
├─ repository                      # Spring Data JPA repositories
├─ service                         # business logic (interfaces + impls)
├─ util                            # helpers, mappers, validators
└─ web                             # static resources and thymeleaf templates
```

Layer responsibilities:

* **Model layer**: Entities, JPA mappings, validation annotations
* **Repository layer**: Spring Data interfaces (extend `JpaRepository`)
* **Service layer**: Transactional business logic; DTO mapping; exceptions
* **Controller layer**: Handles web requests; returns Thymeleaf views or JSON
* **Exception layer**: Custom exceptions + `@ControllerAdvice` for global handling

## Tech Stack & Dependencies

Minimum dependencies to include (use Maven or Gradle):

* Java 17 or 21 (LTS recommended)
* Spring Boot (current stable 3.x)
* Spring Web
* Spring Security
* Spring Data JPA (Hibernate)
* MySQL Connector/J
* Thymeleaf
* Spring Boot DevTools
* Spring Validation (`spring-boot-starter-validation`)
* Springdoc OpenAPI / Swagger UI (recommended: springdoc-openapi-ui)
* Lombok (optional but useful for reducing boilerplate)
* Logback / SLF4J (logging)
* Flyway or Liquibase (optional — recommended for production DB migrations)

Example Maven dependencies snippet (pom.xml):

```xml
<!-- simplified excerpt -->
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  <dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <!-- optional -->
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
  </dependency>
</dependencies>
```

## Data Model (high-level)

Entities to include (recommended):

* `User` (id, name, email, phone, password, roles, enabled, createdAt)
* `Role` (ROLE_CANDIDATE, ROLE_COMPANY, ROLE_HIRING_MANAGER, ROLE_SUPER_ADMIN)
* `Company` (id, name, description, website, address, ownerUser)
* `Job` (id, title, description, location, employmentType, salaryRange, company, postedBy, createdAt, status)
* `Application` (id, job, candidate (User), coverLetter, resumeUrl, appliedAt, status)
* `Audit` fields (createdBy, createdAt, modifiedBy, modifiedAt) — use `@MappedSuperclass` + JPA auditing

Keep entity relationships clear (many-to-one Job→Company; one-to-many Company→Job; many-to-many User↔Role; one-to-many Job→Application).

**Auto-generation of schemas**: for development you can enable auto DDL by setting `spring.jpa.hibernate.ddl-auto=update` (or `create-drop` for dev). For production, rely on Flyway/Liquibase migrations instead.

## Authentication & Authorization

Two recommended approaches depending on needs:

1. **Server-side rendered Thymeleaf application** (recommended if using Thymeleaf pages)

   * Use Spring Security with form-login and session management.
   * Store passwords using BCrypt (`BCryptPasswordEncoder`).
   * Roles map to authorities (e.g. `ROLE_CANDIDATE`, `ROLE_COMPANY`).
   * Use method-level security (`@PreAuthorize("hasRole('ROLE_COMPANY')")`) on service/controller methods.

2. **REST API + SPA / API-first**

   * Use JWT-based stateless authentication for APIs and separate frontend.
   * Keep refresh tokens, short-lived access tokens, and implement token blacklisting for logout.

### Example (session-based) Security config outline

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf().and()
      .authorizeHttpRequests()
         .requestMatchers("/auth/**", "/css/**", "/js/**", "/swagger-ui/**").permitAll()
         .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
         .anyRequest().authenticated()
      .and()
        .formLogin().loginPage("/auth/login").defaultSuccessUrl("/")
      .and()
        .logout().logoutUrl("/auth/logout").logoutSuccessUrl("/auth/login?logout")
    ;
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
```

## Security & Best Practices

* **Password storage**: Always use a strong hashing function such as BCrypt with a secure work factor.
* **Transport security**: Use HTTPS in production and HSTS header.
* **CSRF**: Enable CSRF protection for stateful forms (Thymeleaf). For APIs, require CSRF tokens or use stateless tokens properly.
* **Input validation & output escaping**: Use `@Valid` and server-side validation. Escape user content in Thymeleaf (`th:text`) to avoid XSS.
* **Principle of least privilege**: Roles should have the minimum permissions necessary.
* **Avoid storing sensitive data in logs** (passwords, tokens).
* **Use prepared statements** (JPA does this). Validate and sanitize any dynamic SQL.
* **Rate limiting**: Add rate limiting on critical endpoints (login, sign-up) to prevent brute force.
* **Account lockout**: Consider lockouts or CAPTCHA after repeated failed logins.
* **Secrets management**: Do not hardcode DB credentials; use environment variables or secrets manager.

## Exception Handling, Validation & Logging

* Use a global exception handler with `@ControllerAdvice` to format error responses and return proper HTTP codes.
* Create custom exceptions like `ResourceNotFoundException`, `AccessDeniedException`, `DuplicateResourceException`.
* Use `@Valid` and validation annotations for DTOs; handle `MethodArgumentNotValidException` in the controller advice.
* Use SLF4J + Logback for structured logging; log important events (user sign-ups, job postings) at INFO; errors at ERROR with stack trace.
* For traceability, include request id correlation (UUID) to track logs across requests — use a servlet filter to populate a MDC key.

## Frontend (Thymeleaf) notes

* Keep a layout template (header, footer, nav). Use Thymeleaf layout dialect or fragments to reuse components.
* Store HTML templates under `src/main/resources/templates` and static assets under `src/main/resources/static` (`/css`, `/js`, `/images`).
* Use form backing objects (DTOs) for binding; validate with `@Valid` and show errors in the template.
* Separate pages:

  * `/auth/login.html`
  * `/auth/register.html`
  * `/jobs/list.html`
  * `/jobs/detail.html`
  * `/jobs/create.html` (for hiring managers/companies)
  * `/dashboard/admin.html`
  * `/profile/edit.html`

## Configuration & Running Locally

### application.properties (dev example)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobly?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=jobly_user
spring.datasource.password=CHANGEME
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Show SQL in log (dev only)
spring.jpa.show-sql=true

# Thymeleaf
spring.thymeleaf.cache=false

# Server port (optional)
server.port=8080
```

> **Note:** `spring.jpa.hibernate.ddl-auto=update` helps auto-generate schemas during the initial run for dev. In production switch to `validate` and use Flyway/Liquibase migrations.

### Running

1. Create MySQL database and user or configure env variables.
2. Build: `mvn clean package` or `./gradlew bootJar`.
3. Run: `mvn spring-boot:run` or run the generated jar: `java -jar target/jobly-0.0.1-SNAPSHOT.jar`.

## Swagger / API Documentation

Use Springdoc (OpenAPI) for auto-generating API docs. Example dependency included earlier. By default the UI will be available at `/swagger-ui.html` or `/swagger-ui/index.html`.

Provide `@Operation` and `@Schema` annotations on controllers and DTOs for richer docs.

## Testing

* Unit tests: JUnit 5 + Mockito for service and repository-level logic.
* Integration tests: `@SpringBootTest` with an embedded DB (H2) and test profiles. Alternatively use Testcontainers for running ephemeral MySQL instances.
* Controller tests: `@WebMvcTest` for MVC controllers.

## Deployment

* Use externalized configuration (env vars) for DB credentials and secrets.
* Build a Docker image (multi-stage) with a small JRE runtime. Example `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

* For production DB migrations use Flyway; run migrations during CI/CD.
* Use a reverse proxy (NGINX) and secure with TLS.

## Coding Standards & Best Practices

* **Naming conventions**: Use `camelCase` for variables and methods, `PascalCase` for types and classes, `UPPER_SNAKE_CASE` for constants.
* **Packages**: Group by feature or layer consistently (layered approach shown above).
* **Formatting**: Use an opinionated code formatter (Eclipse/IntelliJ formatter or Google Java Format) and enforce with CI pre-commit checks.
* **Document code**: JavaDoc public service methods and complex logic.
* **Small methods**: Keep functions focused — single responsibility.
* **DTOs**: Avoid exposing JPA entities directly to controllers; map entities to DTOs for API/Views.
* **Transactions**: Annotate service methods that change state with `@Transactional`.
* **Avoid business logic in controllers** — controllers should be thin.
* **Logging**: Use parameterized logging (`log.info("User {} logged in", userId)`), avoid string concatenation in logs.
* **Security reviews**: Review endpoints for excessive privileges.

## Sample Endpoints (MVC + REST hybrid)

```
GET  /                -> home (job list)
GET  /jobs            -> list jobs
GET  /jobs/{id}       -> job detail
GET  /jobs/new        -> form to create job (company / hiring manager)
POST /jobs            -> create job
POST /jobs/{id}/apply -> candidate applies to job
GET  /admin/dashboard -> admin panel (super admin only)
POST /auth/register   -> register user
POST /auth/login      -> login (handled by Spring Security form)
```

## Example: User entity (simplified)

```java
@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // store hashed (BCrypt)

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles;
}
```

## Recommended Development Workflow

1. Use feature branches (`feature/xxx`) and PRs.
2. Add unit tests for business logic before adding major features.
3. Run static analysis tools — SpotBugs, PMD, Checkstyle (optional).
4. Use CI to run tests and build artifacts.
5. Use database migration files (Flyway) and review migration history.

## Roadmap / Next Steps

* Implement full RBAC with method-level security
* Implement resume upload & file storage (S3-compatible or local storage for dev)
* Add search & filter for jobs (Elasticsearch or DB full-text search)
* Add email notifications for new applications (SMTP or transactional email)
* Implement pagination & sorting
* Add analytics for admin dashboard

## Contributing

Contributions are welcome! Please follow the style guidelines, open an issue first for large changes, and create PRs against `develop` branch. Include tests for new logic.

---

### Final notes & corrections made (based on your brief)

* Because you requested Thymeleaf HTML pages, a session-based Spring Security configuration is recommended (rather than JWT) — this simplifies server-side rendering, CSRF handling and form authentication.
* For automatic table generation at startup, we set `spring.jpa.hibernate.ddl-auto=update` in development. For production, use migration tooling (Flyway/Liquibase) and `ddl-auto=validate`.
* `Authentication` spelled as `Authentication`; ensured correct naming in the docs.
* Clarified distinction between Company and Hiring Manager roles (both can post jobs; company represents an organisation, hiring manager is a user tied to a company).

---

If you want, I can also:

* produce skeleton Maven/Gradle `pom.xml`/`build.gradle` with exact dependencies,
* generate sample entity/controller/service/repository code for one feature (e.g., Jobs CRUD + apply flow), or
* create a minimal Thymeleaf template set for the main pages.

---

*README generated by Jobly README generator.*
