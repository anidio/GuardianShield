package com.guardian.shield.service;

import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SecurityScannerService {

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        log.info("GuardianShield: Motor de escaneamento inicializado.");
    }

    public void scanUrl(String url) {
        log.info("Iniciando varredura em: " + url);
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        page.onRequest(request -> {
            if ("POST".equals(request.method())) {
                // Aqui estamos capturando o payload bruto
                String postData = request.postData();
                String requestUrl = request.url();

                // Filtro inicial: ignorar domínios que não nos interessam (como o próprio FB para não poluir o log)
                if (!requestUrl.contains("facebook.com")) {
                    log.info("🚨 ALERTA DE DADOS: Destino: {}", requestUrl);
                    log.info("📦 Payload capturado: {}", postData != null ? postData : "Vazio");

                    // Futuro: analyzeWithAI(requestUrl, postData);
                }
            }
        });

        page.navigate(url);
        page.waitForTimeout(5000);
        context.close();
    }

    private void processRequest(Request request) {
        String destination = request.url();

        // Lista negra simples de domínios ignoráveis (não queremos poluir nossa IA)
        if (destination.contains("google-analytics") || destination.contains("facebook.net")) {
            return;
        }

        if ("POST".equals(request.method())) {
            String payload = request.postData();

            // Aqui entra a regra de ouro: se tiver campos sensíveis, sobe o alerta
            if (payload != null && payload.toLowerCase().contains("password")) {
                log.warn("🚨 ALERTA CRÍTICO: Possível vazamento de credenciais em {}", destination);
                // Aqui você chamaria o seu SecurityAnalyzer (IA)
            }
        }
    }
}