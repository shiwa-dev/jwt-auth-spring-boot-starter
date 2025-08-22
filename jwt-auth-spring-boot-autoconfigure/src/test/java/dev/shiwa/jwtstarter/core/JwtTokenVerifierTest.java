package dev.shiwa.jwtstarter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtAuthentication;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

class JwtTokenVerifierTest {

    private JwtAuthProperties mockProps;
    private JwtTokenVerifier verifier;
    private SecretKey key;
    private final String ISSUER = "trusted-issuer";

    @BeforeEach
    void setup() {
	String secret = "my-very-secure-jwt-secret-that-is-32-bytes";
	key = Keys.hmacShaKeyFor(secret.getBytes());

	mockProps = Mockito.mock(JwtAuthProperties.class);
	Mockito.when(mockProps.getSecret()).thenReturn(secret);
	Mockito.when(mockProps.getIssuer()).thenReturn(ISSUER);

	verifier = new JwtTokenVerifier(mockProps);
    }

    private String generateToken(String subject, List<String> roles, Instant issuedAt, Instant expiration,
	    String issuer) {
	return Jwts.builder().setSubject(subject).claim("roles", roles).setIssuedAt(Date.from(issuedAt))
		.setExpiration(Date.from(expiration)).setIssuer(issuer).signWith(key, SignatureAlgorithm.HS256)
		.compact();
    }

    @Test
    void constructor_shouldThrowExceptionForShortSecret() {
	JwtAuthProperties props = Mockito.mock(JwtAuthProperties.class);
	Mockito.when(props.getSecret()).thenReturn("too-short-secret");

	IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new JwtTokenVerifier(props));
	assertTrue(ex.getMessage().contains("at least 32 characters"));
    }

    @Test
    void parseToken_shouldReturnJwtAuthentication() {
	Instant now = Instant.now();
	String token = generateToken("user123", List.of("ROLE_USER"), now, now.plusSeconds(3600), ISSUER);

	JwtAuthentication auth = verifier.parseToken(token);

	assertEquals("user123", auth.getSubject());
	assertEquals(List.of("ROLE_USER"), auth.getRoles());
	assertTrue(auth.getExpiration().isAfter(now));
    }

    @Test
    void isValid_shouldReturnTrueForValidToken() {
	Instant now = Instant.now();
	String token = generateToken("user123", List.of("ROLE_USER"), now, now.plusSeconds(3600), ISSUER);

	assertTrue(verifier.isValid(token));
    }

    @Test
    void isValid_shouldReturnFalseForExpiredToken() {
	Instant now = Instant.now();
	String token = generateToken("user123", List.of("ROLE_USER"), now.minusSeconds(3600), now.minusSeconds(1800),
		ISSUER);

	assertFalse(verifier.isValid(token));
    }

    @Test
    void isValid_shouldReturnFalseForInvalidIssuer() {
	Instant now = Instant.now();
	String token = generateToken("user123", List.of("ROLE_USER"), now, now.plusSeconds(3600), "invalid-issuer");

	assertFalse(verifier.isValid(token));
    }

    @Test
    void isValid_shouldAcceptBearerPrefix() {
	Instant now = Instant.now();
	String rawToken = generateToken("user123", List.of("ROLE_USER"), now, now.plusSeconds(3600), ISSUER);
	String tokenWithBearer = "Bearer " + rawToken;

	assertTrue(verifier.isValid(tokenWithBearer));
    }

    @Test
    void isValid_shouldReturnFalseForNullToken() {
	assertFalse(verifier.isValid(null));
    }

    @Test
    void parseToken_shouldStripBearerPrefix() {
	Instant now = Instant.now();
	String rawToken = generateToken("subject", List.of("ADMIN"), now, now.plusSeconds(1000), ISSUER);
	String bearerToken = "Bearer " + rawToken;

	JwtAuthentication auth = verifier.parseToken(bearerToken);
	assertEquals("subject", auth.getSubject());
    }
}