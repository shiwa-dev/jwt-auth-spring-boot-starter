package dev.shiwa.jwtstarter.demo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import dev.shiwa.jwtstarter.core.refresh.RefreshTokenService;
import dev.shiwa.jwtstarter.core.refresh.RefreshTokenStore;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for handling user authentication.
 *
 * <p>
 * This controller exposes a demo login endpoint that generates a JWT token if
 * hardcoded credentials are provided. The token includes predefined roles and
 * can be used to test protected endpoints.
 * </p>
 *
 * <p>
 * Note: This implementation is for demonstration purposes only and should not
 * be used in production environments.
 * </p>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login & Token Generation")
public class JwtLoginController {

    private final JwtTokenGenerator generator;
    private final JwtTokenVerifier verifier;
    private final RefreshTokenService refreshService;
    private final RefreshTokenStore store;
    private final JwtAuthProperties jwtAuthProperties;

    public JwtLoginController(JwtTokenGenerator g, JwtTokenVerifier v, RefreshTokenService rs, RefreshTokenStore s,
	    JwtAuthProperties jwtAuthProperties) {
	this.generator = g;
	this.verifier = v;
	this.refreshService = rs;
	this.store = s;
	this.jwtAuthProperties = jwtAuthProperties;
    }

    /**
     * Performs a demo login and generates a JWT token.
     *
     * <p>
     * The endpoint accepts a username and password. If the credentials match the
     * hardcoded values ("admin" / "password"), a JWT token is returned that
     * includes the roles "USER" and "ADMIN".
     * </p>
     *
     * @param loginRequest the login credentials provided in the request body
     * @return HTTP 200 with token if credentials are valid, HTTP 401 with an error
     *         message otherwise
     */
    @PostMapping("/login")
    @Operation(summary = "Demo login", description = "Generates a JWT token for a hardcoded user. Only for testing purposes.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Login successful, token returned"),
	    @ApiResponse(responseCode = "401", description = "Unauthorized – invalid credentials") })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
	if (!"admin".equals(loginRequest.username()) || !"password".equals(loginRequest.password()))
	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

	List<String> roles = List.of("ADMIN", "USER");
	String access = generator.generateAccessToken("admin", roles);
	String refresh = generator.generateRefreshToken("admin");

	Claims rt = verifier.parse(refresh);
	store.save(rt.getId(), rt.getSubject(), rt.getExpiration().toInstant());

	long expAt = System.currentTimeMillis() + jwtAuthProperties.getTtlMillis();

	return ResponseEntity.ok(new LoginResponse(access, refresh, expAt));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest req) {
	RefreshTokenService.Tokens t = refreshService.refresh(req.refreshToken());
	return ResponseEntity
		.ok(new RefreshResponse(t.accessToken(), t.refreshToken(), t.accessTokenExpiresAtMillis()));
    }

    /**
     * Request body used for the demo login endpoint.
     *
     * @param username the username
     * @param password the password
     */
    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String accessToken, String refreshToken, long accessTokenExpiresAtMillis) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record RefreshResponse(String accessToken, String refreshToken, long accessTokenExpiresAtMillis) {
    }
}