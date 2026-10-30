# Adendo: Motor de temas (theme-engine)

> Identificador da feature: `001-theme-engine`
> Data: `2026-10-30`
> Cenário: greenfield (âncora: `prd.md` + specs SDD)
> Branch de entrega: `feature/001-theme-engine` (commit `44d325c`)

## Vigência

Vigente desde 2026-10-30.

## Resumo da entrega

A feature entregou o motor de temas do launcher: o formato do pacote `.tematv` (esquema `theme.json` v1) e a parte do app Android TV `br.tematv.launcher` que encontra, valida e aplica o pacote, com tema padrão embutido e a regra do "último tema válido". Também entregou a importação de pacotes pelo ADB, necessária porque a TV de referência não deixa o ADB gravar na pasta do app. Foram concluídas 33 de 33 ações, com 50 testes automatizados passando e 9 de 9 verificações na TV de referência (TCL, Android 11). Ainda não há tela inicial: ela chega na feature 002.

## Impacto por artefato da extração

| Artefato | Seção | Tipo de impacto | Delta |
|----------|-------|-----------------|-------|
| `_reversa_sdd/prd.md` | `#4-escopo-in` (motor de temas, personalização por cliente, versículo do dia) | componente-novo | O motor de temas e o formato `.tematv` existem e foram verificados na TV; o versículo do dia já vem embutido no pacote, mas só aparece na tela a partir da feature 002. |
| `_reversa_sdd/prd.md` | `#6-restrições` (técnica) | componente-novo | Versão mínima adotada: Android TV 9 (API 28); o app compila com Kotlin, JDK 17 e Gradle 8.7. |
| `_reversa_sdd/prd.md` | `#8-riscos` (instalação exige modo desenvolvedor) | componente-novo | Confirmado na prática: o caminho por ADB exige a depuração ligada na TV; além disso, o ADB não grava em `Android/data/<pacote>/` na TCL, e os temas entram pela importação descrita em `_reversa_forward/001-theme-engine/interfaces/importacao-adb.md`. |
| `_reversa_sdd/sdd/theme-engine.md` | `#6-requisitos-funcionais` | componente-novo | RF-01 a RF-09 implementados como especificado; somam-se o RF-12 (proporção da moldura exposta) e o RF-13 (importação pelo ADB) do `requirements.md` da feature. |
| `_reversa_sdd/sdd/theme-engine.md` | `#9-modelo-de-dados` | componente-novo | O esquema v1 vale como especificado; as regras exatas de cada campo (tamanhos, `theme_id` em kebab-case de 3 a 40 caracteres, versículo de até 600 caracteres, sigla de 2 a 10) estão em `_reversa_forward/001-theme-engine/data-delta.md` §1.1. |
| `_reversa_sdd/sdd/theme-engine.md` | `#7-requisitos-não-funcionais` | componente-novo | Além dos 15 MB por pacote, o extrator recusa mais de 200 entradas e mais de 40 MB descompactados (`THEME_ARCHIVE_UNSAFE`). |
| `_reversa_sdd/sdd/theme-engine.md` | `#11-edge-cases-e-tratamento-de-erros` | componente-novo | Os códigos de erro reais incluem também `THEME_MANIFEST_INVALID` (JSON malformado) e `THEME_ASSET_INVALID` (arquivo do tipo errado); pacote ativo inexistente e falha de leitura voltam ao tema padrão, pacote recusado mantém o último válido. |
| `_reversa_sdd/sdd/theme-engine.md` | `#14-open-questions` (OQ-01, OQ-02) | componente-novo | Resolvidas: moldura em retrato com 260×340 px de referência (5 apps por linha em 1080p); mensagem de até 120 caracteres em no máximo 2 linhas. |
| `_reversa_sdd/sdd/launcher-home.md` | `#6-requisitos-funcionais` (RF-02, RF-06, RF-07, RF-12) | componente-novo | A tela da feature 002 consome `ThemeRepository.activeTheme` (`StateFlow<ActiveTheme?>`, nulo até a primeira leitura), decodifica imagens com `ThemeImageLoader` e lê o último erro em `ThemeRepository.lastError`. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#6-requisitos-funcionais` (RF-07, RF-08) | componente-novo | Ler como: o tema e o `active_theme.txt` **não** são copiados para a pasta do app; são enviados por `adb shell "content write --uri content://br.tematv.launcher.themes/<nome>" < arquivo`, e a recarga usa `am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver`. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#14-open-questions` (OQ-01) | componente-novo | Respondida para o Android 11 da TCL: o ADB não grava na pasta externa do app (shell sem o grupo `ext_data_rw`); a importação pelo ADB resolve sem depender da versão do Android. |
| `_reversa_sdd/sdd/theme-builder.md` | `#6-requisitos-funcionais` (RF-10) | componente-novo | O formato que a ferramenta deve gerar está fixado em `_reversa_forward/001-theme-engine/interfaces/pacote-tematv.md`; nomes de pacote precisam seguir `^[a-z0-9-]+\.tematv$` para passar pela importação. |

## Regras sob vigilância

W001, W002, W003, W004, W005, W006, W007, W008, W009 e W010, todos em "Observações" (greenfield, sem peso de regressão): ver `_reversa_forward/001-theme-engine/regression-watch.md`.

## Fontes

- `_reversa_forward/001-theme-engine/legacy-impact.md`
- `_reversa_forward/001-theme-engine/regression-watch.md`
- `_reversa_forward/001-theme-engine/requirements.md`
- `_reversa_forward/001-theme-engine/roadmap.md`
- `_reversa_forward/001-theme-engine/data-delta.md`
- `_reversa_forward/001-theme-engine/interfaces/pacote-tematv.md`
- `_reversa_forward/001-theme-engine/interfaces/broadcast-reload-theme.md`
- `_reversa_forward/001-theme-engine/interfaces/importacao-adb.md`
- `_reversa_forward/001-theme-engine/progress.jsonl`

## Atualização 2026-10-30

| Artefato | Seção | Tipo de impacto | Delta |
|----------|-------|-----------------|-------|
| `_reversa_sdd/sdd/theme-builder.md` | `#6-requisitos-funcionais` (RF-05 a RF-08) e `#14-open-questions` (OQ-01, OQ-03) | componente-novo | A chave da Bíblia API não pôde ser obtida. O tema padrão e o exemplo bíblico passaram a trazer uma lista pronta de 20 versículos da NVI (emenda E001 da feature 001), com a atribuição "Bíblia Sagrada, Nova Versão Internacional®, NVI® © 1993, 2000 Biblica, Inc.® Usado com permissão." Até nova decisão, a ferramenta do notebook deve partir dessa lista em vez da API. |
| `_reversa_sdd/prd.md` | `#7-dependências-externas` (BIBLIAAPI) | componente-novo | A dependência da Bíblia API não está ativa; o texto bíblico vem da lista pronta. A Biblica permite citar até 500 versículos com atribuição; confirmar antes de vender em escala. |

Fonte: `_reversa_forward/001-theme-engine/actions.md` (seção Emendas, E001).
