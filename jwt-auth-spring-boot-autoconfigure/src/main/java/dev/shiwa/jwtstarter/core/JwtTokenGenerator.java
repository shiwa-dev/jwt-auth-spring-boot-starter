package dev.shiwa.jwtstarter.core;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for generating JWT tokens.
 *
 * <p>
 * This component creates signed JWT tokens that include claims such as:
 * <ul>
 * <li>Subject (e.g. username or email)</li>
 * <li>Roles assigned to the user</li>
 * <li>Issuer (iss)</li>
 * <li>Issued time and expiration</li>
 * </ul>
 *
 * <p>
 * It uses the secret key and expiration settings defined in
 * {@link JwtAuthProperties}. This class is typically used after successful
 * authentication (e.g., login) to generate a token for the client.
 */
public class JwtTokenGenerator {

    /** Logger for monitoring token generation and debugging. */
    private static final Logger log = LoggerFactory.getLogger(JwtTokenGenerator.class);

    /** JWT configuration properties injected via constructor. */
    private final JwtAuthProperties properties;

    /** The HMAC secret key used to sign the JWT tokens. */
    private SecretKey secretKey;

    /**
     * Constructs a {@code JwtTokenGenerator} with the given JWT configuration.
     *
     * @param properties the JWT configuration (e.g. secret, issuer, expiration
     *                   time)
     */
    public JwtTokenGenerator(JwtAuthProperties properties) {
	this.properties = properties;
	this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes());
    }

    @Deprecated
    public String generateToken(String subject, List<String> roles) {
	return this.generateAccessToken(subject, roles);
    }

    /**
     * Generates a signed JWT token for the specified subject and roles.
     *
     * <p>
     * The token will contain the following claims:
     * <ul>
     * <li>{@code sub}: the subject (user ID or username)</li>
     * <li>{@code roles}: a list of roles as custom claim</li>
     * <li>{@code iss}: the issuer</li>
     * <li>{@code iat}: issued-at timestamp</li>
     * <li>{@code exp}: expiration timestamp</li>
     * </ul>
     *
     * @param subject the user identity (typically email or username)
     * @param roles   the roles granted to the user
     * @return a signed JWT token string
     * @throws RuntimeException if token creation fails
     */
    public String generateAccessToken(String subject, List<String> roles) {
	try {
	    long nowMillis = System.currentTimeMillis();
	    Date now = new Date(nowMillis);
	    Date expiry = new Date(nowMillis + properties.getTtlMillis());

	    final var token = Jwts.builder().setSubject(subject).claim("roles", roles).claim("type", "access")
		    .setIssuer(properties.getIssuer()).setIssuedAt(now).setExpiration(expiry)
		    .signWith(secretKey, SignatureAlgorithm.HS256).compact();

	    log.info("🔐 Token generated for subject: {}", subject);
	    log.debug("→ roles={}, expiresIn={}s", roles, properties.getTtlMillis());

	    return token;
	} catch (Exception e) {
	    log.error("🚨 Failed to generate token for subject '{}': {}", subject, e.getMessage());
	    throw e;
	}
    }

    public String generateRefreshToken(String subject) {
	long now = System.currentTimeMillis();
	String jti = UUID.randomUUID().toString();
	return Jwts.builder().setSubject(subject).setIssuer(properties.getIssuer()).setId(jti) // jti für
											       // Store/Revocation
		.setIssuedAt(new Date(now)).setExpiration(new Date(now + properties.getRefreshTtlMillis()))
		.claim("type", "refresh").signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }
}
