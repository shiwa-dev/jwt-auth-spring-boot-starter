## 🔐 Spring Boot JWT Auth Starter

[![Maven Central](https://img.shields.io/maven-central/v/dev.shiwa.jwtstarter/jwt-auth-spring-boot-starter.svg)](https://search.maven.org/artifact/dev.shiwa.jwtstarter/jwt-auth-spring-boot-starter)
[![License](https://img.shields.io/github/license/shiwa-dev/jwt-auth-spring-boot-starter.svg)](https://github.com/shiwa-dev/jwt-auth-spring-boot-starter/blob/main/LICENSE)
[![Sponsor](https://img.shields.io/badge/sponsor-❤-brightgreen.svg)](https://github.com/sponsors/shiwa-dev)


A production-ready, plug-and-play **JWT authentication starter** for Spring Boot 3.x.  
This starter follows best practices for secure token handling, integrates easily into any Spring Boot application, and can save you **up to 40 hours** of development time.

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

## 🧱 Project Structure (Multi-Module Setup)

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

### ⚙️ Quick Start

```xml
<dependency>
  <groupId>dev.shiwa.jwtstarter</groupId>
  <artifactId>jwt-auth-spring-boot-starter</artifactId>
  <version>0.0.1</version>
</dependency>
```

```yaml
# application.yml
jwt:
  auth:
    secret: your-256-bit-secret
    issuer: your-app-name
    expirySeconds: 3600
    enabled: true
```

### 🧪 Example Usage

```java
@RestController
public class MyController {

    @Autowired
    private JwtTokenVerifier verifier;

    @GetMapping("/api/secure")
    public boolean verify(@RequestHeader("Authorization") String token) {
        return verifier.isValid(token);
    }

    @GetMapping("/api/me")
    public JwtAuthentication me(@RequestHeader("Authorization") String token) {
        return verifier.parseToken(token);
    }
}
```

### 🔐 Token Generation

```java
@Autowired
JwtTokenGenerator generator;

String token = generator.generateToken("alice", List.of("USER", "ADMIN"));
```

## 📺 Demo

The included `demo-app` showcases how to use the JWT Auth Starter in a real Spring Boot project.
Once you start the demo app, open:

http://localhost:8080/swagger-ui/index.html

You will see the available endpoints:

- `POST /auth/login` – Returns a JWT token for a valid user  
- `GET /api/verify` – Verifies the JWT  
- `GET /api/me` – Returns parsed token details (`JwtAuthentication`)  
- `GET /api/has-role` – Role check for current token  

### 🔑 Using Authorization Header in Swagger UI

1. Click **"Authorize"** in the top-right corner of Swagger UI.
2. Enter your token in the format: Bearer <your-jwt-token>
3. Click **"Authorize"**, then close the popup.
4. All protected endpoints will now include the token automatically.

### 🧪 Example

1. Use `/auth/login` to get a token:

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin"}'
```

2. Copy the token from the response and paste it in Swagger UI.
3. Call /api/me or /api/has-role to verify the current JWT.

---

## 🆚 Free vs. Pro

| Feature                          | Free  | Pro |
|----------------------------------|:-----:|:---:|
| JWT creation and validation      |  ✅   | ✅  |
| Spring Boot autoconfiguration    |  ✅   | ✅  |
| Multi-tenant support             |  ❌   | ✅  |
| Redis token blacklist            |  ❌   | ✅  |
| Refresh token handling           |  ❌   | ✅  |
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
