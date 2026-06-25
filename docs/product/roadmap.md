# Roadmap — Meus Remédios (pós-MVP)

> **Estado em 2026-06-25:** MVP completo e estável (**v1.1.0**, F0–F12 entregues, 181 testes,
> **zero dívida técnica aberta**). O produto funciona ponta a ponta, mas ainda **não foi
> publicado**. Este documento organiza o futuro do produto. **A priorização do backlog será
> feita depois** — aqui apenas registramos a direção e o inventário de possibilidades.
>
> Histórico de implementação (fases F0–F12) em [openspec-plan.md](../openspec-plan.md).

## Definições estratégicas (decididas — base de todo o roadmap)
1. **Modelo: 100% gratuito e open-source.** Sem monetização, sem anúncios, sem tiers pagos.
   Foco em **alcance e impacto social**. → **Ação:** registrar essa natureza (gratuito +
   código aberto) no **disclaimer/README do projeto**.
2. **Offline é inegociável — sem nuvem, sem cuidador remoto.** O produto **se autoprotege**
   por design: aprende **apenas os comprimidos cadastrados pelo próprio usuário** e, no
   cadastro, **valida e bloqueia/avisa casos que gerariam conflito por semelhança**
   (nomes duplicados — F10; fotos visualmente colidentes — F11), pedindo nova foto e
   explicando o problema. Não há necessidade de sincronização externa para "segurança".
3. **Mercado: B2C consumidor** (idoso + cuidador familiar), distribuição direta via Play Store.
4. **Geografia: Brasil / pt-BR.** Sem internacionalização nesta fase.

## Limitação técnica de raiz (o grande TODO de qualidade)
O embedding usa **MobileNetV3-Small genérico (ImageNet)**, que **não discrimina comprimidos
visualmente idênticos** (mesma cor/forma, sem inscrição distinta). A autoproteção do cadastro
(item 2) mitiga o risco avisando colisões, mas a **solução definitiva** é **fine-tuning do
modelo com fotos reais de comprimidos** (dataset rotulado pt-BR, mantendo o pipeline offline).
Continua sendo o principal investimento de qualidade do reconhecimento.

## Inventário do backlog futuro (sem ordem de prioridade ainda)

> A priorização (RICE/sequenciamento) fica para uma rodada posterior. Itens listados por
> tema apenas para organização.

**Portabilidade de dados (100% offline)**
- **Exportar / importar dados do app** — backup local completo (medicamentos, fotos/features,
  agenda, histórico, settings) para um arquivo que o usuário guarda e **reimporta em outro
  celular**. Permite **migração de aparelho** e backup pessoal **sem nuvem**, preservando a
  promessa offline. Considerar criptografia/integridade do arquivo e compatibilidade de
  schema entre versões.

**Lançamento e onboarding**
- Publicação na Play Store (keystore release, ficha ASO pt-BR, screenshots acessíveis,
  política de privacidade, **disclaimer "não é dispositivo médico"** + natureza gratuita/aberta).
- Onboarding guiado no primeiro uso.
- Teste de reconhecimento logo após o cadastro ("Vamos testar? Aponte para o comprimido").
- Feedback de qualidade da foto no cadastro (luz/foco/enquadramento) — melhora reconhecimento
  sem ML adicional.

**Qualidade do reconhecimento**
- **Fine-tuning do modelo de comprimidos** (resolve a limitação de raiz acima).

**Utilidade diária e retenção**
- Controle de estoque + alerta "acabando em X dias".
- Multi-perfil **local** (vários idosos no mesmo device/tablet do cuidador — sem nuvem).
- Leitura por voz (TTS) do resultado e dos lembretes.
- Calendário / streak de adesão (motivação).
- Lembrete persistente / snooze avançado.

**Acessibilidade aprofundada**
- Modo voz-first para baixa visão severa.

**Comunidade**
- Governança open-source (contribuição, licença, CONTRIBUTING) — alinhado ao modelo aberto.

## O que está fora de escopo (decidido)
- Nuvem, backup remoto, sincronização ou cuidador remoto (viola offline — item 2).
- Login, contas, monetização, anúncios, tiers pagos (item 1).
- B2B / institucional e internacionalização (itens 3 e 4).

## Próximo passo
Quando quisermos avançar: **priorizar este inventário** (definir o que entra primeiro) e só
então criar as changes OpenSpec correspondentes, uma por feature priorizada.
