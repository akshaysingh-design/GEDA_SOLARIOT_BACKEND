package com.qpaix.geda.auth.service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "otp_code")
@Getter
@Setter
@NoArgsConstructor
public class OtpCode {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public OtpCode(Long userId, String code, Instant expiresAt) {
        this.userId = userId;
        this.code = code;
        this.expiresAt = expiresAt;
    }
}
