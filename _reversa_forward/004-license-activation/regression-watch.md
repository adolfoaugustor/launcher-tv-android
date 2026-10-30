# Regression watch: Ativação por licença (license-activation)

> Identificador: `004-license-activation`
> Data: `2026-10-30`
> Feature greenfield: não há regras 🟢 para vigiar ainda. Os itens abaixo ficam em "Observações", sem peso de regressão, até uma extração `/reversa` sobre o código novo confirmá-los como 🟢.

## Watch principal

| ID | Origem (arquivo, seção) | Regra esperada após mudança | Tipo de verificação | Sinal de violação |
|----|--------------------------|-----------------------------|---------------------|-------------------|

Nenhum item nesta rodada.

## Observações

| ID | Origem (arquivo, seção) | Regra esperada | Tipo de verificação | Sinal de violação |
|----|--------------------------|----------------|---------------------|-------------------|
| W001 | `interfaces/senha-ativacao.md` | Os vetores fixos de `ActivationCodeTest` continuam iguais | presença | Vetor alterado: senhas já entregues deixariam de funcionar |
| W002 | `requirements.md` RN-02 | O segredo de ativação nunca aparece em arquivos versionados, pacotes de tema ou logs | ausência | Valor do segredo em `git ls-files` ou no log `TemaTV` |
| W003 | `requirements.md` RN-04 | Sem ativação, Home mostra o painel de ativação, nunca a grade nem o Google | presença | Grade temática visível sem ativação |
| W004 | `requirements.md` RN-05 | 5 erros seguidos bloqueiam 30 s | presença | Tentativas ilimitadas |
| W005 | `requirements.md` RF-07 | A ativação é reconferida contra o código do aparelho atual | presença | TV continua ativada após mudança do código |
| W006 | `interfaces/ativacao-adb.md` | `device_code.txt` legível e `activation_code.txt` aceito pela importação do ADB | presença | Instalação automática sem conseguir ativar |

## Histórico de re-extrações

Nenhuma re-extração registrada.

## Arquivadas

Nenhum item arquivado.
