package com.factory.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "login_history", indexes = {
        @Index(name = "idx_login_history_user", columnList = "user_id"),
        @Index(name = "idx_login_history_time", columnList = "timestamp")
})
public class LoginHistory {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "login_status", nullable = false, length = 50)
    private String loginStatus; // SUCCESS, FAILED_INVALID_PASSWORD, FAILED_USER_NOT_FOUND, LOCKED

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public LoginHistory() {}

    public LoginHistory(String id, String userId, String email, String loginStatus, String ipAddress, String userAgent) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.loginStatus = loginStatus;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
