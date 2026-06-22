# ONE PAGER — Meus Remédios

> Confirmação visual de comprimidos, 100% offline, para idosos.

## Visão
App Android **100% offline** que confirma visualmente, pela câmera, **qual remédio
pré-cadastrado** é o comprimido que o usuário (idoso) está segurando, e informa o horário de
tomá-lo — reduzindo erros de medicação.

## Problema
Idosos guardam vários comprimidos juntos (porta-comprimidos manhã/tarde/noite) e confundem
qual é qual. Pílulas variam em cor, formato, tamanho e gravação (ex.: Epez, Benicar,
Anastrozol, Caltrate, Druze — ver [docs/inspiracao.jpeg](../inspiracao.jpeg)). Erro de
medicação é perigoso.

## Solução
O usuário cadastra seus remédios com fotos dos comprimidos e horários. Na tela inicial, um
botão grande abre a câmera, captura o comprimido e o app **compara apenas com as fotos
cadastradas do próprio usuário** (sem internet, sem base externa), respondendo de forma
simples: **"É o Remédio X, tomar às HH:mm"**. Em caso de dúvida, pede foto do outro lado.

## Público-alvo
Idosos e seus cuidadores. Prioridade total em acessibilidade: fontes/botões grandes, alto
contraste, linguagem simples em pt-BR, compatível com TalkBack.

## Princípios / Restrições
- 100% offline; **nenhuma** requisição de rede; banco de dados local.
- Reconhecimento on-device (hardware local): embeddings TFLite + cor + forma.
- Sem login, sem nuvem, sem anúncios nesta fase (arquitetura preparada para o futuro).
- `minSdk 24` (Android 7.0 Nougat). Privacidade: fotos em armazenamento privado do app.

## Escopo (incluído nesta versão)
Cadastro de remédios com fotos e horários • Reconhecimento visual • Lista/busca/timeline •
Relatórios de tomadas • Lembretes com alarme exato • Marcar como tomado • Configurações
(retenção 90 dias, auto-captura).

## Fora de escopo (agora)
Login/multiusuário • Sincronização/nuvem • Base de medicamentos da internet •
Anúncios/monetização • OCR de bula/receita • Diagnóstico médico ou recomendação de dose.

## Métricas de sucesso
- Taxa de acerto top-1 do reconhecimento ≥ alvo definido em testes com o conjunto cadastrado.
- Tempo do toque no botão até a resposta ≤ poucos segundos.
- Tarefas-chave concluíveis por idoso sem ajuda (teste de usabilidade).
- 0 chamadas de rede (verificável em teste/manifest).

## Riscos & mitigações
- **Pílulas muito parecidas** → 2ª foto (verso) + score combinado + limiar de ambiguidade.
- **Iluminação/fundo ruins** → guia visual de captura, pré-processo, auto-captura ao focar.
- **Falsos positivos perigosos** → quando score < limiar, NÃO afirmar; pedir nova foto /
  mostrar candidatos. Disclaimer: confirmação visual auxiliar, não substitui orientação médica.

## Roadmap por fases (mapeia PRDs e OpenSpec changes)
| Fase | Entrega | PRD |
|------|---------|-----|
| F0 | Scaffolding do projeto | — |
| F1 | Camada de dados (Room) | — |
| F2 | Cadastro de remédios | [PRD-1](prd-1-cadastro.md) |
| F3 | Consulta e relatórios | [PRD-2](prd-2-consulta-relatorios.md) |
| F4 | Reconhecimento visual | [PRD-3](prd-3-reconhecimento.md) |
| F5 | Registro de tomadas | [PRD-5](prd-5-registro-tomadas.md) |
| F6 | Lembretes | [PRD-4](prd-4-lembretes.md) |
| F7 | Configurações | [PRD-6](prd-6-configuracoes.md) |
| F8 | Acessibilidade | — |
| F9 | Testes e CI | — |
