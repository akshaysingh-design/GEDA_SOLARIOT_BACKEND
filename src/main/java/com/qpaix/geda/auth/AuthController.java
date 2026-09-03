package com.qpaix.geda.auth;

import com.qpaix.geda.auth.dto.LoginRequest;
import com.qpaix.geda.auth.dto.LoginResponse;
import com.qpaix.geda.auth.dto.OtpVerifyRequest;
import com.qpaix.geda.auth.dto.OtpVerifyResponse;
import com.qpaix.geda.auth.dto.UserSummaryDto;
import com.qpaix.geda.auth.service.AuthService;
import com.qpaix.geda.common.ApiResponse;
import com.qpaix.geda.common.exception.ApiException;
import com.qpaix.geda.user.User;
import com.qpaix.geda.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${app.otp.dev-expose:false}")
    private boolean devExposeOtp;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getUsername(), request.getPassword(), devExposeOtp);
        return ApiResponse.ok(response);
    }

    @PostMapping("/otp/verify")
    public ApiResponse<OtpVerifyResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        OtpVerifyResponse response = authService.verifyOtp(request.getPendingToken(), request.getOtpCode());
        return ApiResponse.ok(response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Map<String, String>> forgotPassword() {
        return ApiResponse.ok(Map.of("message",
                "If an account with that username exists, password reset instructions have been sent."));
    }

    @GetMapping("/me")
    public ApiResponse<UserSummaryDto> me() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = (Long) principal;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User not found"));
        return ApiResponse.ok(authService.toSummary(user));
    }
}
