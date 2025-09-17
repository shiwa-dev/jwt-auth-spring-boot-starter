package dev.shiwa.jwtstarter.core.refresh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import dev.shiwa.jwtstarter.core.error.JwtAuthException;
import dev.shiwa.jwtstarter.core.error.JwtErrorCode;

class RefreshTokenServiceTest {

    private JwtAuthProperties props;
    private JwtTokenGenerator generator;
    private JwtTokenVerifier verifier;
    private RefreshTokenStore store;
    private RefreshTokenService service;

    private static final String SECRET = "this-is-a-very-long-256bit-secret-for-tests-1234567890";

    @BeforeEach
    void setUp() {
	props = new JwtAuthProperties();
	props.setSecret(SECRET);
	props.setIssuer("test-issuer");
	props.setTtlMillis(5 * 60 * 1000);
	props.setRefreshTtlMillis(7 * 24 * 60 * 60 * 1000L);
	props.setRefreshEnabled(true);
	props.setRefreshRotate(true);
	props.setReuseDetection(true);

	generator = new JwtTokenGenerator(props);
	verifier = new JwtTokenVerifier(props);
	store = new InMemoryRefreshTokenStore();
	service = new RefreshTokenService(verifier, generator, store, props);
    }

    private static String bearer(String raw) {
	return "Bearer " + raw;
    }

    /** Happy Path: valid refresh token rotates and issues new tokens */
    @Test
    void refresh_validToken_rotatesAndReturnsNewTokens() {
	String subject = "admin";
	List<String> roles = List.of("ADMIN", "USER");
	String refresh0 = generator.generateRefreshToken(subject);

	var rtClaims0 = verifier.parse(refresh0);
	store.save(rtClaims0.getId(), subject, rtClaims0.getExpiration().toInstant());

	var result = service.refresh(bearer(refresh0));

	assertNotNull(result.accessToken());
	assertNotNull(result.refreshToken());
	assertNotEquals(refresh0, result.refreshToken(), "Refresh token should be rotated");

	// Old jti must be inactive, new jti must be active
	assertFalse(store.isActive(rtClaims0.getId()));
	var rtClaims1 = verifier.parse(result.refreshToken());
	assertTrue(store.isActive(rtClaims1.getId()));
    }

    /** Reuse-Detection: old refresh token after rotation should throw */
    @Test
    void refresh_reuseOldToken_throwsReuseDetected() {
	String subject = "user1";
	String refresh0 = generator.generateRefreshToken(subject);

	var c0 = verifier.parse(refresh0);
	store.save(c0.getId(), subject, c0.getExpiration().toInstant());

	service.refresh(bearer(refresh0));

	JwtAuthException ex = assertThrows(JwtAuthException.class, () -> service.refresh(bearer(refresh0)));
	assertEquals(JwtErrorCode.REFRESH_REUSE_DETECTED, ex.getCode());
    }

    /** Wrong type: access token used at refresh endpoint should throw */
    @Test
    void refresh_withAccessToken_throwsInvalidTokenType() {
	String subject = "bob";
	String access = generator.generateAccessToken(subject, List.of("USER"));

	JwtAuthException ex = assertThrows(JwtAuthException.class, () -> service.refresh(bearer(access)));
	assertEquals(JwtErrorCode.INVALID_TOKEN_TYPE, ex.getCode());
    }

    /** Refresh disabled in properties */
    @Test
    void refresh_disabled_throwsRefreshDisabled() {
	props.setRefreshEnabled(false);

	String subject = "carol";
	String refresh = generator.generateRefreshToken(subject);
	var c = verifier.parse(refresh);
	store.save(c.getId(), subject, c.getExpiration().toInstant());

	JwtAuthException ex = assertThrows(JwtAuthException.class, () -> service.refresh(bearer(refresh)));
	assertEquals(JwtErrorCode.REFRESH_DISABLED, ex.getCode());
    }

    /** Expired refresh token should throw EXPIRED_TOKEN */
    @Test
    void refresh_expiredToken_throwsExpired() {
	props.setRefreshTtlMillis(-1000); // force expiry
	String subject = "dave";
	String expiredRefresh = generator.generateRefreshToken(subject);

	JwtAuthException ex = assertThrows(JwtAuthException.class, () -> service.refresh(bearer(expiredRefresh)));
	assertEquals(JwtErrorCode.EXPIRED_TOKEN, ex.getCode());
    }

    /** After rotation, only the new jti must remain active */
    @Test
    void rotation_deactivatesOldJti_onlyNewRemainsActive() {
	String subject = "erin";
	String refresh0 = generator.generateRefreshToken(subject);
	var c0 = verifier.parse(refresh0);
	store.save(c0.getId(), subject, c0.getExpiration().toInstant());

	var t1 = service.refresh(bearer(refresh0));
	var c1 = verifier.parse(t1.refreshToken());

	assertFalse(store.isActive(c0.getId()));
	assertTrue(store.isActive(c1.getId()));
    }
}