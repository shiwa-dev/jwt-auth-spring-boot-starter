package dev.shiwa.jwtstarter.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.shiwa.jwtstarter.autoconfigure.JwtAuthProperties;

class JwtAuthPropertiesTest {

    private JwtAuthProperties props;

    @BeforeEach
    void setUp() {
	props = new JwtAuthProperties();
    }

    @Test
    void testIssuerGetterAndSetter() {
	props.setIssuer("my-issuer");
	assertEquals("my-issuer", props.getIssuer());
    }

    @Test
    void testSecretGetterAndSetter() {
	props.setSecret("super-secret-key");
	assertEquals("super-secret-key", props.getSecret());
    }

    @Test
    void testTtlMillisGetterAndSetter() {
	props.setTtlMillis(3600000L); // 1 hour
	assertEquals(3600000L, props.getTtlMillis());
    }

    @Test
    void testHeaderDefaultValue() {
	assertEquals("Authorization", props.getHeader());
    }

    @Test
    void testHeaderSetterAndGetter() {
	props.setHeader("X-Custom-Header");
	assertEquals("X-Custom-Header", props.getHeader());
    }

    @Test
    void testProtectedPathsDefault() {
	assertEquals(List.of("/api/*"), props.getProtectedPaths());
    }

    @Test
    void testSetProtectedPaths() {
	List<String> paths = List.of("/secure/*", "/admin/*");
	props.setProtectedPaths(paths);
	assertEquals(paths, props.getProtectedPaths());
    }

    @Test
    void testExcludedPathsDefault() {
	assertTrue(props.getExcludedPaths().isEmpty());
    }

    @Test
    void testSetExcludedPaths() {
	List<String> paths = List.of("/login", "/public");
	props.setExcludedPaths(paths);
	assertEquals(paths, props.getExcludedPaths());
    }
}