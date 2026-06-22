# PRD-1 — Cadastro de Remédios

- **Fase:** F2
- **Capabilities OpenSpec:** `medication-catalog`, `medication-photos`
- **Convenção de IDs:** `RF-1.n` (funcional), `RN-1.n` (regra de negócio)

## Objetivo
Permitir cadastrar cada remédio do usuário com dados mínimos e fotos do comprimido para
posterior reconhecimento visual.

## Usuário / Contexto
Idoso ou cuidador cadastrando os remédios de uso. Deve ser simples, em etapas curtas, com
campos grandes e linguagem clara em pt-BR.

## Requisitos funcionais
- **RF-1.1** Cadastrar remédio com **nome** (obrigatório) e **dosagem/observações** (opcional).
- **RF-1.2** Definir **período de uso** (contínuo ou por período) e **data início/fim**
  (opcionais).
- **RF-1.3** Definir um ou mais **horários do dia** (HH:mm) e os **dias da semana** aplicáveis.
- **RF-1.4** Adicionar **fotos do comprimido** (frente e, opcionalmente, verso). Fotos são
  opcionais, mas recomendadas — sem foto, o remédio não participa do reconhecimento visual.
- **RF-1.5** Capturar foto pela câmera ou escolher da galeria; recortar/centralizar o
  comprimido.
- **RF-1.6** Toggle **"avise-me"** por remédio (liga/desliga lembrete individual).
- **RF-1.7** Editar e excluir remédio; ao excluir, remover as fotos associadas do
  armazenamento.
- **RF-1.8** Ao salvar, **extrair e persistir as features** da(s) foto(s): embedding, cor
  dominante (Lab) e aspect ratio — para uso offline no reconhecimento.

## Regras de negócio
- **RN-1.1** Nome é obrigatório; demais campos têm defaults sensatos.
- **RN-1.2** Fotos ficam em armazenamento **privado** do app; o caminho é referenciado no DB.
- **RN-1.3** Data fim, se informada, deve ser ≥ data início.
- **RN-1.4** Sem foto cadastrada → remédio listável/agendável, mas não reconhecível
  visualmente.

## Fluxo principal
Lista → "Adicionar remédio" → preenche nome/dosagem → define horários/período → adiciona
foto(s) → salva (extrai features) → volta à lista com o item novo.

## UI / Acessibilidade
Formulário em etapas curtas, rótulos grandes, campos com ajuda em pt-BR, botão salvar
destacado e feedback de sucesso. Captura com guia "centralize o comprimido".

## Critérios de aceite
- Cadastro com nome + horário salva e aparece na lista.
- Foto capturada gera features persistidas (verificável em teste de repositório).
- Edição/exclusão refletem no DB e no storage de imagens.

## Dependências
F1 (camada de dados), `data/ml` (extrator de features).

## Fora de escopo
Importar lista de remédios externa; leitura de bula/receita.
