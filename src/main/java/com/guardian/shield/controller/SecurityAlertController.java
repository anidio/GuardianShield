package com.guardian.shield.controller;

import com.guardian.shield.model.SecurityAlert;
import com.guardian.shield.service.SecurityScannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas de Segurança", description = "Endpoints para monitoramento de ameaças")
public class SecurityAlertController {

    private final SecurityScannerService scannerService;

    @GetMapping
    @Operation(summary = "Lista todos os alertas detectados", description = "Retorna os últimos alertas de segurança processados pelo GuardianShield")
    public List<SecurityAlert> getAllAlerts() {
        return scannerService.getAllAlerts();
    }
}