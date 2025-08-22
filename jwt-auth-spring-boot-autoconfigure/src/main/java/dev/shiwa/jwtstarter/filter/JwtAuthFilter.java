package dev.shiwa.jwtstarter.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet filter that validates incoming requests using JWT tokens.
 *
 * <p>
 * This filter checks if a request is protected and, if so, verifies the JWT
 * token in the {@code Authorization} header (with "Bearer " prefix). It
 * supports path-based exclusions and returns HTTP 401 on missing or invalid
 * tokens.
 *
 * <p>
 * Excluded paths can be configured via
 * {@link JwtAuthProperties#getExcludedPaths()}.
 */
public class JwtAuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtTokenVerifier verifier;
    private final JwtAuthProperties jwtAuthProperties;

    /**
     * Constructs the filter with the required verifier and configuration.
     *
     * @param verifier          the token verifier to validate JWTs
     * @param jwtAuthProperties the configuration properties (e.g. excluded paths)
     */
    public JwtAuthFilter(JwtTokenVerifier verifier, JwtAuthProperties jwtAuthProperties) {
	this.verifier = verifier;
	this.jwtAuthProperties = jwtAuthProperties;
    }

    /**
     * Filters HTTP requests and validates the JWT token if the path is protected.
     *
     * <p>
     * If the request URI matches one of the excluded paths, the request is passed
     * through without validation. Otherwise, the filter checks for the
     * {@code Authorization} header and verifies the JWT token.
     *
     * @param request  the incoming servlet request
     * @param response the servlet response
     * @param chain    the filter chain to proceed to the next filter or target
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if the request cannot be processed
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    throws IOException, ServletException {

	HttpServletRequest http = (HttpServletRequest) request;
	String path = http.getRequestURI();

	for (String excluded : jwtAuthProperties.getExcludedPaths()) {
	    if (path.matches(convertToRegex(excluded))) {
		log.debug("🔓 Path '{}' is excluded from token check", path);
		chain.doFilter(request, response);
		return;
	    }
	}

	String authHeader = http.getHeader("Authorization");

	if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	    log.warn("🚫 No Authorization header present: {}", http.getRequestURI());
	    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	    return;
	}

	String token = authHeader.substring(7);

	if (!verifier.isValid(token)) {
	    log.warn("❌ Invalid or expired token on path {}", http.getRequestURI());
	    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
	    return;
	}

	// Optional: Set attributes for downstream access
	http.setAttribute("jwt", token);

	log.debug("✅ Request authorized: {}", http.getRequestURI());
	chain.doFilter(request, response);
    }

    /**
     * Converts an Ant-style wildcard pattern to a regular expression.
     *
     * @param pattern the pattern to convert (e.g., {@code /api/*})
     * @return a regex string for matching request paths
     */
    private String convertToRegex(String pattern) {
	return pattern.replace("*", ".*");
    }
}
