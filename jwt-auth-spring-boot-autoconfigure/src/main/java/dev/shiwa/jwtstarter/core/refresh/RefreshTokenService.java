package dev.shiwa.jwtstarter.core.refresh;

import java.util.List;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import dev.shiwa.jwtstarter.core.error.JwtAuthException;
import dev.shiwa.jwtstarter.core.error.JwtErrorCode;
import io.jsonwebtoken.Claims;

public class RefreshTokenService {
    private final JwtTokenVerifier verifier;
    private final JwtTokenGenerator generator;
    private final RefreshTokenStore store;
    private final JwtAuthProperties props;

    public RefreshTokenService(JwtTokenVerifier v, JwtTokenGenerator g, RefreshTokenStore s, JwtAuthProperties p) {
	this.verifier = v;
	this.generator = g;
	this.store = s;
	this.props = p;
    }

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

    public record Tokens(String accessToken, String refreshToken, long accessTokenExpiresAtMillis) {
    }
}