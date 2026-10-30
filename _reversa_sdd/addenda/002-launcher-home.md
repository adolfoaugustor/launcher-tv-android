# Adendo: Tela inicial do launcher (launcher-home)

> Identificador da feature: `002-launcher-home`
> Data: `2026-10-30`
> Cenário: greenfield (âncora: `prd.md` + specs SDD + adendo `001-theme-engine.md`)
> Branch de entrega: `feature/002-launcher-home` (commit `bf13d00`)

## Vigência

Vigente desde 2026-10-30.

## Resumo da entrega

A feature entregou a tela inicial temática do launcher `br.tematv.launcher`:
- cabeçalho com logotipo, mensagem e os botões "Configurações" e "Tema";
- versículo do dia com referência, sigla e atribuição;
- grade de 5 colunas com cada app de TV dentro da moldura do tema (banner no painel, nome na faixa);
- tela "Tema" com diagnóstico.

A tela é aberta pela lista de apps do Google TV; ainda não assume o botão Home. Foram concluídas 23 de 23 ações, com 63 testes automatizados passando e teste na TV de referência (TCL, Android 11) com capturas de tela. Medidas na TCL: abertura a frio de cerca de 1,5 s, 80,7 MB de PSS e pacote de 14 MB aplicado em 570 ms.

## Impacto por artefato da extração

| Artefato | Seção | Tipo de impacto | Delta |
|----------|-------|-----------------|-------|
| `_reversa_sdd/prd.md` | `#4-escopo-in` (app launcher, tema infantil cristão, versículo do dia) | componente-novo | A tela inicial temática existe e foi verificada na TV; o versículo do dia aparece sem internet. Ainda falta assumir a tela inicial (feature 003). |
| `_reversa_sdd/prd.md` | `#5-não-objetivos-out` (modo criança) | componente-novo | Sem filtro por perfil; só uma lista fixa de utilitários técnicos do fabricante fica fora da grade (não é modo criança). |
| `_reversa_sdd/sdd/launcher-home.md` | `#6-requisitos-funcionais` (RF-01) | componente-novo | Ler como: a grade exclui também os utilitários técnicos de fabricante de uma lista fixa (na TCL: `com.tcl.guard`, `com.tcl.suspension`, `com.tcl.partnercustomizer`, `com.tcl.notereminder`, `com.tcl.dashboard`, `com.tcl.waterfall.overseas`, `com.tcl.t_solo`); a TV ao vivo sempre aparece. |
| `_reversa_sdd/sdd/launcher-home.md` | `#6-requisitos-funcionais` (RF-02) | componente-novo | A imagem do app é o banner de TV (16:9), encaixado na largura do painel sem distorcer; o ícone só entra quando o app não tem banner. |
| `_reversa_sdd/sdd/launcher-home.md` | `#6-requisitos-funcionais` (RF-10, RF-11) | componente-novo | "Configurações" e "Tema" são botões fixos no canto superior direito do cabeçalho, alcançados com o direcional para cima, e não itens no fim da grade. |
| `_reversa_sdd/sdd/launcher-home.md` | `#6-requisitos-funcionais` (RF-12) | componente-novo | O diagnóstico mostra versão, tema ativo, origem e último erro; acessibilidade, licença e "tema como tela inicial" aparecem como "disponível em versão futura" até as features 003 e 004. |
| `_reversa_sdd/sdd/launcher-home.md` | `#6-requisitos-funcionais` (novos RF-15, RF-16 do `requirements.md` da feature) | componente-novo | O launcher aparece na lista de apps do Google TV ("Launcher Temático", com banner), e a primeira imagem é a cor de fundo do tema padrão, nunca tela preta. |
| `_reversa_sdd/sdd/launcher-home.md` | `#7-requisitos-não-funcionais` | componente-novo | Metas cumpridas na TCL: abertura a frio ~1,5 s (≤ 2 s), PSS 80,7 MB (≤ 100 MB). |
| `_reversa_sdd/sdd/launcher-home.md` | `#8-design-e-interface` | componente-novo | Medidas: tela 1920×1080, densidade 320; cada item tem 130×170 dp (260×340 px), 5 por linha, cerca de 2 linhas visíveis. Grade vazia mostra "Nenhum app instalado" e mantém os botões no cabeçalho. |
| `_reversa_sdd/sdd/launcher-home.md` | `#14-open-questions` (OQ-02, categoria HOME) | componente-novo | Ainda aberta: o launcher não declara a categoria `HOME` nesta entrega; a decisão fica para a feature 003. |
| `_reversa_sdd/sdd/theme-engine.md` | `#7-requisitos-não-funcionais` (RNF-01, 3 s) | componente-novo | Medido: pacote de 14 MB aplicado em 570 ms na primeira vez e 96 ms com cache; o log registra `Tema aplicado: <id> em <N> ms`. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#6-requisitos-funcionais` (RF-06 a RF-08) | componente-novo | Logo após `adb install -r`, o `content write` da importação respondeu `Could not find provider` por alguns segundos; a instalação precisa repetir a importação até o provider responder. |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-02, RF-03) | componente-novo | A tela que a troca deve abrir é `br.tematv.launcher/.home.HomeActivity` (`launchMode="singleTask"`); o Voltar nela já é consumido. |

## Regras sob vigilância

W001 a W009, todos em "Observações" (greenfield, sem peso de regressão): ver `_reversa_forward/002-launcher-home/regression-watch.md`.

## Fontes

- `_reversa_forward/002-launcher-home/legacy-impact.md`
- `_reversa_forward/002-launcher-home/regression-watch.md`
- `_reversa_forward/002-launcher-home/requirements.md`
- `_reversa_forward/002-launcher-home/roadmap.md`
- `_reversa_forward/002-launcher-home/data-delta.md`
- `_reversa_forward/002-launcher-home/actions.md`
- `_reversa_forward/002-launcher-home/progress.jsonl`
