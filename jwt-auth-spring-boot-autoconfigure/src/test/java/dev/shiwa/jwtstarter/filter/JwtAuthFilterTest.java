package dev.shiwa.jwtstarter.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import dev.shiwa.jwtstarter.filter.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JwtAuthFilterTest {

    private JwtTokenVerifier verifier;
    private JwtAuthProperties props;
    private JwtAuthFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setup() {
	verifier = mock(JwtTokenVerifier.class);
	props = new JwtAuthProperties();
	props.setExcludedPaths(List.of("/public/*"));
	props.setSecret("a-very-long-and-secure-secret-value-here-1234");
	request = mock(HttpServletRequest.class);
	response = mock(HttpServletResponse.class);
	chain = mock(FilterChain.class);

	filter = new JwtAuthFilter(verifier, props);
    }

    @Test
    void shouldSkipFilterForExcludedPath() throws ServletException, IOException {
	when(request.getRequestURI()).thenReturn("/public/info");

	filter.doFilter(request, response, chain);

	verify(chain).doFilter(request, response);
	verifyNoInteractions(verifier);
	verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldRejectMissingAuthorizationHeader() throws ServletException, IOException {
	when(request.getRequestURI()).thenReturn("/api/data");
	when(request.getHeader("Authorization")).thenReturn(null);

	filter.doFilter(request, response, chain);

	verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldRejectInvalidToken() throws ServletException, IOException {
	when(request.getRequestURI()).thenReturn("/api/data");
	when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
	when(verifier.isValid("invalidtoken")).thenReturn(false);

	filter.doFilter(request, response, chain);

	verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
	verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldAcceptValidToken() throws ServletException, IOException {
	when(request.getRequestURI()).thenReturn("/api/data");
	when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
	when(verifier.isValid("validtoken")).thenReturn(true);

	filter.doFilter(request, response, chain);

	verify(chain).doFilter(request, response);
	verify(response, never()).sendError(anyInt(), anyString());
	verify(request).setAttribute("jwt", "validtoken");
    }
}