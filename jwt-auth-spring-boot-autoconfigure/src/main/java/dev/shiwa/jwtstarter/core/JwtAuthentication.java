package dev.shiwa.jwtstarter.core;

import java.time.Instant;
import java.util.List;

/**
 * Represents the authentication information extracted from a JWT token.
 *
 * <p>
 * This includes:
 * <ul>
 * <li>The subject (usually a user identifier)</li>
 * <li>A list of granted roles</li>
 * <li>The token's issue and expiration timestamps</li>
 * </ul>
 *
 * <p>
 * This class is typically used as the result of token parsing and verification,
 * e.g. {@code JwtTokenVerifier#parseToken(...)}.
 */
public class JwtAuthentication {

    private String subject;
    private List<String> roles;
    private Instant issuedAt;
    private Instant expiration;

    /**
     * Default constructor for deserialization frameworks (e.g., Jackson).
     */
    public JwtAuthentication() {
	// needed for Jackson
    }

    /**
     * Constructs a new JwtAuthentication instance with subject, roles, and
     * timestamps.
     *
     * @param subject    the token subject (typically the username or user ID)
     * @param roles      a list of roles associated with the subject
     * @param issuedAt   the timestamp at which the token was issued
     * @param expiration the expiration timestamp of the token
     */
    public JwtAuthentication(String subject, List<String> roles, Instant issuedAt, Instant expiration) {
	this.subject = subject;
	this.roles = roles;
	this.issuedAt = issuedAt;
	this.expiration = expiration;
    }

    /**
     * Returns the subject (user identifier) of the JWT token.
     *
     * @return the subject
     */
    public String getSubject() {
	return subject;
    }

    /**
     * Sets the subject of the JWT token.
     *
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
	this.subject = subject;
    }

    /**
     * Returns the list of roles granted to the subject.
     *
     * @return the list of roles
     */
    public List<String> getRoles() {
	return roles;
    }

    /**
     * Sets the list of roles for the subject.
     *
     * @param roles the list of roles to set
     */
    public void setRoles(List<String> roles) {
	this.roles = roles;
    }

    /**
     * Returns the timestamp when the token was issued.
     *
     * @return the issue time
     */
    public Instant getIssuedAt() {
	return issuedAt;
    }

    /**
     * Sets the timestamp when the token was issued.
     *
     * @param issuedAt the issue time to set
     */
    public void setIssuedAt(Instant issuedAt) {
	this.issuedAt = issuedAt;
    }

    /**
     * Returns the expiration time of the token.
     *
     * @return the expiration timestamp
     */
    public Instant getExpiration() {
	return expiration;
    }

    /**
     * Sets the expiration time of the token.
     *
     * @param expiration the expiration timestamp to set
     */
    public void setExpiration(Instant expiration) {
	this.expiration = expiration;
    }

    /**
     * Checks whether the token has expired compared to the current time.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
	return expiration != null && expiration.isBefore(Instant.now());
    }

    /**
     * Checks whether the subject has a specific role.
     *
     * @param role the role to check
     * @return true if the subject has the role, false otherwise
     */
    public boolean hasRole(String role) {
	return roles != null && roles.contains(role);
    }

    /**
     * Checks whether the subject has at least one of the specified roles.
     *
     * @param allowed array of allowed roles
     * @return true if any role matches, false otherwise
     */
    public boolean hasAnyRole(String... allowed) {
	if (roles == null)
	    return false;
	for (String a : allowed) {
	    if (roles.contains(a))
		return true;
	}
	return false;
    }
}