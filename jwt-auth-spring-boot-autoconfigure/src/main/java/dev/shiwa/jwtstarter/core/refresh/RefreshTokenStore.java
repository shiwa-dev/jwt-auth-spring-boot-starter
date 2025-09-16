package dev.shiwa.jwtstarter.core.refresh;

import java.time.Instant;

public interface RefreshTokenStore {
    void save(String jti, String subject, Instant expiresAt);

    boolean isActive(String jti);

    String subjectFor(String jti); // optional

    void revoke(String jti);

    void revokeAllForSubject(String subject);
}