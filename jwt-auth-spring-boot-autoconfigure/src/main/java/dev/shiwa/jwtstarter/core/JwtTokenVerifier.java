package dev.shiwa.jwtstarter.core;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for verifying JWT tokens using the HMAC-SHA256 algorithm.
 *
 * <p>
 * This class is responsible for:
 * <ul>
 * <li>Validating the token signature</li>
 * <li>Checking expiration and issuer</li>
 * <li>Extracting authentication details such as subject and roles</li>
 * </ul>
 *
 * <p>
 * Only tokens signed with a secure HMAC secret (minimum 256 bits) are
 * supported.
 */
public class JwtTokenVerifier {

    /** Logger for verification events and errors. */
    private final Logger log = LoggerFactory.getLogger(JwtTokenVerifier.class);

    /** Secret key used to verify the JWT signature. */
    private final SecretKey secretKey;

    /** Configuration properties containing issuer, secret, and token rules. */
    private final JwtAuthProperties jwtAuthProperties;

    /**
     * Constructs a new {@code JwtTokenVerifier} using the given secret key.
     *
     * @param jwtAuthProperties the JWT configuration including secret and issuer
     * @throws IllegalArgumentException if the secret is null or shorter than 32
     *                                  characters
     */
    public JwtTokenVerifier(JwtAuthProperties jwtAuthProperties) {
	this.jwtAuthProperties = jwtAuthProperties;

	final var secret = jwtAuthProperties.getSecret();
	if (secret == null || secret.length() < 32) {
	    throw new IllegalArgumentException("JWT secret must be at least 32 characters long (256 bits) for HS256.");
	}
	this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Parses the given JWT token and extracts authentication information.
     *
     * <p>
     * The token must be signed with the configured secret key and contain valid
     * claims such as subject, roles, issuedAt, and expiration.
     *
     * @param token the JWT token (optionally prefixed with "Bearer ")
     * @return a {@link JwtAuthentication} object representing the parsed token
     * @throws JwtException if the token is invalid or cannot be parsed
     */
    public JwtAuthentication parseToken(String token) {
	Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(stripBearerPrefix(token))
		.getBody();

	String subject = claims.getSubject();
	List<String> roles = claims.get("roles", List.class);
	Instant issuedAt = claims.getIssuedAt().toInstant();
	Instant expiration = claims.getExpiration().toInstant();

	return new JwtAuthentication(subject, roles, issuedAt, expiration);
    }

    public Claims parse(String token) {
	return Jwts.parserBuilder().requireIssuer(jwtAuthProperties.getIssuer()).setSigningKey(secretKey).build()
		.parseClaimsJws(stripBearerPrefix(token)).getBody();
    }

    /**
     * Validates the given JWT token by checking:
     * <ul>
     * <li>its signature</li>
     * <li>its expiration timestamp</li>
     * <li>its issuer claim matches the configured issuer</li>
     * </ul>
     *
     * @param token the JWT token string (with or without "Bearer" prefix)
     * @return {@code true} if the token is valid and not expired; {@code false}
     *         otherwise
     */
    public boolean isValid(String token) {
	if (token == null)
	    return false;

	try {
	    Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
		    .parseClaimsJws(cleanToken(token));

	    String issuer = claims.getBody().getIssuer();

	    if (!jwtAuthProperties.getIssuer().equals(issuer)) {
		throw new JwtException("Invalid token issuer");
	    }

	    Date expiration = claims.getBody().getExpiration();
	    final var valid = expiration == null || expiration.after(new Date());

	    if (valid) {
		log.debug("✅ Token valid for subject: {}", claims.getBody().getSubject());
	    }

	    return valid;
	} catch (ExpiredJwtException e) {
	    log.warn("❌ Token expired: subject={}, expiredAt={}", e.getClaims().getSubject(),
		    e.getClaims().getExpiration());
	} catch (JwtException e) {
	    log.warn("❌ Invalid token: {}", e.getMessage());
	}

	return false;
    }

    public boolean isAccessToken(String token) {
	try {
	    return "access".equals(parse(token).get("type", String.class));
	} catch (JwtException e) {
	    return false;
	}
    }

    public boolean isRefreshToken(String token) {
	try {
	    return "refresh".equals(parse(token).get("type", String.class));
	} catch (JwtException e) {
	    return false;
	}
    }

    /**
     * Removes "Bearer" prefix from the token if present.
     *
     * @param token the raw token string (possibly with "Bearer" prefix)
     * @return the token string without "Bearer" prefix
     */
    private String cleanToken(String token) {
	if (token == null)
	    return "";
	return token.replace("Bearer", "").trim();
    }

    /**
     * Strips the "Bearer " prefix (case-sensitive) from the token if present.
     *
     * @param token the token string
     * @return the token without the "Bearer " prefix
     */
    private String stripBearerPrefix(String token) {
	if (token != null && token.startsWith("Bearer ")) {
	    return token.substring(7);
	}
	return token;
    }
}