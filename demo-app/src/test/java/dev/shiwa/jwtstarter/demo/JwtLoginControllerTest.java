package dev.shiwa.jwtstarter.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JwtLoginControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void login_validCredentialsReturnToken() {
	// Arrange
	Map<String, String> loginRequest = Map.of("username", "admin", "password", "password");

	// Act
	ResponseEntity<Map> response = restTemplate.postForEntity("/auth/login", loginRequest, Map.class);

	// Assert
	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	assertThat(response.getBody()).containsKey("accessToken");
	assertThat(((String) response.getBody().get("accessToken"))).startsWith("ey");

	assertThat(response.getBody()).containsKey("refreshToken");
	assertThat(((String) response.getBody().get("refreshToken"))).startsWith("ey");
    }

    @Test
    void login_invalidCredentialsReturnUnauthorized() {
	// Arrange: JSON-Login-Payload
	Map<String, String> loginRequest = Map.of("username", "admin", "password", "wrong");

	HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.APPLICATION_JSON);
	HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);

	final var response = restTemplate.exchange("/auth/login", HttpMethod.POST, requestEntity, Void.class);

	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_wrongUsernameReturnsUnauthorized() {
	// Arrange: JSON-Login-Payload
	Map<String, String> loginRequest = Map.of("username", "wronguser", "password", "password");

	HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.APPLICATION_JSON);
	HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);

	final var response = restTemplate.exchange("/auth/login", HttpMethod.POST, requestEntity, Void.class);

	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
