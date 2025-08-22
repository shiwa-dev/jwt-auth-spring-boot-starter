package dev.shiwa.jwtstarter.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dev.shiwa.jwtstarter.core.JwtAuthentication;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtVerificationControllerTest {

    @LocalServerPort
    private int port;

    @Value("${jwt.auth.issuer}")
    private String issuer;

    @Value("${jwt.auth.secret}")
    private String secret;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void verify_validTokenReturnsTrue() {
	// Token erzeugen (gültig)
	String token = generateJwt("test-user", 60000); // 1 Min

	assertTokenValid(token, true, "verify");
    }

    @Test
    void verify_invalidTokenReturnsUnauthorized() {
	// given
	String invalidToken = "Bearer this.is.not.a.valid.jwt";

	HttpHeaders headers = new HttpHeaders();
	headers.setBearerAuth(invalidToken);
	HttpEntity<Void> entity = new HttpEntity<>(headers);

	// when + then
	final var response = restTemplate.exchange("http://localhost:" + port + "/api/verify", HttpMethod.GET, entity,
		Void.class);

	assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void verify_expiredTokenReturnsUnauthorized() {
	// given
	String expiredToken = generateJwt("test-user", -60000);

	HttpHeaders headers = new HttpHeaders();
	headers.setBearerAuth(expiredToken);
	HttpEntity<Void> entity = new HttpEntity<>(headers);

	// when + then
	final var response = restTemplate.exchange("http://localhost:" + port + "/api/verify", HttpMethod.GET, entity,
		Void.class);

	assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void isAdmin_tokenWithAdminRoleReturnsTrue() {
	String token = generateTokenWithRoles("ADMIN");

	assertTokenValid(token, true, "is-admin");
    }

    @Test
    void isAdmin_tokenWithMultipleRolesIncludesAdminReturnsTrue() {
	String token = generateTokenWithRoles("USER", "ADMIN");

	assertTokenValid(token, true, "is-admin");
    }

    @Test
    void isAdmin_tokenWithoutAdminRoleReturnsFalse() {
	String token = generateTokenWithRoles("USER", "MODERATOR");

	assertTokenValid(token, false, "is-admin");
    }

    @Test
    void hasRole_userWithMatchingRoleReturnsTrue() {
	// Token mit Rollen: USER, ADMIN
	String token = generateTokenWithRoles("USER", "ADMIN");

	assertTokenValid(token, true, "has-role?roles=USER&roles=MODERATOR");
    }

    @Test
    void hasRole_userWithoutMatchingRoleReturnsFalse() {
	String token = generateTokenWithRoles("GUEST");

	assertTokenValid(token, false, "has-role?roles=USER&roles=MODERATOR");
    }

    @Test
    void me_returnsValidJwtAuthentication() {
	// given
	String subject = "user123";
	List<String> roles = List.of("USER", "ADMIN");

	String token = generateTokenWithRoles(roles.toArray(new String[] {}));

	HttpHeaders headers = new HttpHeaders();
	headers.setBearerAuth(token);
	HttpEntity<Void> entity = new HttpEntity<>(headers);

	// when
	ResponseEntity<JwtAuthentication> response = restTemplate.exchange("http://localhost:" + port + "/api/me",
		HttpMethod.GET, entity, JwtAuthentication.class);

	// then
	assertEquals(HttpStatus.OK, response.getStatusCode());

	JwtAuthentication auth = response.getBody();
	assertNotNull(auth);
	assertEquals(subject, auth.getSubject());
	assertTrue(auth.getRoles().contains("USER"));
	assertTrue(auth.getRoles().contains("ADMIN"));
	assertFalse(auth.isExpired());
    }

    private String generateJwt(String subject, long ttlMillis) {
	Date now = new Date();
	Date expiry = new Date(now.getTime() + ttlMillis);

	return Jwts.builder().setSubject(subject).setExpiration(expiry).setIssuer(issuer)
		.signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256).compact();
    }

    private String generateTokenWithRoles(String... roles) {
	return Jwts.builder().setSubject("user123").claim("roles", List.of(roles))
		.setExpiration(new Date(System.currentTimeMillis() + 300000)).setIssuer(issuer)
		.setIssuedAt(Date.from(Instant.now()))
		.signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256).compact();
    }

    private void assertTokenValid(String token, boolean expectedValid, String endpoint) {
	final var response = performAuthenticatedRequest(endpoint, token, Boolean.class);

	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	assertThat(response.getBody()).isEqualTo(expectedValid);
    }

    private <T> ResponseEntity<T> performAuthenticatedRequest(String endpoint, String token, Class<T> responseType) {
	HttpHeaders headers = new HttpHeaders();
	headers.set("Authorization", "Bearer " + token);
	HttpEntity<String> entity = new HttpEntity<>(headers);

	return restTemplate.exchange("http://localhost:" + port + "/api/" + endpoint, HttpMethod.GET, entity,
		responseType);
    }

}