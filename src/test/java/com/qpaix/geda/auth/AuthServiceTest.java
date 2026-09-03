package com.qpaix.geda.auth;

import com.qpaix.geda.auth.dto.LoginResponse;
import com.qpaix.geda.auth.dto.OtpVerifyResponse;
import com.qpaix.geda.auth.service.AuthService;
import com.qpaix.geda.auth.service.OtpService;
import com.qpaix.geda.security.JwtService;
import com.qpaix.geda.user.Role;
import com.qpaix.geda.user.User;
import com.qpaix.geda.user.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    private User mfaUser;
    private User nonMfaUser;

    @BeforeEach
    void setUp() {
        Role superAdmin = new Role();
        superAdmin.setId(1L);
        superAdmin.setName("SUPER_ADMIN");

        mfaUser = new User();
        mfaUser.setId(1L);
        mfaUser.setUsername("admin");
        mfaUser.setPasswordHash("hashed-password");
        mfaUser.setFullName("System Administrator");
        mfaUser.setRole(superAdmin);
        mfaUser.setEnabled(true);
        mfaUser.setMfaRequired(true);

        Role viewer = new Role();
        viewer.setId(4L);
        viewer.setName("VIEWER");

        nonMfaUser = new User();
        nonMfaUser.setId(2L);
        nonMfaUser.setUsername("viewer1");
        nonMfaUser.setPasswordHash("hashed-password-2");
        nonMfaUser.setFullName("View Only User");
        nonMfaUser.setRole(viewer);
        nonMfaUser.setEnabled(true);
        nonMfaUser.setMfaRequired(false);
    }

    @Test
    void login_withMfaRequiredUser_returnsPendingToken() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mfaUser));
        when(passwordEncoder.matches("Admin@123", "hashed-password")).thenReturn(true);
        when(jwtService.issuePendingMfaToken(1L)).thenReturn("pending-jwt-token");
        when(otpService.generateAndStore(1L)).thenReturn("123456");

        LoginResponse response = authService.login("admin", "Admin@123", true);

        assertThat(response.isMfaRequired()).isTrue();
        assertThat(response.getPendingToken()).isEqualTo("pending-jwt-token");
        assertThat(response.getDevOtpCode()).isEqualTo("123456");
        assertThat(response.getAccessToken()).isNull();
    }

    @Test
    void login_withMfaRequiredUser_hidesOtpWhenDevExposeDisabled() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mfaUser));
        when(passwordEncoder.matches("Admin@123", "hashed-password")).thenReturn(true);
        when(jwtService.issuePendingMfaToken(1L)).thenReturn("pending-jwt-token");
        when(otpService.generateAndStore(1L)).thenReturn("123456");

        LoginResponse response = authService.login("admin", "Admin@123", false);

        assertThat(response.isMfaRequired()).isTrue();
        assertThat(response.getDevOtpCode()).isNull();
    }

    @Test
    void login_withoutMfaRequired_returnsAccessTokenDirectly() {
        when(userRepository.findByUsername("viewer1")).thenReturn(Optional.of(nonMfaUser));
        when(passwordEncoder.matches("Pass@123", "hashed-password-2")).thenReturn(true);
        when(jwtService.issueAccessToken(eq(2L), any(), any())).thenReturn("access-jwt-token");

        LoginResponse response = authService.login("viewer1", "Pass@123", false);

        assertThat(response.isMfaRequired()).isFalse();
        assertThat(response.getAccessToken()).isEqualTo("access-jwt-token");
        assertThat(response.getUser().getUsername()).isEqualTo("viewer1");
    }

    @Test
    void login_withBadPassword_throwsBadCredentials() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mfaUser));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("admin", "wrong", false))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void verifyOtp_withValidPendingTokenAndCode_returnsAccessToken() {
        Claims claims = mock(Claims.class);
        when(jwtService.parseClaims("pending-jwt-token")).thenReturn(claims);
        when(jwtService.getTokenType(claims)).thenReturn(JwtService.TOKEN_TYPE_PENDING_MFA);
        when(jwtService.isExpired(claims)).thenReturn(false);
        when(jwtService.getUserId(claims)).thenReturn(1L);
        when(otpService.verify(1L, "123456")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mfaUser));
        when(jwtService.issueAccessToken(eq(1L), any(), any())).thenReturn("real-access-token");

        OtpVerifyResponse response = authService.verifyOtp("pending-jwt-token", "123456");

        assertThat(response.getAccessToken()).isEqualTo("real-access-token");
        assertThat(response.getUser().getUsername()).isEqualTo("admin");
    }

    @Test
    void verifyOtp_withWrongCode_throws() {
        Claims claims = mock(Claims.class);
        when(jwtService.parseClaims("pending-jwt-token")).thenReturn(claims);
        when(jwtService.getTokenType(claims)).thenReturn(JwtService.TOKEN_TYPE_PENDING_MFA);
        when(jwtService.isExpired(claims)).thenReturn(false);
        when(jwtService.getUserId(claims)).thenReturn(1L);
        when(otpService.verify(1L, "000000")).thenReturn(false);

        assertThatThrownBy(() -> authService.verifyOtp("pending-jwt-token", "000000"))
                .isInstanceOf(com.qpaix.geda.common.exception.ApiException.class);
    }
}
