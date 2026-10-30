# Adendo: Ativação por licença (license-activation)

> Identificador da feature: `004-license-activation`
> Data: `2026-10-30`
> Cenário: greenfield (âncora: `prd.md` + specs SDD + adendos `001-theme-engine.md`, `002-launcher-home.md` e `003-home-takeover.md`)
> Branch de entrega: `feature/004-license-activation` (commit `9a7e0a4`)

## Vigência

Vigente desde 2026-10-30.

## Resumo da entrega

A feature entregou a ativação da TV: o app mostra um código do aparelho de 12 números e só exibe o tema depois que a pessoa digita, com o controle, a senha de ativação de 12 números que o revendedor gera no notebook a partir desse código. A ativação é perpétua, funciona sem internet e também pode ser feita pela importação do ADB. Sem ativação, a tela inicial (e o Home) mostra o painel de ativação no lugar da grade. Foram concluídas 20 de 20 ações, com 94 testes automatizados passando. Na TV de referência (TCL, Android 11) o APK foi só instalado, sem testes de tela, a pedido do usuário; o código do aparelho foi lido pelo ADB e a senha entregue a ele para testar.

## Impacto por artefato da extração

| Artefato | Seção | Tipo de impacto | Delta |
|----------|-------|-----------------|-------|
| `_reversa_sdd/sdd/license-activation.md` | `#61-requisitos-principais` (RF-01, RF-02) | componente-novo | Ler o código do aparelho como 12 números `XXXX-XXXX-XXXX` (SHA-256 de `ANDROID_ID:applicationId`, 8 bytes `mod 10^12`), lido pelo ADB com `content read` de `device_code.txt` na importação, não com `cat` da pasta do app. |
| `_reversa_sdd/sdd/license-activation.md` | `#61-requisitos-principais` (RF-03 a RF-06, RF-09) | componente-novo | Não há `license.key` nem Ed25519: a licença é uma senha de 12 números, `HMAC-SHA256` de um segredo de 32 bytes (`~/.config/tematv/activation-secret`, `600`) sobre o código, embutido no APK na compilação; sem validade nem códigos de recusa `LICENSE_*`. |
| `_reversa_sdd/sdd/license-activation.md` | `#61-requisitos-principais` (RF-07, RF-08) | componente-novo | O painel de ativação fica dentro da tela inicial, com campo para a senha e "Ativar"; 5 senhas erradas seguidas bloqueiam 30 s. Ver `requirements.md` da feature (RF-05, RF-06, RF-08). |
| `_reversa_sdd/sdd/license-activation.md` | `#61-requisitos-principais` (RF-10) | componente-novo | O registro do revendedor é `~/tematv/ativacoes.csv` (data, código, cliente), gravado pelo `activation-tool senha`. |
| `_reversa_sdd/sdd/license-activation.md` | `#9-modelo-de-dados` | componente-novo | A entidade `License` não existe; a TV guarda `activation_code` e `activated_at` em `activation_prefs` e reconfere a senha contra o código atual a cada consulta (restauração de fábrica invalida). Ver `data-delta.md` da feature. |
| `_reversa_sdd/sdd/license-activation.md` | `#10-integrações-e-dependências` | componente-novo | Sem biblioteca de assinatura: `javax.crypto.Mac` basta. A compilação do app falha sem o arquivo do segredo. |
| `_reversa_sdd/sdd/license-activation.md` | `#12-segurança-e-privacidade` | componente-novo | Quem desmontar o APK pode extrair o segredo e gerar senhas (risco aceito, NG-03). O segredo nunca vai para o git, pacotes de tema ou logs; perder o arquivo impede gerar senhas novas, então precisa de cópia de segurança. |
| `_reversa_sdd/sdd/license-activation.md` | `#14-open-questions` (OQ-01, OQ-02) | componente-novo | Respondidas: ativação perpétua; sem vencimento, a carência não se aplica. |
| `_reversa_sdd/sdd/license-activation.md` | `#15-decisões-tomadas-decision-log` | componente-novo | A decisão vigente é a senha curta com segredo no APK (D-03 do `roadmap.md` da feature), trocando a licença assinada porque uma assinatura não é digitável com o controle. |
| `_reversa_sdd/sdd/home-takeover.md` | `#6-requisitos-funcionais` (RF-04, RF-09) | componente-novo | A troca não consulta mais a licença: sempre abre a tela inicial, que mostra a grade ou o painel de ativação. Sem ativação, a capa mostra só o fundo, sem o logotipo. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#6-requisitos-funcionais` (RF-07, RF-08, RF-14) | componente-novo | A instalação lê o código com `content read` de `device_code.txt`, gera a senha com o núcleo de ativação e a grava com `content write` em `activation_code.txt` seguido da recarga (contrato `_reversa_forward/004-license-activation/interfaces/ativacao-adb.md`); no kit de pendrive, a senha vai num texto para digitar. |
| `_reversa_sdd/sdd/device-provisioning.md` | `#11-edge-cases-e-tratamento-de-erros` (EC-07) | componente-novo | O erro passa a ser a falta do segredo de ativação (`activation-secret`), não da chave privada. |
| `_reversa_sdd/sdd/theme-builder.md` | `#6-requisitos-funcionais` | componente-novo | A página da feature 005 deve oferecer a geração da senha usando o mesmo núcleo e conferir contra os vetores fixos de `ActivationCodeTest` (contrato `_reversa_forward/004-license-activation/interfaces/senha-ativacao.md`). |

## Regras sob vigilância

W001 a W006, todos em "Observações" (greenfield, sem peso de regressão): ver `_reversa_forward/004-license-activation/regression-watch.md`.

## Fontes

- `_reversa_forward/004-license-activation/legacy-impact.md`
- `_reversa_forward/004-license-activation/regression-watch.md`
- `_reversa_forward/004-license-activation/requirements.md`
- `_reversa_forward/004-license-activation/roadmap.md`
- `_reversa_forward/004-license-activation/data-delta.md`
- `_reversa_forward/004-license-activation/interfaces/senha-ativacao.md`
- `_reversa_forward/004-license-activation/interfaces/ativacao-adb.md`
- `_reversa_forward/004-license-activation/actions.md`
- `_reversa_forward/004-license-activation/progress.jsonl`
