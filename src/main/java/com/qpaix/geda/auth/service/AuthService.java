package com.qpaix.geda.auth.service;

import com.qpaix.geda.auth.dto.LoginResponse;
import com.qpaix.geda.auth.dto.OtpVerifyResponse;
import com.qpaix.geda.auth.dto.UserSummaryDto;
import com.qpaix.geda.common.exception.ApiException;
import com.qpaix.geda.security.JwtService;
import com.qpaix.geda.user.User;
import com.qpaix.geda.user.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    public LoginResponse login(String username, String rawPassword, boolean devExposeOtp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "USER_DISABLED", "This account is disabled");
        }

        if (user.isMfaRequired()) {
            String pendingToken = jwtService.issuePendingMfaToken(user.getId());
            String otpCode = otpService.generateAndStore(user.getId());
            return LoginResponse.pendingMfa(pendingToken, devExposeOtp ? otpCode : null);
        }

        String accessToken = issueAccessTokenFor(user);
        return LoginResponse.directAccess(accessToken, toSummary(user));
    }

    public OtpVerifyResponse verifyOtp(String pendingToken, String otpCode) {
        Claims claims;
        try {
            claims = jwtService.parseClaims(pendingToken);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Invalid or expired pending token");
        }

        if (!JwtService.TOKEN_TYPE_PENDING_MFA.equals(jwtService.getTokenType(claims))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_TYPE", "Token is not a pending-MFA token");
        }

        if (jwtService.isExpired(claims)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Pending token has expired");
        }

        Long userId = jwtService.getUserId(claims);

        if (!otpService.verify(userId, otpCode)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_OTP", "Invalid or expired OTP code");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User not found"));

        String accessToken = issueAccessTokenFor(user);
        return new OtpVerifyResponse(accessToken, toSummary(user));
    }

    private String issueAccessTokenFor(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "VIEWER";
        Long orgUnitId = user.getOrgUnit() != null ? user.getOrgUnit().getId() : null;
        return jwtService.issueAccessToken(user.getId(), List.of(roleName), orgUnitId);
    }

    public UserSummaryDto toSummary(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "VIEWER";
        Long orgUnitId = user.getOrgUnit() != null ? user.getOrgUnit().getId() : null;
        return new UserSummaryDto(user.getId(), user.getUsername(), user.getFullName(), roleName, orgUnitId);
    }
}
