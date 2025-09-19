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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * REST controller for handling user authentication.
 *
 * <p>
 * This controller exposes endpoints for demo login and refresh token handling.
 * The login endpoint issues access and refresh tokens if hardcoded credentials
 * are provided. The refresh endpoint exchanges a valid refresh token for new
 * tokens.
 * </p>
 *
 * <p>
 * <b>Note:</b> This implementation is for demonstration purposes only and
 * should not be used in production environments.
 * </p>
 *
 * <h3>🔑 Example Usage</h3>
 *
 * <h4>Login</h4>
 * 
 * <pre>{@code
 * curl -X POST http://localhost:8080/auth/login \
 *   -H "Content-Type: application/json" \
 *   -d '{"username":"admin","password":"password"}'
 * }</pre>
 * 
 * Example response:
 * 
 * <pre>{@code
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "accessTokenExpiresAtMillis": 1737200000000
 * }
 * }</pre>
 *
 * <h4>Refresh</h4>
 * 
 * <pre>{@code
 * curl -X POST http://localhost:8080/auth/refresh \
 *   -H "Content-Type: application/json" \
 *   -d '{"refreshToken":"<your-refresh-token>"}'
 * }</pre>
 * 
 * Example response:
 * 
 * <pre>{@code
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "accessTokenExpiresAtMillis": 1737203600000
 * }
 * }</pre>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login & Token Generation")
public class JwtAuthController {

    private final JwtTokenGenerator generator;
    private final JwtTokenVerifier verifier;
    private final RefreshTokenService refreshService;
    private final RefreshTokenStore store;
    private final JwtAuthProperties jwtAuthProperties;

    public JwtAuthController(JwtTokenGenerator g, JwtTokenVerifier v, RefreshTokenService rs, RefreshTokenStore s,
	    JwtAuthProperties jwtAuthProperties) {
	this.generator = g;
	this.verifier = v;
	this.refreshService = rs;
	this.store = s;
	this.jwtAuthProperties = jwtAuthProperties;
    }

    /**
     * Performs a demo login and generates JWT tokens.
     *
     * <p>
     * The endpoint accepts a username and password. If the credentials match the
     * hardcoded values ("admin" / "password"), both an access token and a refresh
     * token are returned. The access token includes the roles "USER" and "ADMIN".
     * </p>
     *
     * @param loginRequest the login credentials provided in the request body
     * @return HTTP 200 with {@link LoginResponse} if credentials are valid, HTTP
     *         401 if invalid
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

	long expAt = System.currentTimeMillis() + jwtAuthProperties.getAccessTtlMillis();

	return ResponseEntity.ok(new LoginResponse(access, refresh, expAt));
    }

    /**
     * Refreshes an expired or soon-to-expire access token using a valid refresh
     * token.
     *
     * <p>
     * If the provided refresh token is valid and active, new access and refresh
     * tokens are generated and returned. This endpoint relies on
     * {@link RefreshTokenService} for validation, rotation, and reuse detection.
     * </p>
     *
     * @param req the refresh request containing the refresh token
     * @return HTTP 200 with {@link RefreshResponse} if refresh is successful, HTTP
     *         401 or 400 if the token is invalid, expired, or reused
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a refresh token for new access and refresh tokens.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "New tokens returned"),
	    @ApiResponse(responseCode = "401", description = "Unauthorized – invalid or expired refresh token") })
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest req) {
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

    /**
     * Response body returned after successful login.
     *
     * @param accessToken                the issued access token
     * @param refreshToken               the issued refresh token
     * @param accessTokenExpiresAtMillis timestamp (epoch millis) when the access
     *                                   token expires
     */
    public record LoginResponse(String accessToken, String refreshToken, long accessTokenExpiresAtMillis) {
    }

    /**
     * Request body used for the refresh endpoint.
     *
     * @param refreshToken the refresh token used to obtain new tokens
     */
    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    /**
     * Response body returned after a successful refresh.
     *
     * @param accessToken                the newly issued access token
     * @param refreshToken               the newly issued refresh token
     * @param accessTokenExpiresAtMillis timestamp (epoch millis) when the new
     *                                   access token expires
     */
    public record RefreshResponse(String accessToken, String refreshToken, long accessTokenExpiresAtMillis) {
    }
}