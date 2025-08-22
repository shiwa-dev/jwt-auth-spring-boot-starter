package dev.shiwa.jwtstarter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.shiwa.jwtstarter.core.JwtAuthentication;

class JwtAuthenticationTest {

    @Test
    void constructor_shouldSetAllFieldsCorrectly() {
	String subject = "john.doe";
	List<String> roles = List.of("USER", "ADMIN");
	Instant issuedAt = Instant.now();
	Instant expiration = issuedAt.plusSeconds(3600);

	JwtAuthentication auth = new JwtAuthentication(subject, roles, issuedAt, expiration);

	assertEquals(subject, auth.getSubject());
	assertEquals(roles, auth.getRoles());
	assertEquals(issuedAt, auth.getIssuedAt());
	assertEquals(expiration, auth.getExpiration());
    }

    @Test
    void isExpired_shouldReturnTrue_whenExpirationIsInPast() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setExpiration(Instant.now().minusSeconds(60));

	assertTrue(auth.isExpired(), "Token should be expired");
    }

    @Test
    void isExpired_shouldReturnFalse_whenExpirationIsInFuture() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setExpiration(Instant.now().plusSeconds(60));

	assertFalse(auth.isExpired(), "Token should not be expired");
    }

    @Test
    void isExpired_shouldReturnFalse_whenExpirationIsNull() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setExpiration(null);

	assertFalse(auth.isExpired(), "Token without expiration should not be expired");
    }

    @Test
    void hasRole_shouldReturnTrue_whenRoleIsPresent() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setRoles(List.of("ADMIN", "USER"));

	assertTrue(auth.hasRole("ADMIN"));
	assertTrue(auth.hasRole("USER"));
    }

    @Test
    void hasRole_shouldReturnFalse_whenRoleIsMissing() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setRoles(List.of("USER"));

	assertFalse(auth.hasRole("ADMIN"));
    }

    @Test
    void hasRole_shouldReturnFalse_whenRolesIsNull() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setRoles(null);

	assertFalse(auth.hasRole("ADMIN"));
    }

    @Test
    void hasAnyRole_shouldReturnTrue_whenAnyRoleMatches() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setRoles(List.of("MODERATOR", "USER"));

	assertTrue(auth.hasAnyRole("ADMIN", "USER"));
	assertTrue(auth.hasAnyRole("MODERATOR"));
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenNoRolesMatch() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setRoles(List.of("VIEWER"));

	assertFalse(auth.hasAnyRole("ADMIN", "USER"));
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenRolesIsNull() {
	JwtAuthentication auth = new JwtAuthentication();
	auth.setRoles(null);

	assertFalse(auth.hasAnyRole("ADMIN"));
    }
}