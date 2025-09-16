package dev.shiwa.jwtstarter.core.refresh;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final ConcurrentMap<String, String> jtiToSubject = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> jtiToExpiry = new ConcurrentHashMap<>();

    @Override
    public void save(String jti, String subject, Instant exp) {
	jtiToSubject.put(jti, subject);
	jtiToExpiry.put(jti, exp);
    }

    @Override
    public boolean isActive(String jti) {
	Instant exp = jtiToExpiry.get(jti);
	return exp != null && Instant.now().isBefore(exp) && jtiToSubject.containsKey(jti);
    }

    @Override
    public String subjectFor(String jti) {
	return jtiToSubject.get(jti);
    }

    @Override
    public void revoke(String jti) {
	jtiToSubject.remove(jti);
	jtiToExpiry.remove(jti);
    }

    @Override
    public void revokeAllForSubject(String subject) {
	jtiToSubject.entrySet().removeIf(e -> {
	    if (Objects.equals(e.getValue(), subject)) {
		jtiToExpiry.remove(e.getKey());
		return true;
	    }
	    return false;
	});
    }
}