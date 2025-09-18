package dev.shiwa.jwtstarter.core.refresh;

import java.util.List;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import dev.shiwa.jwtstarter.core.error.JwtAuthException;
import dev.shiwa.jwtstarter.core.error.JwtErrorCode;
import io.jsonwebtoken.Claims;

/**
 * Service for handling refresh token flows, including rotation, reuse
 * detection, and issuing new access/refresh tokens.
 */
public class RefreshTokenService {

    private final JwtTokenVerifier verifier;
    private final JwtTokenGenerator generator;
    private final RefreshTokenStore store;
    private final JwtAuthProperties props;

    /**
     * Creates a new {@link RefreshTokenService}.
     *
     * @param v the JWT verifier used to validate and parse tokens
     * @param g the JWT generator used to create new tokens
     * @param s the refresh token store used for persistence and revocation
     * @param p the JWT auth properties (configuration flags)
     */
    public RefreshTokenService(JwtTokenVerifier v, JwtTokenGenerator g, RefreshTokenStore s, JwtAuthProperties p) {
	this.verifier = v;
	this.generator = g;
	this.store = s;
	this.props = p;
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * <p>
     * This method performs the following:
     * <ul>
     * <li>Validates that refresh token flow is enabled</li>
     * <li>Parses and validates the refresh token</li>
     * <li>Checks token type</li>
     * <li>Performs reuse detection (if enabled)</li>
     * <li>Rotates refresh tokens (if enabled)</li>
     * <li>Generates a new access token and refresh token</li>
     * <li>Stores the new refresh token JTI</li>
     * </ul>
     *
     * @param refreshToken the refresh token provided by the client
     * @return a {@link Tokens} record containing the new access and refresh tokens
     * @throws JwtAuthException if refresh flow is disabled, token is
     *                          invalid/expired, token type is incorrect, or reuse
     *                          is detected
     */
    public Tokens refresh(String refreshToken) {
	if (!props.isRefreshEnabled())
	    throw new JwtAuthException(JwtErrorCode.REFRESH_DISABLED, "Refresh token flow is disabled");

	final Claims claims;
	try {
	    claims = verifier.parse(refreshToken);
	} catch (io.jsonwebtoken.ExpiredJwtException e) {
	    throw new JwtAuthException(JwtErrorCode.EXPIRED_TOKEN, "Refresh token expired");
	} catch (io.jsonwebtoken.JwtException e) {
	    throw new JwtAuthException(JwtErrorCode.INVALID_TOKEN, "Invalid refresh token: " + e.getMessage());
	}

	if (!"refresh".equals(claims.get("type", String.class)))
	    throw new JwtAuthException(JwtErrorCode.INVALID_TOKEN_TYPE, "Token type must be 'refresh'");

	String jti = claims.getId();
	String subject = claims.getSubject();

	// Reuse detection
	if (props.isReuseDetection() && !store.isActive(jti)) {
	    // kill all sessions of this subject
	    store.revokeAllForSubject(subject);
	    throw new JwtAuthException(JwtErrorCode.REFRESH_REUSE_DETECTED, "Refresh token reuse detected");
	}

	// Rotation: invalidate old RT, generate new one
	if (props.isRefreshRotate())
	    store.revoke(jti);

	String access = generator.generateAccessToken(subject, claims.get("roles", List.class));
	String refresh = generator.generateRefreshToken(subject);

	// save new jti
	Claims newRtClaims = verifier.parse(refresh);
	store.save(newRtClaims.getId(), subject, newRtClaims.getExpiration().toInstant());

	return new Tokens(access, refresh, System.currentTimeMillis() + props.getTtlMillis());
    }

    /**
     * Record containing both access and refresh tokens, along with the access
     * token's expiry timestamp in milliseconds.
     *
     * @param accessToken                the newly issued access token
     * @param refreshToken               the newly issued refresh token
     * @param accessTokenExpiresAtMillis expiration time (epoch millis) of the
     *                                   access token
     */
    public record Tokens(String accessToken, String refreshToken, long accessTokenExpiresAtMillis) {
    }
}