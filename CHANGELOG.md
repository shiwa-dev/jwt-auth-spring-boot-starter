# 📄 Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/),  
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [0.2.0] – 2025-09-19

### ✨ Added
- Refresh token support (access & refresh token pair issued at login)
- `/auth/refresh` endpoint to obtain new tokens
- Configurable refresh options:
  - `jwt.auth.refreshTtlMillis` (default: 7 days)
  - `jwt.auth.refreshEnabled` (default: true)
  - `jwt.auth.refreshRotate` (default: true)
  - `jwt.auth.reuseDetection` (default: true)
- Demo app updated with refresh flow examples in Swagger UI and README

### 💥 Breaking Changes
- Property `ttlMillis` has been renamed to `accessTtlMillis`
- Renamed `JwtLoginController` to `JwtAuthController` in demo module

---

## [0.1.0] – 2025-08-27

### ✨ Added
- Initial release of the JWT Auth Starter for Spring Boot 3+
- Supports JWT generation, validation, and parsing
- Plug-and-play auto-configuration via `application.yml`
- Optional `/me` endpoint
- Built-in Swagger UI demo (`demo-app`)
- Public Maven dependency: `dev.shiwa.jwtstarter:jwt-auth-spring-boot-starter`

---
