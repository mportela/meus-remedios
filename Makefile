# Makefile helper — Meus Remédios
# Atalhos para build, testes e execução local no emulador Android.
# Uso: `make <alvo>` (ex.: `make emulator`, `make run`, `make test`).
# Rode `make help` para ver todos os alvos.

# --- Configuração ----------------------------------------------------------
SHELL := /bin/bash

ANDROID_HOME ?= /opt/homebrew/share/android-commandlinetools
JAVA_HOME    ?= $(shell /usr/libexec/java_home -v 17 2>/dev/null)

ADB       := $(ANDROID_HOME)/platform-tools/adb
EMULATOR  := $(ANDROID_HOME)/emulator/emulator
AVDMANAGER:= $(ANDROID_HOME)/cmdline-tools/latest/bin/avdmanager
SDKMANAGER:= $(ANDROID_HOME)/cmdline-tools/latest/bin/sdkmanager

AVD_NAME    ?= meus_remedios_pixel6
AVD_DEVICE  ?= pixel_6
SYSTEM_IMAGE?= system-images;android-35;google_apis;arm64-v8a

APP_ID    := com.meusremedios
MAIN_ACT  := $(APP_ID)/.ui.MainActivity
APK_DEBUG := app/build/outputs/apk/debug/app-debug.apk

# Exporta variáveis de ambiente para os comandos do Gradle/SDK.
export ANDROID_HOME
export JAVA_HOME

GRADLE := ./gradlew --no-configuration-cache

.DEFAULT_GOAL := help

# --- Ajuda -----------------------------------------------------------------
.PHONY: help
help: ## Lista os alvos disponíveis
	@echo "Meus Remédios — alvos do Makefile:"
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| sort \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

# --- Build / Testes --------------------------------------------------------
.PHONY: build
build: ## Compila o APK de debug
	$(GRADLE) assembleDebug

.PHONY: test
test: ## Roda os testes unitários (JVM)
	$(GRADLE) test

.PHONY: check
check: ## Roda testes + build do APK de debug
	$(GRADLE) test assembleDebug

.PHONY: connected
connected: ## Roda testes instrumentados (precisa de emulador/device)
	$(GRADLE) connectedCheck

.PHONY: lint
lint: ## Roda o lint do Android
	$(GRADLE) lint

.PHONY: clean
clean: ## Limpa artefatos de build
	$(GRADLE) clean

# --- Emulador --------------------------------------------------------------
.PHONY: avd-create
avd-create: ## Cria o AVD ($(AVD_NAME)) se não existir
	@echo no | "$(AVDMANAGER)" create avd -n $(AVD_NAME) -k "$(SYSTEM_IMAGE)" -d $(AVD_DEVICE) --force

.PHONY: emulator
emulator: ## Inicia o emulador em background (desacoplado, render por software)
	@if "$(ADB)" get-state >/dev/null 2>&1; then \
		echo "Emulador já está rodando."; \
	else \
		echo "Iniciando emulador $(AVD_NAME)..."; \
		nohup "$(EMULATOR)" -avd $(AVD_NAME) -no-snapshot -no-boot-anim \
			-gpu swiftshader_indirect > /tmp/emulator_meus_remedios.log 2>&1 & \
		echo "Emulador iniciado (log: /tmp/emulator_meus_remedios.log)."; \
	fi

.PHONY: wait-boot
wait-boot: ## Aguarda o emulador conectar e finalizar o boot
	@"$(ADB)" wait-for-device
	@echo "Aguardando boot..."
	@until [ "$$("$(ADB)" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 3; done
	@echo "BOOT_COMPLETED"

.PHONY: ime-fix
ime-fix: ## Força o teclado virtual a aparecer mesmo com teclado físico
	@"$(ADB)" shell settings put secure show_ime_with_hard_keyboard 1
	@echo "Teclado virtual habilitado (show_ime_with_hard_keyboard=1)."

.PHONY: kill-emulator
kill-emulator: ## Encerra o emulador em execução
	@"$(ADB)" emu kill 2>/dev/null || echo "Nenhum emulador rodando."

# --- App -------------------------------------------------------------------
.PHONY: install
install: ## Instala/atualiza o APK de debug no emulador
	@test -f $(APK_DEBUG) || { echo "APK não encontrado. Rode 'make build' antes."; exit 1; }
	@"$(ADB)" install -r $(APK_DEBUG)

.PHONY: open
open: ## Abre o app (force-stop + start limpo)
	@"$(ADB)" shell am force-stop $(APP_ID)
	@"$(ADB)" shell am start -n $(MAIN_ACT) >/dev/null
	@echo "App aberto."

.PHONY: reopen
reopen: build install open ## Recompila, reinstala e reabre o app

.PHONY: uninstall
uninstall: ## Remove o app do emulador
	@"$(ADB)" uninstall $(APP_ID) 2>/dev/null || echo "App não instalado."

# --- Debug / Diagnóstico ---------------------------------------------------
.PHONY: run
run: emulator wait-boot ime-fix build install open ## Sobe emulador, builda, instala e abre o app

.PHONY: logcat
logcat: ## Mostra o logcat filtrado pelo app
	@"$(ADB)" logcat --pid=$$("$(ADB)" shell pidof -s $(APP_ID) 2>/dev/null) 2>/dev/null \
		|| "$(ADB)" logcat | grep -i $(APP_ID)

.PHONY: screenshot
screenshot: ## Captura a tela do emulador para /tmp/screen.png
	@"$(ADB)" shell screencap -p /sdcard/screen.png
	@"$(ADB)" pull /sdcard/screen.png /tmp/screen.png
	@echo "Screenshot salvo em /tmp/screen.png"

.PHONY: devices
devices: ## Lista os devices/emuladores conectados
	@"$(ADB)" devices -l
