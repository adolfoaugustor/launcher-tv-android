# Investigation: Ativação por licença (license-activation)

> Identificador: `004-license-activation`
> Data: `2026-10-30`

## 1. Pesquisa de fundo

- 🟢 **`Settings.Secure.ANDROID_ID` (Android 8+):** valor de 64 bits por combinação de app (chave de assinatura), usuário e aparelho. Não muda ao reiniciar nem ao reinstalar com a mesma assinatura; muda na restauração de fábrica. Não exige permissão.
- 🟢 **HMAC-SHA256:** disponível em `javax.crypto.Mac` no Android desde as primeiras versões e na JVM; nenhuma biblioteca extra.
- 🟢 **Tamanho de assinatura digital:** Ed25519 e ECDSA P-256 produzem 64 a 72 bytes; em números, mais de 150 dígitos, impossível de digitar no controle. Por isso a senha curta usa código de autenticação com segredo compartilhado.
- 🟢 **Espaço de 12 dígitos:** 10^12 combinações; com espera de 30 s a cada 5 erros, tentar ao acaso pelo controle levaria milhares de anos.
- 🟢 **Redução `mod 10^12`:** os primeiros 8 bytes do HMAC formam um número de 64 bits; o viés da redução módulo 10^12 é desprezível (10^12 é muito menor que 2^64).

## 2. Alternativas avaliadas

| Tema | Alternativa | Por que foi descartada |
|------|-------------|------------------------|
| Prova de licença | Arquivo assinado (Ed25519/ECDSA) | O usuário quer ativar na hora digitando uma senha |
| Prova de licença | Servidor online | Descartado na spec (sem servidor no MVP) |
| Código do aparelho | 16 caracteres hexadecimais | Letras e números misturados são ruins de ditar por telefone |
| Ferramenta do notebook | Script Python separado | Duplicaria o algoritmo; a JVM já está instalada |
| Painel de ativação | Atividade própria | A troca e o Home já abrem a `HomeActivity`; trocar o conteúdo dela é mais simples |

## 3. Padrões aplicáveis

- **Núcleo compartilhado:** um único módulo define o algoritmo; app e ferramenta só o chamam.
- **Vetores de teste fixos:** garantem que mudanças futuras não quebrem senhas já entregues.
- **Comparação em tempo constante:** `MessageDigest.isEqual` para não vazar acertos parciais.

## 4. Fontes externas

- 🟢 `Settings.Secure.ANDROID_ID`: https://developer.android.com/reference/android/provider/Settings.Secure#ANDROID_ID
- 🟢 HMAC (RFC 2104): https://www.rfc-editor.org/rfc/rfc2104
- 🟢 Gradle `application` plugin: https://docs.gradle.org/current/userguide/application_plugin.html
