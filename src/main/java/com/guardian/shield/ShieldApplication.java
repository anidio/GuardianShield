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
			// Usamos uma Thread separada para o scan não bloquear o boot do Spring
			Thread scanThread = new Thread(() -> {
				scanner.scanUrl("https://g1.globo.com");
			});
			scanThread.start();
		};
	}
}