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

    private final SecurityAlertRepository alertRepository;

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        log.info("GuardianShield: Motor de escaneamento baseado em Playwright inicializado com sucesso.");
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        log.info("GuardianShield: Recursos de escaneamento liberados.");
    }

    /**
     * Retorna todos os alertas salvos no PostgreSQL para renderização no Front-End.
     */
    public List<SecurityAlert> getAlerts() {
        return alertRepository.findAll();
    }

    /**
     * Executa a varredura ativa baseada na URL fornecida.
     */
    public void scanUrl(String url) {
        log.info("Iniciando varredura ativa em: {}", url);
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.onRequest(this::processRequest);
            page.navigate(url);
            page.waitForTimeout(10000); // 10 segundos inspecionando o tráfego de rede da página
        } catch (Exception e) {
            log.error("Erro ao escanear URL {}: {}", url, e.getMessage());
        }
    }

    /**
     * Método acionado pelo botão 'DISPARAR SCAN ATIVO' do Front-End (via Controller)
     * para simular um evento rápido e popular a interface em tempo real.
     */
    public List<SecurityAlert> runScan() {
        log.info("Disparando scan de contingência/teste sob demanda pelo painel web.");

        // Simula uma varredura em uma URL de teste local ou sandbox
        SecurityAlert sample = SecurityAlert.builder()
                .url("https://sandbox-auth.guardianshield.local/v1/oauth")
                .destination("https://api.external-leak-test.com/analytics")
                .method("POST")
                .payload("{ \"user\": \"admin\", \"secret_token\": \"gs_live_9a2f1c8e\" }")
                .riskScore(85) // Risco Alto
                .reason("Detecção de credenciais administrativas expostas em requisição de saída terceirizada.")
                .build();

        alertRepository.save(sample);
        return alertRepository.findAll();
    }

    /**
     * Interceptador de requisições de rede do Playwright
     */
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

            // Calcula a criticidade real com base no conteúdo do payload
            int calculatedScore = calculateRiskScore(payload);

            SecurityAlert alert = SecurityAlert.builder()
                    .url(destination)
                    .destination(destination)
                    .method("POST")
                    .payload(payload)
                    .riskScore(calculatedScore)
                    .reason("Detecção de dados sensíveis (senha/credencial/CPF) em tráfego de saída monitorado por Playwright.")
                    .build();

            // Salva diretamente no PostgreSQL via JPA
            alertRepository.save(alert);
            log.info("✅ Alerta persistido no banco de dados com score de risco: {}", calculatedScore);
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

    /**
     * Motor Auxiliar: Define matematicamente a gravidade do incidente de 0 a 100
     */
    private int calculateRiskScore(String payload) {
        String p = payload.toLowerCase();
        if (p.contains("password") || p.contains("pwd") || p.contains("cpf")) {
            return 95; // Risco Crítico -> Vai direto para o card vermelho de ameaças críticas do Next.js
        }
        if (p.contains("credit_card") || p.contains("secret")) {
            return 85; // Risco Alto -> Vai para o card laranja de riscos altos
        }
        return 60; // Risco Médio/Alto padrão para tokens genéricos
    }
}