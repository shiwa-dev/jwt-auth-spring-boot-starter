## 🔐 Spring Boot JWT Auth Starter
[![Maven Central](https://img.shields.io/maven-central/v/dev.shiwa.jwtstarter/jwt-auth-spring-boot-starter.svg)](https://search.maven.org/artifact/dev.shiwa.jwtstarter/jwt-auth-spring-boot-starter)
[![License](https://img.shields.io/github/license/shiwa-dev/jwt-auth-spring-boot-starter.svg)](https://github.com/shiwa-dev/jwt-auth-spring-boot-starter/blob/main/LICENSE)
[![Sponsor](https://img.shields.io/badge/sponsor-❤-brightgreen.svg)](https://github.com/sponsors/shiwa-dev)

A production-ready, plug-and-play **JWT authentication starter** for Spring Boot 3.x.  
This starter follows best practices for secure token handling, integrates easily into any Spring Boot application, and can save you **up to 40 hours** of development time.

## 📑 Table of Contents
- [Compatibility](#version-compatibility)
- [Features](#features-free-version)
- [Modules](#modules)
  - [Project Structure](#project-structure-multi-module-setup)
- [Build](#build-instructions)
- [Quick Start](#quick-start-maven)
  - [Maven](#quick-start-maven)
  - [Gradle](#quick-start-gradle)
- [Configuration](#configuration-with-application-yml)
  - [YAML](#configuration-with-application-yml)
  - [Properties](#configuration-with-application-properties)
- [Usage](#example-usage)
  - [Step-by-Step Guide](#step-by-step-requesting-a-token-calling-endpoints)
- [Demo](#demo)
  - [Swagger UI](#using-authorization-header-in-swagger-ui)
  - [Example](#example)
- [Comparison](#free-vs-pro)
- [Pro Version](#pro-version-coming-soon)
- [License](#license)
- [Disclaimer](#disclaimer)
- [Third-Party Licenses](#third-party-licenses)
- [Questions](#questions)

---

### 📌 Version Compatibility

* ✅ **Tested with**: Spring Boot **3.4.x – 3.5.x** (latest: 3.5.5)
* 🟢 **Generally compatible**: All Spring Boot **3.x** versions
* ⚠️ **Not compatible**: Spring Boot 2.x
* ℹ️ Spring Boot 3.3.x has reached **end-of-life (EOL)** and is therefore **not officially supported**.

---

### ✨ Features (Free Version)

- Plug-and-play JWT authentication setup  
- `@ConfigurationProperties` for easy config via `application.yml`  
- Compatible with Spring Security  
- Built-in token generator (`JwtTokenGenerator`)  
- Parses and maps JWT into a rich `JwtAuthentication` object  
- Optional `/me` endpoint for current user info  
- Auto-configuration and extensibility

## 📦 Modules

This project is organized into:

- **`jwt-auth-spring-boot-autoconfigure`** – Core JWT logic and configuration  
- **`jwt-auth-spring-boot-starter`** – Combines core logic into an easy-to-use starter  
- **`demo-app`** – A working example Spring Boot app demonstrating usage

### 🧱 Project Structure (Multi-Module Setup)

This repository follows a **multi-module Maven structure**, meaning the project is split into separate submodules that can be developed and built together or individually:

### 🔹 `jwt-auth-spring-boot-autoconfigure/`
Contains the core JWT logic, configuration properties, token parsing/validation, and Spring Security integration.

### 🔹 `jwt-auth-spring-boot-starter/`
Provides a lightweight dependency to include in external projects. This module packages the autoconfiguration and is prepared for **deployment to Maven Central**.

### 🔹 `demo-app/`
A full Spring Boot application showcasing the usage of the starter, including Swagger UI and protected endpoints.

### 🔧 Build Instructions

To build the entire project (all modules):

```bash
./mvnw clean install
```

To build just the starter module:

```bash
./mvnw -pl jwt-auth-spring-boot-starter clean install
```

> 📝 Only the `jwt-auth-spring-boot-starter` module is intended for publication on Maven Central. The demo and autoconfigure modules remain local dependencies.

### ⚙️ Quick Start (Maven)

```xml
<dependency>
  <groupId>dev.shiwa.jwtstarter</groupId>
  <artifactId>jwt-auth-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

### ⚙️ Quick Start (Gradle)

```groovy
implementation 'dev.shiwa.jwtstarter:jwt-auth-spring-boot-starter:0.1.0'
```

---

### Configuration with `application.yml`

```yaml
jwt:
  auth:
    # Identifier for the JWT issuer, used to validate the 'iss' claim in tokens
    issuer: jwt-starter-demo

    # Secret key used for signing and verifying JWT tokens (HS256 algorithm)
    # Must be at least 32 characters long (256 bits) for strong HMAC-based security
    secret: my-super-secret-key-1234567890!!

    # Token Time-To-Live (TTL) in milliseconds
    # Determines how long a generated token remains valid
    # 3600000 ms = 1 hour
    ttlMillis: 3600000

    # List of URL patterns that require JWT authentication
    # Supports Ant-style path patterns like /api/*, /api/**, /admin/**
    protected-paths:
      - /api/*


    # Refresh token settings
    # Time-to-live for refresh tokens in milliseconds (default: 7 days)
    refreshTtlMillis: 604800000

    # Enable/disable refresh token flow (default: true)
    refreshEnabled: true

    # If true, refresh tokens are rotated (old ones invalidated) on every use
    refreshRotate: true

    # If true, reuse of old refresh tokens is detected and all sessions for the subject are revoked
    reuseDetection: true

    # List of URL patterns to exclude from JWT authentication
    # Supports Ant-style patterns as above. Can be left empty if no exclusions are needed.
    excluded-paths:
```

---

### 🔧 Configuration with `application.properties`

```properties
jwt.auth.issuer=jwt-starter-demo
jwt.auth.secret=my-super-secret-key-1234567890!!
jwt.auth.ttlMillis=3600000
jwt.auth.protected-paths=/api/*
jwt.auth.excluded-paths=
jwt.auth.refreshTtlMillis=604800000
jwt.auth.refreshEnabled=true
jwt.auth.refreshRotate=true
jwt.auth.reuseDetection=true

```

### 🧪 Example Usage

```java
@RestController
public class MyController {

    @Autowired
    private JwtTokenVerifier verifier; // Utility for validating and parsing tokens
    
    @Autowired
    private JwtTokenGenerator tokenGenerator; // Utility for generating new tokens
    
    /**
     * Example login endpoint.
     * Normally, you would validate username/password here.
     * For demo purposes, a static JWT token is returned without credential checks.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
       // Here you would normally check user credentials (username/password)
       // For testing purposes, we just return a demo token without validation
       String token = tokenGenerator.generateToken("demo-user", List.of("USER", "ADMIN"));
       
       return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Example of a secured endpoint.
     * The request must include an Authorization header with a valid token.
     */
    @GetMapping("/api/secure")
    public boolean verify(@RequestHeader("Authorization") String token) {
        return verifier.isValid(token);
    }

    /**
     * Example endpoint that returns details about the current user (from the JWT).
     */
    @GetMapping("/api/me")
    public JwtAuthentication me(@RequestHeader("Authorization") String token) {
        return verifier.parseToken(token);
    }

    /**
     * Example refresh endpoint.
     * Accepts a refresh token and returns new access/refresh tokens.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        // Normally delegate to RefreshTokenService here
        String newAccess = tokenGenerator.generateToken("demo-user", List.of("USER", "ADMIN"));
        String newRefresh = tokenGenerator.generateRefreshToken("demo-user");
        return ResponseEntity.ok(Map.of(
            "accessToken", newAccess,
            "refreshToken", newRefresh,
            "accessTokenExpiresAtMillis", System.currentTimeMillis() + 3600000
        ));
    }

}
```

#### ✅ Step-by-Step: Requesting a Token & Calling Endpoints

1. **Start the app**
   Make sure your `application.yml` is configured (issuer/secret/TTL & path rules) and start the app, e.g.:

   ```bash
   ./mvnw spring-boot:run
   ```

2. **Request a token** (simple demo login without credential check):

   ```bash
   curl -X POST http://localhost:8080/login -H "Content-Type: application/json"
   ```

   **Response** (example):

   ```json
   { "token": "<JWT_HERE>" }
   ```

   Copy the value of `token` (without quotes).

   > Tip: This is a **Bearer Token**. Do not modify or shorten it.

3. **Call /api/me** – Show token details:

   ```bash
   curl -X GET http://localhost:8080/api/me \
     -H "Authorization: Bearer <JWT_HERE>"
   ```

   **Expected**: A JSON object with information contained in the token (e.g. subject/username, roles, issuer, expiration).

4. **Call /api/secure** – Validate token:

   ```bash
   curl -X GET http://localhost:8080/api/secure \
     -H "Authorization: Bearer <JWT_HERE>"
   ```

   **Expected**: `true` (if the token is valid).


6. **Call /refresh** – Use refresh token to obtain new tokens:

   ```bash
   curl -X POST http://localhost:8080/refresh \
     -H "Content-Type: application/json" \
     -d '{"refreshToken":"<REFRESH_TOKEN_HERE>"}'
   ```

   **Expected**: A JSON object containing a new access token, a new refresh token, and the new expiration timestamp.


5. **Swagger UI** (optional):
   Open `http://localhost:8080/swagger-ui/index.html`, click **Authorize**, enter `Bearer <JWT_HERE>` and confirm. Afterwards, you can call `/api/me` and `/api/secure` directly from the UI.

**Common Issues**

* Missing **`Bearer`** prefix in `Authorization` header → always send `Authorization: Bearer <JWT>`.
* Token expired (**TTL** reached) → request a new token via `/login`.
* Wrong `issuer` or `secret` → check your `application.yml` settings.
* Path not protected → check `protected-paths`/`excluded-paths` in `application.yml`.

## 📺 Demo

The included `demo-app` showcases how to use the JWT Auth Starter in a real Spring Boot project.
Once you start the demo app, open:

http://localhost:8080/swagger-ui/index.html

You will see the available endpoints:

- `POST /auth/login` – Returns a JWT token for a valid user  
- `GET /api/verify` – Verifies the JWT  
- `GET /api/me` – Returns parsed token details (`JwtAuthentication`)  
- `GET /api/has-role` – Role check for current token
- `POST /auth/refresh` – Exchanges a refresh token for a new access & refresh token pair  

### 🔄 Example Refresh Flow

1. Use `/auth/login` to get both `accessToken` and `refreshToken`:

```bash
curl -X POST http://localhost:8080/auth/login   -H "Content-Type: application/json"   -d '{"username":"admin","password":"admin"}'
```

Response:
```json
{
  "accessToken": "<ACCESS_TOKEN_HERE>",
  "refreshToken": "<REFRESH_TOKEN_HERE>",
  "accessTokenExpiresAtMillis": 1737200000000
}
```

2. When the access token expires, call `/auth/refresh` with the refresh token:

```bash
curl -X POST http://localhost:8080/auth/refresh   -H "Content-Type: application/json"   -d '{"refreshToken":"<REFRESH_TOKEN_HERE>"}'
```

Response:
```json
{
  "accessToken": "<NEW_ACCESS_TOKEN>",
  "refreshToken": "<NEW_REFRESH_TOKEN>",
  "accessTokenExpiresAtMillis": 1737203600000
}
```

3. Use the new `accessToken` for further API calls.
  

### 🔑 Using Authorization Header in Swagger UI

1. Click **"Authorize"** in the top-right corner of Swagger UI.
2. Enter your token in the format: Bearer <your-jwt-token>
3. Click **"Authorize"**, then close the popup.
4. All protected endpoints will now include the token automatically.

**Test the refresh flow in Swagger UI**

1. Expand **`POST /auth/login`**, click **Try it out**, send the request and copy the returned `refreshToken`.
2. Expand **`POST /auth/refresh`**, click **Try it out**, and paste this body:
   ```json
   { "refreshToken": "<PASTE_REFRESH_TOKEN_HERE>" }
   ```
3. Execute the request. You should receive a **new** `accessToken` and `refreshToken`.
4. Click **Authorize** again and paste `Bearer <NEW_ACCESS_TOKEN>` to call protected endpoints with the latest token.


### 🧪 Example

1. Use `/auth/login` to get a token:

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin"}'
```

2. Copy the token from the response and paste it in Swagger UI.
3. Call /api/me or /api/has-role to verify the current JWT.

---



---

## 🔐 Refresh Token Flow

The starter supports **refresh tokens** out of the box.  
This allows clients to obtain a new access token without forcing the user to log in again.

### Configuration

```yaml
jwt:
  auth:
    secret: your-256-bit-secret
    issuer: your-app-name
    ttlMillis: 3600000

    # Refresh token settings
    refresh-enabled: true
    refresh-rotate: true
    reuse-detection: true
```

### Endpoints

- `POST /auth/login`  
  Issues both **access** and **refresh tokens**.

  Example response:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresAtMillis": 1737200000000
  }
  ```

- `POST /auth/refresh`  
  Exchanges a refresh token for a new token pair.  

  Request:
  ```bash
  curl -X POST http://localhost:8080/auth/refresh     -H "Content-Type: application/json"     -d '{"refreshToken":"<your-refresh-token>"}'
  ```

  Response:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresAtMillis": 1737203600000
  }
  ```

### Behavior

- **Rotation**: if enabled, old refresh tokens are invalidated once used.  
- **Reuse detection**: if an invalidated token is reused, all tokens for that user are revoked.  
- **Error handling**: expired/invalid refresh tokens → `401 Unauthorized`.


## 🆚 Free vs. Pro

| Feature                          | Free  | Pro |
|----------------------------------|:-----:|:---:|
| JWT creation and validation      |  ✅   | ✅  |
| Spring Boot autoconfiguration    |  ✅   | ✅  |
| Multi-tenant support             |  ❌   | ✅  |
| Redis token blacklist            |  ❌   | ✅  |
| Refresh token handling           |  ✅   | ✅  |
| Admin dashboard                  |  ❌   | ✅  |
| License enforcement              |  ❌   | ✅  |
| Support                          | Community | Priority |

---

## 🚀 Pro Version (Coming Soon)

The Pro version will offer advanced features for production-ready security:

- Multi-tenant token support
- Token revocation (e.g. Redis)
- Refresh token flow
- Admin dashboard with token insights
- License verification and license key system
- Premium updates and support

➡️ Coming soon – stay tuned!
Join the waitlist or get notified at shiwa.dev

---

## 🧾 License

- The **Free version** of this project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- The **Pro version** is commercially licensed and may only be used with a valid license.
- Redistribution, resale, or modification of the Pro version is prohibited without written permission.

---

## ⚠️ Disclaimer

This software is provided **"as is"**, without warranty of any kind, express or implied.  
Use at your own risk. No liability is accepted for any damages resulting from its use.

---

## 📄 Third-Party Licenses

This project uses the following third-party library:

- [JJWT](https://github.com/jwtk/jjwt), licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

---

## 🙋 Questions?

Have questions about the Pro version or licensing?

📧 Contact us at [support@shiwa.dev](mailto:support@shiwa.dev)

