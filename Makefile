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

# Câmera traseira do emulador: webcam0 = webcam do Mac; emulated = simulada.
CAMERA_BACK ?= webcam0
# Modo de GPU do emulador: host (acelerada, recomendado no Mac) ou swiftshader_indirect (software).
GPU_MODE    ?= host
# Origem padrão das fotos a enviar para a galeria (ex.: make push-photos SRC=~/Downloads).
SRC         ?= $(HOME)/Downloads
# Pasta de destino na galeria do device.
DEVICE_DIR  ?= /sdcard/Pictures

APP_ID      := com.meusremedios
MAIN_ACT    := $(APP_ID)/.ui.MainActivity
APK_DEBUG   := app/build/outputs/apk/debug/meus-remedios-1.0.0-debug.apk
APK_RELEASE := app/build/outputs/apk/release/meus-remedios-1.0.0-release.apk

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

.PHONY: release
release: ## Compila o APK de release (minificado)
	$(GRADLE) assembleRelease

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
emulator: ## Inicia o emulador em background (desacoplado, GPU acelerada)
	@if "$(ADB)" get-state >/dev/null 2>&1; then \
		echo "Emulador já está rodando."; \
	else \
		echo "Iniciando emulador $(AVD_NAME) (gpu $(GPU_MODE))..."; \
		nohup "$(EMULATOR)" -avd $(AVD_NAME) -no-snapshot -no-boot-anim \
			-gpu $(GPU_MODE) > /tmp/emulator_meus_remedios.log 2>&1 & \
		echo "Emulador iniciado (log: /tmp/emulator_meus_remedios.log)."; \
	fi

.PHONY: emulator-cam
emulator-cam: ## Inicia o emulador usando a webcam do Mac como câmera traseira
	@if "$(ADB)" get-state >/dev/null 2>&1; then \
		echo "Emulador já está rodando. Use 'make kill-emulator' antes para aplicar a câmera."; \
	else \
		echo "Iniciando emulador $(AVD_NAME) com câmera traseira '$(CAMERA_BACK)' (gpu $(GPU_MODE))..."; \
		nohup "$(EMULATOR)" -avd $(AVD_NAME) -no-snapshot-load -no-boot-anim \
			-camera-back $(CAMERA_BACK) -gpu $(GPU_MODE) > /tmp/emulator_meus_remedios.log 2>&1 & \
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

.PHONY: keyboard-enable
keyboard-enable: ## Habilita teclado físico do Mac no AVD (hw.keyboard=yes) — requer kill-emulator antes
	@CONFIG="$$HOME/.android/avd/$(AVD_NAME).avd/config.ini"; \
	if [ ! -f "$$CONFIG" ]; then echo "AVD '$(AVD_NAME)' não encontrado."; exit 1; fi; \
	python3 -c "path='$$CONFIG'; f=open(path,'rb'); c=f.read(); f.close(); f=open(path,'wb'); f.write(c.replace(b'hw.keyboard=no',b'hw.keyboard=yes')); f.close()"; \
	echo "hw.keyboard=yes aplicado em $$CONFIG."

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

.PHONY: run-cam
run-cam: emulator-cam wait-boot ime-fix build install open ## Igual ao 'run', mas com a webcam do Mac como câmera

# --- Galeria / Fotos -------------------------------------------------------
.PHONY: push-photo
push-photo: ## Envia uma foto para a galeria (uso: make push-photo FILE=~/Downloads/foto.jpg)
	@test -n "$(FILE)" || { echo "Informe o arquivo: make push-photo FILE=~/Downloads/foto.jpg"; exit 1; }
	@test -f "$(FILE)" || { echo "Arquivo não encontrado: $(FILE)"; exit 1; }
	@"$(ADB)" shell mkdir -p $(DEVICE_DIR)
	@"$(ADB)" push "$(FILE)" $(DEVICE_DIR)/
	@$(MAKE) --no-print-directory scan-media
	@echo "Foto enviada para $(DEVICE_DIR) e galeria reindexada."

.PHONY: push-photos
push-photos: ## Envia todas as imagens de uma pasta (uso: make push-photos SRC=~/Downloads)
	@test -d "$(SRC)" || { echo "Pasta não encontrada: $(SRC)"; exit 1; }
	@"$(ADB)" shell mkdir -p $(DEVICE_DIR)
	@found=0; \
	for f in "$(SRC)"/*.jpg "$(SRC)"/*.jpeg "$(SRC)"/*.png "$(SRC)"/*.webp; do \
		[ -e "$$f" ] || continue; \
		"$(ADB)" push "$$f" $(DEVICE_DIR)/ && found=1; \
	done; \
	if [ "$$found" = "1" ]; then \
		$(MAKE) --no-print-directory scan-media; \
		echo "Imagens de '$(SRC)' enviadas para $(DEVICE_DIR) e galeria reindexada."; \
	else \
		echo "Nenhuma imagem (jpg/jpeg/png/webp) encontrada em '$(SRC)'."; \
	fi

.PHONY: scan-media
scan-media: ## Força o Android a reindexar a galeria ($(DEVICE_DIR))
	@"$(ADB)" shell content call --uri content://media/external/file \
		--method scan_volume --arg external_primary >/dev/null 2>&1 \
		|| "$(ADB)" shell am broadcast \
			-a android.intent.action.MEDIA_SCANNER_SCAN_FILE \
			-d file://$(DEVICE_DIR) >/dev/null 2>&1
	@echo "Galeria reindexada."

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
