# Estágio 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# Estágio 2: Execução
FROM eclipse-temurin:21-jdk-jammy

# Instala as dependências que o Playwright/Chromium precisam no Linux
RUN apt-get update && apt-get install -y \
    libnss3 libx11-xcb1 libxcomposite1 libxcursor1 libxdamage1 libxext6 libxi6 \
    libxtst6 libcups2 libxss1 libxrandr2 libasound2 libpangocairo-1.0-0 libatk1.0-0 \
    libgtk-3-0 libgbm1 libdrm2 libdbus-glib-1-2 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]