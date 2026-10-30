# Adendo: Ferramenta de preparo de tema (theme-builder)

> Identificador da feature: `005-theme-builder`
> Data: `2026-10-30`
> Cenário: greenfield (âncora: `prd.md` + specs SDD + adendos `001-theme-engine.md` a `004-license-activation.md`)
> Branch de entrega: `feature/005-theme-builder` (commit `abc6abb`)

## Vigência

Vigente desde 2026-10-30.

## Resumo da entrega

A feature entregou a ferramenta do notebook do revendedor: o comando `tematv` abre uma página local onde ele cadastra clientes de igreja ou de loja, envia logotipo, fundo e moldura, escolhe cores, escreve a mensagem e os versículos NVI (ou as mensagens da loja), confere a prévia da tela da TV e gera o pacote `.tematv`, salvo só depois de aceito pelo mesmo código que o launcher usa. A página também gera a senha de ativação. Foram concluídas 22 de 22 ações, com 121 testes automatizados passando no projeto (27 da ferramenta) e a página verificada no navegador do Windows pelo WSL2. Nada foi enviado à TV.

## Impacto por artefato da extração

| Artefato | Seção | Tipo de impacto | Delta |
|----------|-------|-----------------|-------|
| `_reversa_sdd/prd.md` | `#4-escopo-in` | componente-novo | O preparo do tema sem programar existe; além de igrejas, a ferramenta já atende lojas (moldura de hambúrguer e mensagens da loja no lugar dos versículos). |
| `_reversa_sdd/prd.md` | `#7-dependências-externas` | componente-novo | A Bíblia API não é usada: os textos bíblicos vêm da lista NVI do tema padrão do app, copiada na compilação. |
| `_reversa_sdd/sdd/theme-builder.md` | `#61-requisitos-principais` (RF-01) | componente-novo | `tematv` sobe em `127.0.0.1` (8787 a 8797) com socket só IPv4, necessário para o navegador do Windows alcançar o WSL2; recusa `Host`, `Origin` e `Content-Type` estranhos para barrar outros sites no mesmo navegador. |
| `_reversa_sdd/sdd/theme-builder.md` | `#61-requisitos-principais` (RF-02, RF-05 a RF-08, RF-13, RF-14) | componente-novo | Não há chave nem tela de configuração da API, escolha de versão nem confirmação de licença. Igreja: lista inicial dos 20 versículos NVI, itens digitados à mão também NVI, atribuição fixa da NVI. Loja: mensagens com versão `LOJA` e atribuição editável. |
| `_reversa_sdd/sdd/theme-builder.md` | `#61-requisitos-principais` (RF-03, RF-04) | componente-novo | Cada cliente tem tipo `igreja` ou `loja`, escolhido ao criar; `theme_id` fixo depois de criado; molduras prontas Bíblia e Hambúrguer. |
| `_reversa_sdd/sdd/theme-builder.md` | `#61-requisitos-principais` (RF-10) | componente-novo | O pacote é montado num temporário e validado com `ThemePackageLoader` do `:theme-core`; recusado, nada é salvo e a página aponta o campo. |
| `_reversa_sdd/sdd/theme-builder.md` | `#7-requisitos-não-funcionais` (RNF-04, RNF-05) | componente-novo | PNG e JPEG acima de 1920×1080 são reduzidos; WEBP só até 1920×1080. Instalação por `tools/instalar-tematv.sh` (exige JDK 17 e o projeto Gradle com o SDK Android configurado). |
| `_reversa_sdd/sdd/theme-builder.md` | `#9-modelo-de-dados` | componente-novo | `ToolConfig` não existe; o `ClientProject` real está em `_reversa_forward/005-theme-builder/data-delta.md` §1.1 (com `type`, sem `bible_version`, `license_confirmed` nem `source`). |
| `_reversa_sdd/sdd/theme-builder.md` | `#10-integrações-e-dependências` | componente-novo | Integrações reais: `:theme-core` (validação), `:activation-core` (senha) e o arquivo do segredo do revendedor; sem rede externa. Contrato da página: `_reversa_forward/005-theme-builder/interfaces/api-local.md`. |
| `_reversa_sdd/sdd/theme-builder.md` | `#14-open-questions` (OQ-02, OQ-03) | componente-novo | Sem objeto: não há Bíblia API; a lista padrão são os 20 versículos NVI. OQ-01 continua aberta para o uso comercial da NVI em escala. |
| `_reversa_sdd/sdd/license-activation.md` | `#61-requisitos-principais` (RF-09, RF-10) | componente-novo | A senha de ativação também é gerada pela aba "Ativar TV", registrando em `~/tematv/ativacoes.csv` no mesmo formato do `activation-tool`. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#61-requisitos-principais` (RF-06 a RF-08, RF-14) | componente-novo | A aba "Instalar na TV" pode reaproveitar o servidor local do `:theme-builder` e os pacotes de `~/tematv/temas/`; até lá, a página mostra os comandos do ADB para enviar o pacote. Sem o segredo, a compilação do app falha só na etapa do app (`preBuild`), não a das ferramentas. |

## Regras sob vigilância

W001 a W007, todos em "Observações" (greenfield, sem peso de regressão): ver `_reversa_forward/005-theme-builder/regression-watch.md`.

## Fontes

- `_reversa_forward/005-theme-builder/legacy-impact.md`
- `_reversa_forward/005-theme-builder/regression-watch.md`
- `_reversa_forward/005-theme-builder/requirements.md`
- `_reversa_forward/005-theme-builder/roadmap.md`
- `_reversa_forward/005-theme-builder/data-delta.md`
- `_reversa_forward/005-theme-builder/interfaces/api-local.md`
- `_reversa_forward/005-theme-builder/actions.md`
- `_reversa_forward/005-theme-builder/progress.jsonl`
