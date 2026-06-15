package com.guardian.shield.service;

import com.guardian.shield.model.SecurityAlert;
import com.guardian.shield.repository.SecurityAlertRepository;
import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityScannerService {

    // Injetando o repositório pelo construtor (gerado pelo Lombok)
    private final SecurityAlertRepository alertRepository;

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        log.info("GuardianShield: Motor de escaneamento inicializado com sucesso.");
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        log.info("GuardianShield: Recursos de escaneamento liberados.");
    }

    public List<SecurityAlert> getAllAlerts() {
        // buscamos direto do banco de dados!
        return alertRepository.findAll();
    }

    public void scanUrl(String url) {
        log.info("Iniciando varredura em: {}", url);
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.onRequest(this::processRequest);
            page.navigate(url);
            page.waitForTimeout(10000);
        } catch (Exception e) {
            log.error("Erro ao escanear URL {}: {}", url, e.getMessage());
        }
    }

    private void processRequest(Request request) {
        if (!"POST".equals(request.method())) {
            return;
        }

        String destination = request.url();
        String payload = request.postData();

        if (isIgnoredDomain(destination)) {
            return;
        }

        if (payload != null && isSensitive(payload)) {
            log.warn("🚨 ALERTA CRÍTICO: Possível vazamento detectado em {}", destination);

            SecurityAlert alert = SecurityAlert.builder()
                    .url(destination)
                    .destination(destination)
                    .method("POST")
                    .payload(payload)
                    .riskScore(10)
                    .reason("Detecção de dados sensíveis (senha/credencial/CPF) em tráfego de saída")
                    .build();

            // Salva diretamente no PostgreSQL via JPA
            alertRepository.save(alert);
            log.info("✅ Alerta persistido no banco de dados.");
        }
    }

    private boolean isIgnoredDomain(String url) {
        return url.contains("google-analytics") ||
                url.contains("facebook.net") ||
                url.contains("facebook.com") ||
                url.contains("doubleclick.net");
    }

    private boolean isSensitive(String payload) {
        String p = payload.toLowerCase();
        return p.contains("password") ||
                p.contains("pwd") ||
                p.contains("token") ||
                p.contains("secret") ||
                p.contains("credit_card") ||
                p.contains("cpf");
    }
}