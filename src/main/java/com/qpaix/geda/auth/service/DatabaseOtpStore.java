package com.qpaix.geda.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Stores OTP codes in the database rather than in-process memory, so
 * verification works correctly regardless of which backend instance handles
 * the request (e.g. Render running more than one instance, or an instance
 * restarting between login and verify).
 */
@Component
@RequiredArgsConstructor
public class DatabaseOtpStore implements OtpStore {

    private final OtpCodeRepository otpCodeRepository;

    @Override
    @Transactional
    public void put(Long userId, String code, Duration ttl) {
        OtpCode entry = otpCodeRepository.findById(userId)
                .orElseGet(() -> new OtpCode(userId, code, Instant.now().plus(ttl)));
        entry.setCode(code);
        entry.setExpiresAt(Instant.now().plus(ttl));
        otpCodeRepository.save(entry);
    }

    @Override
    @Transactional
    public boolean verify(Long userId, String code) {
        return otpCodeRepository.findById(userId)
                .filter(entry -> Instant.now().isBefore(entry.getExpiresAt()))
                .filter(entry -> entry.getCode().equals(code))
                .map(entry -> {
                    otpCodeRepository.deleteById(userId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void invalidate(Long userId) {
        otpCodeRepository.deleteById(userId);
    }
}
