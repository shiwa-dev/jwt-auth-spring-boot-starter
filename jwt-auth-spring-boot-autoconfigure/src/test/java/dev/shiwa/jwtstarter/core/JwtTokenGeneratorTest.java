package dev.shiwa.jwtstarter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtTokenGeneratorTest {

    private JwtTokenGenerator generator;
    private JwtAuthProperties props;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
	props = new JwtAuthProperties();
	props.setIssuer("test-issuer");
	props.setTtlMillis(3600000); // 1 hour

	props.setSecret("my-super-secret-key-1234567890!!");
	this.secretKey = Keys.hmacShaKeyFor(props.getSecret().getBytes());

	generator = new JwtTokenGenerator(props);
    }

    @Test
    void generateToken_shouldReturnValidJwt() {
	String subject = "alice";
	List<String> roles = List.of("USER", "ADMIN");

	String token = generator.generateToken(subject, roles);
	assertNotNull(token);
	assertFalse(token.isBlank());

	// Parse & validate token
	Jws<Claims> parsed = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

	Claims claims = parsed.getBody();

	assertEquals(subject, claims.getSubject());
	assertEquals(props.getIssuer(), claims.getIssuer());
	assertNotNull(claims.getIssuedAt());
	assertNotNull(claims.getExpiration());
	assertTrue(claims.getExpiration().after(new Date()));

	@SuppressWarnings("unchecked")
	List<String> tokenRoles = claims.get("roles", List.class);
	assertTrue(tokenRoles.contains("USER"));
	assertTrue(tokenRoles.contains("ADMIN"));
    }
}