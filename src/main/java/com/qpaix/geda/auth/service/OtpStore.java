package com.qpaix.geda.auth.service;

import java.time.Duration;

/**
 * Abstraction over OTP storage so the demo can run against an in-memory store
 * while allowing a future Redis-backed (or other) implementation to be swapped
 * in without touching call sites.
 */
public interface OtpStore {

    void put(Long userId, String code, Duration ttl);

    /**
     * Verifies the given code for the given user. On success the code is
     * invalidated (one-time use). Returns false if missing, expired, or mismatched.
     */
    boolean verify(Long userId, String code);

    void invalidate(Long userId);
}
