package dev.shiwa.jwtstarter.demo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
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

    @Autowired
    private JwtTokenGenerator tokenGenerator;

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
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
	// Für Demo: Benutzername + Passwort hartkodiert prüfen
	if ("admin".equals(loginRequest.username()) && "password".equals(loginRequest.password())) {
	    String token = tokenGenerator.generateToken(loginRequest.username(), List.of("USER", "ADMIN"));
	    return ResponseEntity.ok(Map.of("token", token));
	} else {
	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
	}
    }

    /**
     * Request body used for the demo login endpoint.
     *
     * @param username the username
     * @param password the password
     */
    public record LoginRequest(String username, String password) {
    }
}