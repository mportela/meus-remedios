# TECH-6 — NFRs, Privacidade e Offline

- **NFR-offline.1**: o app não declara nem usa a permissão `INTERNET`; nenhuma chamada de
  rede.
- **NFR-privacy.1**: fotos e dados ficam em armazenamento privado do app; sem
  compartilhamento externo; sem analytics nesta fase.
- **NFR-compat.1**: `minSdk 24`; testado nas versões alvo; tratar permissões em runtime
  (câmera, notificações Android 13+, alarme exato Android 12+).
- **NFR-perf.1**: resposta do reconhecimento em poucos segundos em hardware modesto; modelo
  leve (MobileNet) e pré-processo eficiente.
- **NFR-reliability.1**: lembretes reagendados após boot; banco com migrações versionadas.
- **NFR-safety.1**: não afirmar identidade com baixa confiança; disclaimer de uso (auxílio
  visual, não substitui orientação médica).
- **NFR-maint.1**: cobertura de testes nos use cases e no scoring; CHANGELOG atualizado por
  feature.
