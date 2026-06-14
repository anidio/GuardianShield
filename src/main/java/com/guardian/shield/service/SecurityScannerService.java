package com.guardian.shield.service;

import com.guardian.shield.model.SecurityAlert;
import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SecurityScannerService {

    private Playwright playwright;
    private Browser browser;

    @Getter
    private final List<SecurityAlert> alerts = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void init() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        log.info("GuardianShield: Motor de escaneamento inicializado com sucesso.");
    }

    // CRÍTICO: Garante que navegadores não fiquem abertos no background
    @PreDestroy
    public void cleanup() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        log.info("GuardianShield: Recursos de escaneamento liberados.");
    }

    public void scanUrl(String url) {
        log.info("Iniciando varredura em: {}", url);
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();

            // Adicionamos um listener que realmente processa as requisições
            page.onRequest(this::processRequest);

            page.navigate(url);
            // Aumentei o tempo para 10s para garantir a captura de sites lentos
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
            log.warn("🚨 ALERTA CRÍTICO: Possível vazamento em {}", destination);

            SecurityAlert alert = SecurityAlert.builder()
                    .url(destination)
                    .destination(destination)
                    .method("POST")
                    .payload(payload)
                    .riskScore(10)
                    .reason("Detecção de dados sensíveis (senha/credencial) em tráfego de saída")
                    .build();

            alerts.add(alert);
        }
    }

    private boolean isIgnoredDomain(String url) {
        return url.contains("google-analytics") ||
                url.contains("facebook.net") ||
                url.contains("facebook.com") ||
                url.contains("doubleclick.net"); // Adicionado mais um comum de ruído
    }

    private boolean isSensitive(String payload) {
        String p = payload.toLowerCase();
        return p.contains("password") ||
                p.contains("pwd") ||
                p.contains("token") ||
                p.contains("secret") ||
                p.contains("credit_card") ||
                p.contains("cpf"); // Incluindo CPF para conformidade com a LGPD no Brasil
    }
}