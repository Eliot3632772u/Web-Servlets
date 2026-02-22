package fr._42.cinema.models;

import java.time.*;

public class History
{
    private Long id;
    private Long userId;
    private String ipAddress;
    private LocalDateTime loginAt;

    public History() {
    }

    public History(Long id, Long userId, String ipAddress, LocalDateTime loginAt) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.loginAt = loginAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(LocalDateTime loginAt) {
        this.loginAt = loginAt;
    }
}