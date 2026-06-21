package com.guardian.shield.controller;

import com.guardian.shield.model.SecurityAlert;
import com.guardian.shield.service.SecurityScannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas de Segurança", description = "Endpoints para monitoramento de ameaças")
@CrossOrigin(origins = "http://localhost:3000")
public class SecurityAlertController {

    private final SecurityScannerService scannerService;

    @GetMapping
    @Operation(summary = "Lista todos os alertas detectados", description = "Retorna os últimos alertas de segurança processados pelo GuardianShield")
    public List<SecurityAlert> getAllAlerts() {
        //Agora chamando o nome real do seu método no Service
        return scannerService.getAlerts();
    }

    @PostMapping("/scan-active")
    @Operation(summary = "Dispara uma varredura ativa via Playwright", description = "Recebe uma URL do frontend e inicia a inspeção de tráfego de rede em segundo plano")
    public List<SecurityAlert> triggerActiveScan(@RequestBody Map<String, String> request) {
        String targetUrl = request.get("url");

        if (targetUrl != null && !targetUrl.isEmpty()) {
            // Chama a varredura real do Playwright
            scannerService.scanUrl(targetUrl);
        }

        // Retorna a lista chamando o método real do seu Service
        return scannerService.getAlerts();
    }

    @PostMapping("/scan")
    @Operation(summary = "Dispara um scan de teste simulado", description = "Gera um alerta mockado para testar o ecossistema rapidamente")
    public List<SecurityAlert> triggerScan() {
        return scannerService.runScan();
    }
}