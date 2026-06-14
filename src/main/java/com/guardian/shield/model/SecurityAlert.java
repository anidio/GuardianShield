package com.guardian.shield.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SecurityAlert {
    private String url;
    private String destination;
    private String method;
    private String payload;
    private int riskScore; // 0 a 10
    private String reason;
}