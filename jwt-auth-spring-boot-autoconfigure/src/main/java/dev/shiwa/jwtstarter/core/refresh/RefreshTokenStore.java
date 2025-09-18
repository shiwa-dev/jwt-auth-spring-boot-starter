package dev.shiwa.jwtstarter.core.refresh;

import java.time.Instant;

/**
 * Contract for managing refresh tokens in a persistent or in-memory store.
 * <p>
 * The store is responsible for saving, validating, and revoking refresh tokens
 * to support secure token rotation and reuse detection.
 */
public interface RefreshTokenStore {

    /**
     * Saves a new refresh token identifier (JTI) for a subject.
     *
     * @param jti       the unique token identifier (JWT ID)
     * @param subject   the subject (usually the username or user ID) associated
     *                  with the token
     * @param expiresAt the expiration timestamp of the refresh token
     */
    void save(String jti, String subject, Instant expiresAt);

    /**
     * Checks whether the given refresh token identifier (JTI) is still active (not
     * revoked and not expired).
     *
     * @param jti the token identifier to check
     * @return {@code true} if the token is active, {@code false} otherwise
     */
    boolean isActive(String jti);

    /**
     * Resolves the subject associated with a given refresh token identifier (JTI).
     *
     * @param jti the token identifier
     * @return the subject associated with this token, or {@code null} if not found
     */
    String subjectFor(String jti);

    /**
     * Revokes a specific refresh token, making it inactive.
     *
     * @param jti the token identifier to revoke
     */
    void revoke(String jti);

    /**
     * Revokes all refresh tokens belonging to a given subject (e.g., on reuse
     * detection or logout).
     *
     * @param subject the subject whose refresh tokens should be revoked
     */
    void revokeAllForSubject(String subject);
}