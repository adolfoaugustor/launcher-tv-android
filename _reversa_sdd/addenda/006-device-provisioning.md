# Adendo: Instalação na TV (device-provisioning)

> Identificador da feature: `006-device-provisioning`
> Data: `2026-10-30`
> Cenário: greenfield (âncora: `prd.md` + specs SDD + adendos `001-theme-engine.md` a `005-theme-builder.md`)
> Branch de entrega: `feature/006-device-provisioning` (commit `eb6ee7c`)

## Vigência

Vigente desde 2026-10-30.

## Resumo da entrega

A feature entregou a aba "Instalar na TV" da ferramenta `tematv`: com o cliente e o IP, ela conecta pelo ADB, instala o launcher de produção, libera o início automático, envia o tema, ativa a TV com a senha gerada na hora, liga a troca da tela inicial e confere a tela temática depois do Home, registrando cada etapa. O app passou a publicar o próprio estado pela importação, e o APK de produção passou a ser assinado com a chave definitiva do revendedor. Foram concluídas 23 de 23 ações, com 146 testes passando; na TV de referência (TCL, Android 11) a instalação completa funcionou e a repetição levou 15 s, sem refazer nem duplicar nada. O teste na TV revelou e corrigiu um defeito da feature 004 (senha pela importação com quebra de linha descartada).

## Impacto por artefato da extração

| Artefato | Seção | Tipo de impacto | Delta |
|----------|-------|-----------------|-------|
| `_reversa_sdd/prd.md` | `#3-métricas-de-sucesso` | componente-novo | Instalar e personalizar uma TV sem comandos digitados existe; na TV de referência a reinstalação levou 15 s. A meta de menos de 10 minutos por TV ainda precisa ser medida numa TV nova de cliente. |
| `_reversa_sdd/prd.md` | `#6-restrições` e `#8-riscos` (modo desenvolvedor) | componente-novo | Confirmado: a instalação exige a depuração por rede ligada na TV; ela fica ligada depois, para trocar o tema pela mesma aba. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#61-requisitos-principais` (RF-03 a RF-05, RF-15) | componente-novo | "TV encontrada" é um teste da porta 5555 (5 s); autorização por até 120 s com reconexão em `offline`; todos os comandos com `-s <ip>:5555`. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#61-requisitos-principais` (RF-06) | componente-novo | O launcher de produção vem de `~/tematv/launcher/` (preparado por `tools/instalar-tematv.sh`); a instalação é pulada com versão e assinatura iguais; assinatura diferente vira "Desfazer e instalar de novo". |
| `_reversa_sdd/sdd/device-provisioning.md` | `#61-requisitos-principais` (RF-07, RF-08) | componente-novo | Ler como: a recarga por broadcast inicia o app sem abrir telas; o código, o tema e o `active_theme.txt` passam pela importação do ADB com repetição por até 30 s; não há `license.key`. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#61-requisitos-principais` (RF-09, RF-10) | componente-novo | Antes da acessibilidade, `appops set br.tematv.launcher APP_AUTO_START allow` (pulado onde não existe); a lista é unida por componente inteiro, gravada entre aspas simples e conferida em `dumpsys accessibility`; a conferência usa a linha `mCurrentFocus` não nula. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#61-requisitos-principais` (RF-12, RF-13) | componente-novo | O registro é uma linha JSON por etapa em `~/tematv/instalacoes/<data>-<ip>.log`, sem senha nem segredos; desfazer tira só o serviço do launcher e desinstala. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#61-requisitos-principais` (RF-14) e `#63-fluxos-alternativos` (Fluxo B) | componente-novo | Sem kit de pendrive nesta versão: sem o ADB, o tema do cliente não entra no app. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#9-modelo-de-dados` e `#14-open-questions` (OQ-01 a OQ-03) | componente-novo | `InstallRecord` real em `_reversa_forward/006-device-provisioning/data-delta.md` §2.1. OQ-01: resolvida pela importação do ADB; OQ-02: modo desenvolvedor aceito; OQ-03: depuração continua ligada. |
| `_reversa_sdd/sdd/license-activation.md` | `#61-requisitos-principais` | componente-novo | O app publica `app_status.json` (ativada, tema, versão, assinatura) só para leitura pelo ADB; a senha recebida pela importação aceita espaços e quebra de linha nas pontas. |
| `_reversa_sdd/sdd/license-activation.md` | `#11-edge-cases-e-tratamento-de-erros` (EC-06) | componente-novo | O APK de produção é assinado com a chave definitiva em `~/.config/tematv/` (fora do git); trocar a chave muda o código do aparelho e exige reativar todas as TVs, por isso a chave precisa de backup. |
| `_reversa_sdd/sdd/theme-engine.md` | `#6-requisitos-funcionais` | componente-novo | A importação do ADB aceita também `app_status.json`, somente leitura (contrato `_reversa_forward/006-device-provisioning/interfaces/estado-do-app.md`). |

## Regras sob vigilância

W001 a W007, todos em "Observações" (greenfield, sem peso de regressão): ver `_reversa_forward/006-device-provisioning/regression-watch.md`.

## Fontes

- `_reversa_forward/006-device-provisioning/legacy-impact.md`
- `_reversa_forward/006-device-provisioning/regression-watch.md`
- `_reversa_forward/006-device-provisioning/requirements.md`
- `_reversa_forward/006-device-provisioning/roadmap.md`
- `_reversa_forward/006-device-provisioning/data-delta.md`
- `_reversa_forward/006-device-provisioning/interfaces/api-instalacao.md`
- `_reversa_forward/006-device-provisioning/interfaces/estado-do-app.md`
- `_reversa_forward/006-device-provisioning/actions.md`
- `_reversa_forward/006-device-provisioning/progress.jsonl`
