# Container da camada MOBILE.
# Etapa 1 compila o APK do app Android (Kotlin + Jetpack Compose) com Gradle e Android SDK.
# Etapa 2 publica o APK por HTTP para ser instalado no celular ou no emulador.

# ---------- Etapa 1: build do APK ----------
FROM eclipse-temurin:17-jdk AS build

ARG ANDROID_CMDLINE_TOOLS=11076708
ARG GRADLE_VERSION=8.14.3
ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_HOME=/opt/gradle
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${GRADLE_HOME}/bin

RUN apt-get update && apt-get install -y --no-install-recommends unzip wget && rm -rf /var/lib/apt/lists/*

# Android SDK (somente o necessário para compilar)
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
 && wget -q https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS}_latest.zip -O /tmp/tools.zip \
 && unzip -q /tmp/tools.zip -d ${ANDROID_HOME}/cmdline-tools \
 && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
 && rm /tmp/tools.zip \
 && yes | sdkmanager --licenses > /dev/null \
 && sdkmanager "platforms;android-36" "build-tools;36.0.0" "platform-tools" > /dev/null

# Gradle (o projeto não versiona o gradle-wrapper.jar)
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip \
 && unzip -q /tmp/gradle.zip -d /opt && mv /opt/gradle-${GRADLE_VERSION} ${GRADLE_HOME} && rm /tmp/gradle.zip

WORKDIR /app
COPY . .

# Endereço da API que será embutido no APK (vem do docker-compose)
ARG SOS_API_URL=http://10.0.2.2:3000/
RUN gradle --no-daemon assembleDebug test -PSOS_API_URL=${SOS_API_URL}

# ---------- Etapa 2: distribuição ----------
FROM nginx:1.27-alpine
COPY docker/index.html /usr/share/nginx/html/index.html
COPY --from=build /app/app/build/outputs/apk/debug/app-debug.apk /usr/share/nginx/html/sos-manaus.apk
EXPOSE 80
