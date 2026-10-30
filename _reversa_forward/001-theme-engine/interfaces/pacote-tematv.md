# Interface: Pacote de tema `.tematv` v1 e pasta de temas

> Identificador da feature: `001-theme-engine`
> Tipo: arquivo
> Produtor: feature 5 (`theme-builder`) e, para os exemplos, a tarefa `:app:packSampleThemes`
> Consumidor: `:app` (motor de temas)
> Transporte até a TV: feature 6 (`device-provisioning`), por ADB ou pendrive

## Request (o que o produtor entrega)

Um arquivo ZIP com extensão `.tematv`:

```
igreja-x.tematv
├── theme.json            obrigatório, na raiz, UTF-8 (esquema em data-delta.md §1.1)
├── logo.png              referenciado por logo_image
├── frame-biblia.png      referenciado por icon_frame.image
├── fundo.webp            opcional, referenciado por background_image
└── fonte.ttf             opcional, referenciado por font_file
```

Exemplo de `theme.json`:

```json
{
  "schema_version": 1,
  "theme_id": "igreja-batista-x",
  "display_name": "Igreja Batista X",
  "colors": { "background": "#FFF6E0", "text": "#3B2A1A", "accent": "#E8A317" },
  "background_image": "fundo.webp",
  "logo_image": "logo.png",
  "message": "Bem-vindo à casa do Pai!",
  "icon_frame": {
    "image": "frame-biblia.png",
    "icon_slot":  { "x": 0.18, "y": 0.20, "w": 0.64, "h": 0.42 },
    "label_slot": { "x": 0.12, "y": 0.70, "w": 0.76, "h": 0.14 }
  },
  "verses": [
    { "reference": "Mt 19:14", "text": "Deixai vir a mim as crianças...", "version": "ACF" }
  ],
  "verses_attribution": "Almeida Corrigida Fiel, SBTB"
}
```

Para ativar, o transportador:
1. Garante que o app já rodou uma vez (cria `files/themes/`).
2. Envia o `.tematv` pela importação do ADB (`importacao-adb.md`). O `adb push` direto para `Android/data/br.tematv.launcher/files/themes/` não funciona: na TCL (Android 11) o ADB não tem permissão nessa pasta.
3. Grava o nome do arquivo (só o nome, sem caminho) em `active_theme.txt`, também pela importação do ADB.
4. Envia o broadcast de recarga (`broadcast-reload-theme.md`) ou espera a próxima abertura do app.

## Response (o que o consumidor faz)

- Pacote válido: descompacta para `theme-cache/`, aplica, guarda como último válido e publica o novo tema aos inscritos.
- Pacote inválido: não aplica, mantém o último tema válido (ou o padrão), registra o erro em `theme_state` e no log `TemaTV`.

## Erros

| Código | Quando |
|--------|--------|
| `THEME_ACTIVE_NOT_FOUND` | `active_theme.txt` aponta para arquivo inexistente |
| `THEME_TOO_LARGE` | arquivo acima de 15 MB |
| `THEME_ARCHIVE_CORRUPT` | ZIP ilegível ou truncado |
| `THEME_ARCHIVE_UNSAFE` | caminho com `..` ou absoluto, mais de 200 entradas ou mais de 40 MB descompactados |
| `THEME_MANIFEST_MISSING` | sem `theme.json` na raiz |
| `THEME_MANIFEST_INVALID` | `theme.json` não é JSON válido |
| `THEME_SCHEMA_UNSUPPORTED` | `schema_version` diferente de 1 |
| `THEME_FIELD_INVALID` | campo ausente ou fora das regras; acompanha o nome do campo |
| `THEME_ASSET_MISSING` | arquivo referenciado não existe no pacote |
| `THEME_ASSET_INVALID` | arquivo existe mas não é do tipo esperado |
| `THEME_IO_ERROR` | falha de leitura ou gravação no armazenamento |

## Idempotência

Aplicar o mesmo pacote de novo não muda nada: o cache é reaproveitado pelo SHA-256 e o tema publicado é igual ao anterior.

## Timeouts

Não há rede. O limite é de desempenho: aplicar um pacote de até 15 MB em no máximo 3 s na TCL de referência.
