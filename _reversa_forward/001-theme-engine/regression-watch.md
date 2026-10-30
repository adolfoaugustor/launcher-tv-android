# Regression watch: Motor de temas (theme-engine)

> Identificador: `001-theme-engine`
> Data: `2026-10-29`
> Feature greenfield: não há regras 🟢 para vigiar ainda. Os itens abaixo ficam em "Observações", sem peso de regressão, até uma extração `/reversa` sobre o código novo confirmá-los como 🟢.

## Watch principal

| ID | Origem (arquivo, seção) | Regra esperada após mudança | Tipo de verificação | Sinal de violação |
|----|--------------------------|-----------------------------|---------------------|-------------------|

Nenhum item nesta rodada.

## Observações

| ID | Origem (arquivo, seção) | Regra esperada | Tipo de verificação | Sinal de violação |
|----|--------------------------|----------------|---------------------|-------------------|
| W001 | `requirements.md` RF-01, RF-02, RF-03 | Pacote `.tematv` com `theme.json` v1 é aceito; campo inválido é recusado com `THEME_FIELD_INVALID` e o nome do campo; `schema_version` ≠ 1 é recusado com `THEME_SCHEMA_UNSUPPORTED` | presença | Pacote inválido aplicado, ou recusa sem código ou sem campo |
| W002 | `requirements.md` RF-05, RN-02 | Sem pacote ativo, pacote ativo inexistente ou falha de leitura: vale o tema padrão "Deus te abençoe!" com moldura de Bíblia | presença | Tela sem tema ou tema diferente do padrão nesses casos |
| W003 | `requirements.md` RF-06, RN-02 | Pacote recusado mantém o último tema válido aplicado | presença | Pacote recusado troca o tema ou cai no padrão havendo um válido anterior |
| W004 | `requirements.md` §6, segurança | Entradas com `..` ou caminho absoluto são recusadas sem gravar fora da pasta do app; limites de 200 entradas e 40 MB descompactados | presença | Arquivo gravado fora de `theme-cache/` ou pacote fora dos limites aceito |
| W005 | `requirements.md` §6, tamanho | Pacote acima de 15 MB recusado com `THEME_TOO_LARGE` | presença | Pacote grande aplicado |
| W006 | `requirements.md` RF-08 | Inscritos em `activeTheme` recebem o novo tema só após troca bem-sucedida | presença | Aviso de troca após recusa |
| W007 | `requirements.md` RF-11, RF-12 | O mesmo APK aplica a moldura de Bíblia e a de hambúrguer; a moldura expõe a proporção reduzida (13:17 para 260×340) | presença | Moldura diferente exige mudar o código |
| W008 | `interfaces/broadcast-reload-theme.md` | `am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver` recarrega o tema | presença | Broadcast ignorado após a instalação |
| W010 | `requirements.md` RF-13; `interfaces/importacao-adb.md` | `content write --uri content://br.tematv.launcher.themes/<nome>` pelo ADB grava pacotes `<kebab-case>.tematv` e o `active_theme.txt`; outros apps recebem `SecurityException`; nomes fora da lista são recusados | presença | Gravação por outro app, nome com `/` aceito, ou importação pelo ADB falhando |
| W009 | `SafeZipExtractor.kt` (observação de plataforma) | Em Android 14+ com targetSdk 34, o `ZipFile` da plataforma lança exceção em entradas com `..` antes do extrator; nessas TVs a recusa aparece como `THEME_ARCHIVE_CORRUPT`, não `THEME_ARCHIVE_UNSAFE`. O pacote continua recusado | confidência | Pacote com `..` aplicado em qualquer versão |

## Histórico de re-extrações

Nenhuma re-extração registrada.

## Arquivadas

Nenhum item arquivado.
