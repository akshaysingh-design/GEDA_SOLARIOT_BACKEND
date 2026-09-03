package com.qpaix.geda.auth.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final OtpStore otpStore;

    /**
     * Generates a 6-digit OTP, stores it for the given user, logs it, and
     * returns the raw code so the caller (AuthController) can decide whether
     * to expose it in the API response based on app.otp.dev-expose.
     */
    // TEMP: fixed OTP for local demo convenience. Revert to the random
    // generator below once real SMS/email delivery is wired up.
    private static final String FIXED_DEMO_OTP = "123456";

    public String generateAndStore(Long userId) {
        String code = FIXED_DEMO_OTP;
        // String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        otpStore.put(userId, code, OTP_TTL);
        log.info("Generated OTP for userId={}: {}", userId, code);
        return code;
    }

    public boolean verify(Long userId, String code) {
        return otpStore.verify(userId, code);
    }
}
