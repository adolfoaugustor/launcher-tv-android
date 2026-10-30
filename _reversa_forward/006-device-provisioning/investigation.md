# Investigation: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`

## 1. O que já existe

| Peça | Onde | Uso |
|------|------|-----|
| Importação pelo ADB (`content read/write`) | `app/.../theme/ThemeImportNames.kt`, provider da feature 001 | Tema, `active_theme.txt`, `device_code.txt`, `activation_code.txt`, e agora `app_status.json` |
| Recarga (`RELOAD_THEME`) | `ReloadThemeReceiver` → `TemaTvApplication.reloadImports()` | Inicia o app sem abrir tela; publica o código, importa a senha e o tema |
| Logs `TemaTV` | `Tema aplicado: <id>`, `Ativação: aceita` | Só diagnóstico; a conferência usa o estado (D-04) |
| Senha | `ResellerActivation` (feature 005) | Mesmo cálculo e registro |
| Servidor local e guarda | `LocalServer`, `LocalRequestGuard` | Novas rotas |
| `adb` no notebook | `/usr/bin/adb` 34.0.4 | Comandos |
| `apksigner` | `~/android-sdk/build-tools/34.0.0/apksigner` | Assinatura do APK no `launcher.json` |

## 2. Lições das features anteriores aplicadas

- **001:** o ADB não grava em `Android/data/<pacote>/` na TCL; tudo passa pela importação.
- **002/004:** logo após `adb install -r`, a importação pode não responder por alguns segundos: repetir.
- **003:** a TCL bloqueia o serviço sem `APP_AUTO_START`; `dumpsys window` traz várias linhas `mCurrentFocus`, usar a não nula; em modo de espera a TV some da rede.
- **005:** a falta de um arquivo secreto não pode quebrar a configuração do Gradle inteira.

## 3. Por que um arquivo de estado em vez do log

`logcat -d` devolve linhas antigas: numa reinstalação, "Ativação: aceita" de uma execução anterior faria a conferência passar mesmo com a senha nova recusada. Filtrar por horário funciona, mas depende do relógio da TV. O estado publicado pelo próprio app responde exatamente às perguntas da instalação (ativada? qual tema? qual versão e assinatura?) e reaproveita o mesmo caminho de leitura do `device_code.txt`.

## 4. Assinatura

Com `minSdk 28`, o AGP assina só com o esquema v2; `keytool -printcert -jarfile` não lê o certificado. O `apksigner verify --print-certs` lê e imprime o SHA-256. No app, `PackageManager.GET_SIGNING_CERTIFICATES` (API 28+) dá o mesmo certificado; o SHA-256 de `Signature.toByteArray()` coincide com o do `apksigner`.

## 5. Aspas no shell da TV

`adb shell settings put secure enabled_accessibility_services <lista>` junta os argumentos e o `sh` da TV os interpreta de novo. Um componente como `com.exemplo/.Servico$Interno` teria `$Interno` expandido para vazio. A lista é gravada entre aspas simples (com `'` escapado), e a junção compara componentes inteiros separados por `:`.
