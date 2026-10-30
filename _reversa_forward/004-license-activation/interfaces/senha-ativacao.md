# Interface: Algoritmo do código do aparelho e da senha de ativação

> Identificador da feature: `004-license-activation`
> Tipo: algoritmo compartilhado
> Implementação de referência: `launcher/android/activation-core/` (Kotlin)
> Consumidores: app na TV, `activation-tool` no notebook, página da feature 005

## Código do aparelho

```
entrada:  androidId (texto), applicationId = "br.tematv.launcher"
hash   = SHA-256( UTF-8( androidId + ":" + applicationId ) )
numero = primeiros 8 bytes de hash, big-endian, como inteiro sem sinal de 64 bits
codigo = numero mod 10^12, com zeros à esquerda até 12 dígitos
exibido como XXXX-XXXX-XXXX
```

## Senha de ativação

```
entrada:  segredo (32 bytes, guardado em hexadecimal), codigo (12 dígitos, sem traços)
mac    = HMAC-SHA256( chave = segredo, mensagem = UTF-8(codigo) )
numero = primeiros 8 bytes de mac, big-endian, sem sinal
senha  = numero mod 10^12, com zeros à esquerda até 12 dígitos
exibida como XXXX-XXXX-XXXX
```

Entrada do usuário: traços, espaços e outros separadores são ignorados; precisam sobrar exatamente 12 dígitos.

## Vetores de teste

Os vetores fixos ficam em `launcher/android/activation-core/src/test/kotlin/br/tematv/activation/ActivationCodeTest.kt` e devem ser reproduzidos por qualquer outra implementação.

## Erros

- Código ou senha sem exatamente 12 dígitos depois de limpar os separadores: entrada inválida.
- Segredo que não seja 64 caracteres hexadecimais: segredo inválido (falha imediata na ferramenta e na compilação).

## Idempotência

O mesmo segredo e o mesmo código sempre geram a mesma senha.

## Segurança

O segredo nunca sai do notebook do revendedor, exceto embutido no APK (risco aceito: NG-03 da spec).
