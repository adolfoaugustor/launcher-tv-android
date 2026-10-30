# Adendo: Troca automática da tela inicial (home-takeover)

> Identificador da feature: `003-home-takeover`
> Data: `2026-10-30`
> Cenário: greenfield (âncora: `prd.md` + specs SDD + adendos `001-theme-engine.md` e `002-launcher-home.md`)
> Branch de entrega: `feature/003-home-takeover` (commit `c3fa165`)

## Vigência

Vigente desde 2026-10-30.

## Resumo da entrega

A feature entregou a troca automática da tela inicial: um serviço de acessibilidade abre a tela temática sempre que o launcher do Google aparece (Home, boot e saída do modo de espera), sem desativar o launcher do Google e sem ler o conteúdo das telas. Uma capa do tema cobre a tela do Google no mesmo instante. A tela "Tema" ganhou desligar (segurando OK por 3 s), religar e o diagnóstico real; a tela inicial ganhou a faixa de serviço desligado. Foram concluídas 22 de 22 ações (19 do plano e 3 da emenda T020 a T022), com 82 testes automatizados passando. Na TV de referência (TCL, Android 11): Home até a tela temática em menos de 1 s, 15 de 15 reinicializações e 6 de 6 saídas do modo de espera com a tela temática voltando sozinha.

## Impacto por artefato da extração

| Artefato | Seção | Tipo de impacto | Delta |
|----------|-------|-----------------|-------|
| `_reversa_sdd/prd.md` | `#1-problema` e `#3-métricas-de-sucesso` | componente-novo | A troca automática existe e passou num teste acelerado (21 ciclos válidos, todos ok); a meta de 30 dias reais sem chamado fica para o cliente piloto. |
| `_reversa_sdd/prd.md` | `#8-riscos` (acessibilidade desligada após atualização) | componente-novo | Mitigado com aviso na tela inicial e passo a passo em "Tema"; ainda não observado na prática. Novo risco confirmado: o gerenciador de início automático da TCL bloqueia o serviço sem `APP_AUTO_START` liberado. |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-01) | componente-novo | O serviço recebe só a troca de janela dos launchers do sistema; não usa filtro de teclas (a TCL não entrega o botão Home a serviços de acessibilidade). |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-02) | componente-novo | Ler como: ao decidir abrir, o serviço mostra na hora uma capa do tema (fundo e logotipo) por cima da tela do Google e abre a tela temática; sem a capa, o Android segura a abertura por cerca de 4 s após o Home; com ela, a tela abre em menos de 1 s (RF-14 do `requirements.md` da feature). |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-03) | componente-novo | Não há receptor de `BOOT_COMPLETED`: o serviço, ao ser conectado nos 3 primeiros minutos após o boot, abre a tela temática (medido: 0 a 7 s após o fim do boot). |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-05, RF-06) | componente-novo | Desligar exige segurar OK por 3 s, com barra de progresso; toque curto mostra "Segure OK por 3 segundos" (RF-13 do `requirements.md`). |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-07) | componente-novo | O aviso de serviço desligado é uma faixa discreta no rodapé da tela inicial; o passo a passo e o botão para as Configurações de acessibilidade ficam em "Tema". |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-09) | componente-novo | O ponto de licença existe (`LicenseGate`), mas responde sempre "válida" até a feature 004. |
| `_reversa_sdd/sdd/home-takeover.md` | `#11-edge-cases-e-tratamento-de-erros` (EC-02) | componente-novo | Eventos repetidos do mesmo Home são agrupados (1,5 s) antes da trava contra loop, para não pausar a troca por engano. |
| `_reversa_sdd/sdd/launcher-home.md` | `#14-open-questions` (OQ-02, categoria HOME) | componente-novo | Respondida: o launcher do Google tem prioridade 2 na categoria HOME e um app comum tem 0; declarar HOME não tem efeito, e o produto não declara. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#6-requisitos-funcionais` (RF-09) | componente-novo | Além de acrescentar o serviço a `enabled_accessibility_services`, a instalação precisa liberar `appops set br.tematv.launcher APP_AUTO_START allow` na TCL (contrato `_reversa_forward/003-home-takeover/interfaces/ativacao-acessibilidade-adb.md`). |
| `_reversa_sdd/sdd/device-provisioning.md` | `#11-edge-cases-e-tratamento-de-erros` | componente-novo | Em modo de espera, a TCL desliga a rede: a TV fica inacessível pelo ADB até ser ligada pelo controle. |

## Regras sob vigilância

W001 a W008, todos em "Observações" (greenfield, sem peso de regressão): ver `_reversa_forward/003-home-takeover/regression-watch.md`.

## Fontes

- `_reversa_forward/003-home-takeover/legacy-impact.md`
- `_reversa_forward/003-home-takeover/regression-watch.md`
- `_reversa_forward/003-home-takeover/requirements.md`
- `_reversa_forward/003-home-takeover/roadmap.md`
- `_reversa_forward/003-home-takeover/interfaces/ativacao-acessibilidade-adb.md`
- `_reversa_forward/003-home-takeover/actions.md`
- `_reversa_forward/003-home-takeover/progress.jsonl`
