package dev.shiwa.jwtstarter.core.refresh;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An in-memory implementation of {@link RefreshTokenStore}.
 * <p>
 * Stores refresh tokens in thread-safe maps, mapping token IDs (JTI) to
 * subjects and their expiration times.
 * </p>
 *
 * <h3>⚠ Limitations</h3>
 * <ul>
 * <li>This implementation is intended for <b>development, testing, or
 * single-node deployments</b>.</li>
 * <li>It is <b>not suitable for production in distributed or clustered
 * environments</b>, since tokens are only stored locally in memory.</li>
 * <li>For production, consider a shared store such as <b>Redis</b>, a database,
 * or another distributed cache.</li>
 * </ul>
 */
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final ConcurrentMap<String, String> jtiToSubject = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> jtiToExpiry = new ConcurrentHashMap<>();

    /**
     * Saves a refresh token identifier (JTI) along with its subject and expiry.
     *
     * @param jti     the unique token identifier
     * @param subject the subject (e.g., username or user ID) associated with the
     *                token
     * @param exp     the expiration timestamp of the refresh token
     */
    @Override
    public void save(String jti, String subject, Instant exp) {
	jtiToSubject.put(jti, subject);
	jtiToExpiry.put(jti, exp);
    }

    /**
     * Checks whether a refresh token is still active.
     * <p>
     * A token is considered active if:
     * <ul>
     * <li>It exists in the store</li>
     * <li>Its expiry time is in the future</li>
     * <li>A subject mapping still exists</li>
     * </ul>
     *
     * @param jti the token identifier
     * @return {@code true} if the token is active, {@code false} otherwise
     */
    @Override
    public boolean isActive(String jti) {
	Instant exp = jtiToExpiry.get(jti);
	return exp != null && Instant.now().isBefore(exp) && jtiToSubject.containsKey(jti);
    }

    /**
     * Retrieves the subject associated with a given token identifier (JTI).
     *
     * @param jti the token identifier
     * @return the subject for the token, or {@code null} if not found
     */
    @Override
    public String subjectFor(String jti) {
	return jtiToSubject.get(jti);
    }

    /**
     * Revokes a specific refresh token by removing it from the store.
     *
     * @param jti the token identifier to revoke
     */
    @Override
    public void revoke(String jti) {
	jtiToSubject.remove(jti);
	jtiToExpiry.remove(jti);
    }

    /**
     * Revokes all refresh tokens belonging to a specific subject.
     * <p>
     * Iterates through all stored JTIs and removes those associated with the given
     * subject.
     *
     * @param subject the subject whose refresh tokens should be revoked
     */
    @Override
    public void revokeAllForSubject(String subject) {
	jtiToSubject.entrySet().removeIf(e -> {
	    if (Objects.equals(e.getValue(), subject)) {
		jtiToExpiry.remove(e.getKey());
		return true;
	    }
	    return false;
	});
    }
}