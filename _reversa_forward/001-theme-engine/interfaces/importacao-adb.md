# Interface: Importação de temas pelo ADB

> Identificador da feature: `001-theme-engine`
> Tipo: ContentProvider Android (escrita de arquivo)
> Produtor: o instalador no notebook, pelo ADB (feature 6 e testes manuais)
> Consumidor: `ThemeImportProvider` no `:app`
> Motivo: na TCL com Android 11, o ADB não tem permissão para gravar em `Android/data/br.tematv.launcher/` (roadmap, premissa 🔴 e D-15)

## Request

```
URI:        content://br.tematv.launcher.themes/<nome>
Nomes:      <kebab-case>.tematv   (regex ^[a-z0-9-]+\.tematv$)
            active_theme.txt
Operação:   abrir para escrita (content write); o conteúdo vem da entrada padrão
Quem pode:  uid 2000 (shell do ADB) ou 0 (root)
```

Pelo ADB:

```bash
I=content://br.tematv.launcher.themes
adb -s <ip>:5555 shell "content write --uri $I/igreja-x.tematv" < igreja-x.tematv
echo igreja-x.tematv | adb -s <ip>:5555 shell "content write --uri $I/active_theme.txt"
adb -s <ip>:5555 shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver
```

A leitura (`content read`) também é aceita, para conferência pelo mesmo usuário do ADB.

## Response

- O arquivo é criado ou substituído (truncado) em `files/themes/<nome>` na pasta externa do app.
- A gravação não aplica o tema sozinha: o instalador envia o broadcast de recarga (`broadcast-reload-theme.md`) depois de gravar o pacote e o `active_theme.txt`.

## Erros

| Situação | Resultado para quem chama |
|----------|---------------------------|
| Chamador que não é o shell nem root | `SecurityException`; nada é gravado |
| Nome fora do padrão, com `/` ou com mais de um segmento | `FileNotFoundException` com o nome recusado; nada é gravado |
| Armazenamento externo do app indisponível | `FileNotFoundException`; nada é gravado |
| `query`, `insert`, `update`, `delete` | `UnsupportedOperationException` |

## Idempotência

Gravar o mesmo arquivo de novo substitui o anterior. O cache do tema é reaproveitado se o conteúdo (SHA-256) não mudou.

## Segurança

O provider é exportado porque o shell do ADB precisa alcançá-lo, mas toda operação confere o uid do chamador antes de abrir qualquer arquivo. Outros apps da TV recebem erro de segurança. O provider não aceita caminhos, só nomes da lista acima.

## Timeouts

Não há timeout próprio: a gravação dura o tempo de transferir o arquivo pelo ADB (pacote de até 15 MB).
