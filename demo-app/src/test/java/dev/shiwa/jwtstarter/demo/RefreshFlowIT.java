package dev.shiwa.jwtstarter.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "jwt.auth.refresh-enabled=true", "jwt.auth.refresh-rotate=true",
	"jwt.auth.reuse-detection=true" })
public class RefreshFlowIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    // ---------- Helpers ----------

    private Map login() {
	ResponseEntity<Map> resp = rest.postForEntity("/auth/login",
		Map.of("username", "admin", "password", "password"), Map.class);
	assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
	return resp.getBody();
    }

    private ResponseEntity<Map> doRefresh(String refreshToken) {
	HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.APPLICATION_JSON);
	HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("refreshToken", refreshToken), headers);

	return rest.exchange("/auth/refresh", HttpMethod.POST, entity, Map.class);
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("login: valid credentials return access & refresh token and future expiry")
    void login_validCredentials_returnsTokensAndFutureExpiry() {
	ResponseEntity<Map> resp = rest.postForEntity("/auth/login",
		Map.of("username", "admin", "password", "password"), Map.class);

	assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
	assertThat(resp.getHeaders().getContentType()).isNotNull();
	assertThat(resp.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)).isTrue();

	Map body = resp.getBody();
	assertThat(body).containsKeys("accessToken", "refreshToken", "accessTokenExpiresAtMillis");

	String access = (String) body.get("accessToken");
	String refresh = (String) body.get("refreshToken");
	long expAt = ((Number) body.get("accessTokenExpiresAtMillis")).longValue();

	assertThat(access).startsWith("ey");
	assertThat(refresh).startsWith("ey");
	assertThat(expAt).isGreaterThan(Instant.now().toEpochMilli());
    }

    @Test
    @DisplayName("refresh: valid refresh token returns new tokens with valid expiration")
    void refresh_validTokenReturnsNewTokens() {
	Map login = login();

	String oldAccess = (String) login.get("accessToken");
	String oldRefresh = (String) login.get("refreshToken");
	long oldExp = ((Number) login.get("accessTokenExpiresAtMillis")).longValue();

	ResponseEntity<Map> resp = doRefresh(oldRefresh);
	assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

	Map body = resp.getBody();
	assertThat(body).containsKeys("accessToken", "refreshToken", "accessTokenExpiresAtMillis");

	String newAccess = (String) body.get("accessToken");
	String newRefresh = (String) body.get("refreshToken");
	long newExp = ((Number) body.get("accessTokenExpiresAtMillis")).longValue();

	assertThat(newAccess).startsWith("ey");
	assertThat(newRefresh).startsWith("ey");
	assertThat(newAccess).isNotEqualTo(oldAccess);
	assertThat(newRefresh).isNotEqualTo(oldRefresh);
	assertThat(newExp).isGreaterThan(Instant.now().toEpochMilli());
	assertThat(newExp).isGreaterThanOrEqualTo(oldExp);
    }

    @Test
    @DisplayName("refresh: rotation + reuse detection makes the old refresh token invalid")
    void refresh_rotationMakesOldTokenInvalid() {
	Map login = login();
	String initialRefresh = (String) login.get("refreshToken");

	// First refresh → triggers rotation
	ResponseEntity<Map> first = doRefresh(initialRefresh);
	assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

	// Reuse of the old token → must fail with 4xx
	ResponseEntity<Map> reuse = doRefresh(initialRefresh);
	assertThat(reuse.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("refresh: invalid refresh token returns 4xx")
    void refresh_invalidTokenReturnsClientError() {
	ResponseEntity<Map> resp = doRefresh("definitely-not-a-jwt");
	assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("refresh: empty refresh token returns 4xx")
    void refresh_emptyTokenReturnsClientError() {
	ResponseEntity<Map> resp = doRefresh("");
	assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("refresh: response is JSON and contains expected keys")
    void refresh_contentTypeAndKeys() {
	Map login = login();
	String rt = (String) login.get("refreshToken");

	HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.APPLICATION_JSON);
	HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("refreshToken", rt), headers);

	ResponseEntity<Map> resp = rest.exchange("/auth/refresh", HttpMethod.POST, entity, Map.class);
	assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
	assertThat(resp.getHeaders().getContentType()).isNotNull();
	assertThat(resp.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)).isTrue();
	assertThat(resp.getBody()).containsKeys("accessToken", "refreshToken", "accessTokenExpiresAtMillis");
    }
}