package com.guardian.shield.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private String destination;
    private String method;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private int riskScore;
    private String reason;
}