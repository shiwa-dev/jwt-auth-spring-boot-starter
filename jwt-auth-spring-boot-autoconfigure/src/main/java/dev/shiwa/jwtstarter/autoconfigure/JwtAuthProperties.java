package dev.shiwa.jwtstarter.autoconfigure;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT authentication headers and secret.
 *
 * <p>
 * This class defines authentication-related settings such as:
 * <ul>
 * <li>The HTTP header used to pass the token</li>
 * <li>The issuer (iss) claim</li>
 * <li>The secret key used for signing/verifying JWTs</li>
 * <li>The token time-to-live</li>
 * <li>Secured and excluded path patterns</li>
 * </ul>
 *
 * <p>
 * These properties are typically defined in {@code application.yml} or
 * {@code application.properties} as follows:
 *
 * <pre>
 * jwt:
 *   auth:
 *     issuer: my-app
 *     secret: my-secret-key
 *     ttlMillis: 60000
 *     protected-paths:
 *       - /api/*
 *     excluded-paths:
 *       - /public/*
 * </pre>
 */
@ConfigurationProperties(prefix = "jwt.auth")
public class JwtAuthProperties {

    /**
     * The issuer to be used in the 'iss' claim of JWT tokens. Can be used for
     * validating the token's origin.
     */
    private String issuer;

    /**
     * The secret key used for signing or verifying JWT tokens. Must be sufficiently
     * long for secure HMAC algorithms (e.g., HS256).
     */
    private String secret;

    /**
     * Token expiration time in milliseconds. Determines how long a generated JWT is
     * valid.
     */
    private long ttlMillis;

    private long refreshTtlMillis = 7L * 24 * 60 * 60 * 1000; // 7 days
    private boolean refreshEnabled = true;
    private boolean refreshRotate = true; // rotation on refresh
    private boolean reuseDetection = true; // detect reuse of old RTs

    /**
     * The HTTP header used to transmit the JWT token. Defaults to "Authorization".
     */
    private String header = "Authorization";

    /**
     * List of Ant-style path patterns that should be protected by JWT
     * authentication. Example: {@code /api/**}
     */
    private List<String> protectedPaths = List.of("/api/*");

    /**
     * List of Ant-style path patterns that should be excluded from JWT
     * authentication. These paths will bypass token validation.
     */
    private List<String> excludedPaths = List.of();

    public String getIssuer() {
	return issuer;
    }

    public void setIssuer(String issuer) {
	this.issuer = issuer;
    }

    /**
     * Returns the secret key for signing or validating tokens.
     *
     * @return the JWT secret
     */
    public String getSecret() {
	return secret;
    }

    /**
     * Sets the secret key for signing or validating tokens.
     *
     * @param secret the JWT secret
     */
    public void setSecret(String secret) {
	this.secret = secret;
    }

    /**
     * Returns the time-to-live for a JWT token in milliseconds.
     *
     * @return the TTL in ms
     */
    public long getTtlMillis() {
	return ttlMillis;
    }

    /**
     * Sets the time-to-live for a JWT token in milliseconds.
     *
     * @param ttlMillis the TTL in ms
     */
    public void setTtlMillis(long ttlMillis) {
	this.ttlMillis = ttlMillis;
    }

    /**
     * Returns the name of the HTTP header used to carry the JWT token.
     *
     * @return the HTTP header name
     */
    public String getHeader() {
	return header;
    }

    /**
     * Sets the HTTP header used to carry the JWT token.
     *
     * @param header the HTTP header name
     */
    public void setHeader(String header) {
	this.header = header;
    }

    /**
     * Returns the list of protected URL path patterns.
     *
     * @return protected paths requiring JWT authentication
     */
    public List<String> getProtectedPaths() {
	return protectedPaths;
    }

    /**
     * Sets the list of protected URL path patterns.
     *
     * @param protectedPaths paths that require token validation
     */
    public void setProtectedPaths(List<String> protectedPaths) {
	this.protectedPaths = protectedPaths;
    }

    /**
     * Returns the list of excluded URL path patterns.
     *
     * @return paths that bypass JWT validation
     */
    public List<String> getExcludedPaths() {
	return excludedPaths;
    }

    /**
     * Sets the list of excluded URL path patterns.
     *
     * @param excludedPaths paths to be excluded from token validation
     */
    public void setExcludedPaths(List<String> excludedPaths) {
	this.excludedPaths = excludedPaths;
    }

    public long getRefreshTtlMillis() {
	return refreshTtlMillis;
    }

    public void setRefreshTtlMillis(long refreshTtlMillis) {
	this.refreshTtlMillis = refreshTtlMillis;
    }

    public boolean isRefreshEnabled() {
	return refreshEnabled;
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
	this.refreshEnabled = refreshEnabled;
    }

    public boolean isRefreshRotate() {
	return refreshRotate;
    }

    public void setRefreshRotate(boolean refreshRotate) {
	this.refreshRotate = refreshRotate;
    }

    public boolean isReuseDetection() {
	return reuseDetection;
    }

    public void setReuseDetection(boolean reuseDetection) {
	this.reuseDetection = reuseDetection;
    }
}