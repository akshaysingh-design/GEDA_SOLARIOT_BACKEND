package com.qpaix.geda.auth.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOtpStore implements OtpStore {

    private final Map<Long, OtpEntry> store = new ConcurrentHashMap<>();

    @Override
    public void put(Long userId, String code, Duration ttl) {
        store.put(userId, new OtpEntry(code, Instant.now().plus(ttl)));
    }

    @Override
    public boolean verify(Long userId, String code) {
        OtpEntry entry = store.get(userId);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiry())) {
            store.remove(userId);
            return false;
        }
        boolean matches = entry.code().equals(code);
        if (matches) {
            store.remove(userId);
        }
        return matches;
    }

    @Override
    public void invalidate(Long userId) {
        store.remove(userId);
    }

    private record OtpEntry(String code, Instant expiry) {
    }
}
