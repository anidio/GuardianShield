package com.guardian.shield.model;

public class SecurityEvent {
    private String ipAddress;
    private String endpoint;
    private String payload;
    private String username;

    // Construtores
    public SecurityEvent() {}

    public SecurityEvent(String ipAddress, String endpoint, String payload, String username) {
        this.ipAddress = ipAddress;
        this.endpoint = endpoint;
        this.payload = payload;
        this.username = username;
    }

    // Getters e Setters
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}