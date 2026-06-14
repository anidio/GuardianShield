package com.guardian.shield;

import com.guardian.shield.service.SecurityScannerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShieldApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShieldApplication.class, args);
	}

	@Bean
	CommandLineRunner runScanner(SecurityScannerService scanner) {
		return args -> {
			// Teste de fogo: Google não faz POST na home,
			// mas vamos ver o log de navegação confirmando que o motor está vivo
			scanner.scanUrl("https://g1.globo.com");
		};
	}
}