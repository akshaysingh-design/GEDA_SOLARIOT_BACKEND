-- V4: OTP code storage, moved from in-process memory to the database so
-- verification works correctly across multiple backend instances/restarts
-- (Render can run more than one instance; an in-memory map only works on a
-- single JVM). One row per user; each login overwrites the previous code.

CREATE TABLE otp_code (
    user_id BIGINT PRIMARY KEY,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_otp_code_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
