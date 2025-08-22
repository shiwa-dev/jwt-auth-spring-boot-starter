package dev.shiwa.jwtstarter.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.shiwa.jwtstarter.core.JwtAuthentication;
import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for verifying JWT tokens and checking embedded roles.
 *
 * <p>
 * This controller provides endpoints to:
 * <ul>
 * <li>Validate the authenticity and integrity of a JWT token</li>
 * <li>Check whether a token contains specific roles (e.g., "ADMIN")</li>
 * <li>Return authentication information extracted from the token</li>
 * </ul>
 *
 * <p>
 * All endpoints require a valid JWT provided in the "Authorization" header
 * using the Bearer scheme.
 *
 * <p>
 * Secured using the OpenAPI security scheme named "bearerAuth".
 */
@RestController
@RequestMapping("/api")
@Tag(name = "JWT Verification", description = "JWT token verification and role checks")
@SecurityRequirement(name = "bearerAuth")
public class JwtVerificationController {

    @Autowired
    private JwtTokenGenerator tokenGenerator;

    private JwtTokenVerifier verifier;

    /**
     * Constructor for injecting a JWT token verifier.
     *
     * @param verifier the JWT token verifier
     */
    public JwtVerificationController(JwtTokenVerifier verifier) {
	this.verifier = verifier;
    }

    /**
     * Verifies if the provided JWT token is valid.
     *
     * @param token the JWT token from the Authorization header
     * @return HTTP 200 with true/false depending on token validity
     */
    @GetMapping("/verify")
    @Operation(summary = "Verify JWT token", description = "Checks whether the provided token is valid and properly signed.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Token is valid or invalid") })
    public ResponseEntity<?> verify(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
	return ResponseEntity.ok(verifier.isValid(token));
    }

    /**
     * Checks if the JWT token contains the "ADMIN" role.
     *
     * @param token the JWT token from the Authorization header
     * @return HTTP 200 with true/false depending on role presence
     */
    @GetMapping("/is-admin")
    @Operation(summary = "Check for ADMIN role", description = "Returns true if the token contains the 'ADMIN' role.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Role check completed") })
    public ResponseEntity<?> isAdmin(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
	return ResponseEntity.ok(verifier.parseToken(token).hasRole("ADMIN"));
    }

    /**
     * Checks if the JWT token contains any of the given roles.
     *
     * @param token the JWT token from the Authorization header
     * @param roles a list of roles to check against the token
     * @return HTTP 200 with true if at least one role is found, false otherwise
     */
    @GetMapping("/has-role")
    @Operation(summary = "Check for any role", description = "Checks whether the token contains any of the specified roles.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Role(s) checked successfully") })
    public ResponseEntity<?> hasAnyRole(@Parameter(hidden = true) @RequestHeader("Authorization") String token,
	    @Parameter(description = "Liste von Rollen, die geprüft werden sollen") @RequestParam(name = "roles") List<String> roles) {
	return ResponseEntity.ok(verifier.parseToken(token).hasAnyRole(roles.toArray(new String[0])));
    }

    /**
     * Returns authentication information extracted from the JWT token.
     *
     * @param token the JWT token from the Authorization header
     * @return the parsed authentication data
     */
    @GetMapping("/me")
    public JwtAuthentication me(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
	return verifier.parseToken(token);
    }

}