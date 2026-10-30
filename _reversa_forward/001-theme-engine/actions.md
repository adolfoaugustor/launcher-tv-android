# Actions: Motor de temas (theme-engine)

> Identificador: `001-theme-engine`
> Data: `2026-10-29`
> Roadmap: `_reversa_forward/001-theme-engine/roadmap.md`
> Branch: `feature/001-theme-engine`
> Raiz do código: `launcher/android/` (única área liberada em `.reversa/reversa-config.json`: `launcher/**`)

## Resumo

| Métrica | Valor |
|---------|-------|
| Total de ações | 33 |
| Paralelizáveis (`[//]`) | 9 |
| Maior cadeia de dependência | 12 (T001 → T003 → T013 → T015 → T016 → T017 → T019 → T022 → T023 → T024 → T029 → T030) |

## Fase 1, Preparação

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T001 | Criar o projeto Gradle raiz: `settings.gradle.kts` com os módulos `:theme-core` e `:app`, `build.gradle.kts` raiz, `gradle.properties` e o catálogo de versões com JDK 17, AGP 8.5.x, Kotlin 2.0.x, kotlinx.serialization e JUnit (D-01, D-02) | - | - | `launcher/android/settings.gradle.kts` | 🟢 | `[X]` |
| T002 | Criar `launcher/.gitignore` ignorando `build/`, `.gradle/`, `local.properties`, `*.iml` e `.idea/` | - | `[//]` | `launcher/.gitignore` | 🟢 | `[X]` |
| T003 | Configurar o módulo `:theme-core` como biblioteca Kotlin JVM 17 com kotlinx.serialization e JUnit (D-03, D-04) | T001 | - | `launcher/android/theme-core/build.gradle.kts` | 🟢 | `[X]` |
| T004 | Configurar o módulo `:app` como aplicativo Android `br.tematv.launcher` (minSdk 28, targetSdk e compileSdk 34) dependendo de `:theme-core` | T001 | - | `launcher/android/app/build.gradle.kts` | 🟢 | `[X]` |
| T005 | Criar o `AndroidManifest.xml` (sem atividade de lançador nesta feature) e a classe `TemaTvApplication`, que cria a pasta externa `themes/` no `onCreate` (D-07, D-08) | T004 | - | `launcher/android/app/src/main/java/br/tematv/launcher/TemaTvApplication.kt` | 🟢 | `[X]` |
| T006 | Escrever o script Python sem dependências que gera PNGs provisórios: moldura de Bíblia e de hambúrguer (260×340 px, retrato, com transparência), logotipo do produto e fundo infantil (D-13) | - | `[//]` | `launcher/android/tools/make_placeholder_art.py` | 🟡 | `[X]` |
| T007 | Rodar o script e gravar as imagens em `sample-themes/biblia-exemplo/`, `sample-themes/hamburguer-exemplo/` e `app/src/main/assets/default-theme/` | T006 | - | `launcher/android/sample-themes/` | 🟡 | `[X]` |
| T008 | Escrever os três `theme.json` (tema padrão "Deus te abençoe!", exemplo Bíblia, exemplo hambúrguer) com `icon_slot` e `label_slot` coerentes com as molduras e versículos provisórios (ver Notas) | T007 | - | `launcher/android/app/src/main/assets/default-theme/theme.json` | 🟡 | `[X]` |

## Fase 2, Testes

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T009 | Criar utilitário de testes que monta pacotes `.tematv` em memória e em arquivo temporário (manifesto válido de base, imagens mínimas PNG, JPEG e WEBP, entradas arbitrárias) | T003 | - | `launcher/android/theme-core/src/test/kotlin/br/tematv/themecore/TestPackages.kt` | 🟢 | `[X]` |
| T010 | Testes do extrator seguro: entrada `../`, caminho absoluto, mais de 200 entradas, mais de 40 MB descompactados, ZIP truncado, extração normal | T009 | `[//]` | `launcher/android/theme-core/src/test/kotlin/br/tematv/themecore/SafeZipExtractorTest.kt` | 🟢 | `[X]` |
| T011 | Testes do validador: cada regra da tabela de `data-delta.md` §1.1 com o código e o campo esperados, `schema_version` 2, imagem ausente, imagem com assinatura errada, campos desconhecidos ignorados | T009 | `[//]` | `launcher/android/theme-core/src/test/kotlin/br/tematv/themecore/ThemeValidatorTest.kt` | 🟢 | `[X]` |
| T012 | Testes do carregador de pacote: pacote válido, sem `theme.json`, JSON malformado, arquivo de 16 MB, fonte própria opcional | T009 | `[//]` | `launcher/android/theme-core/src/test/kotlin/br/tematv/themecore/ThemePackageLoaderTest.kt` | 🟢 | `[X]` |

## Fase 3, Núcleo

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T013 | Criar os códigos `ThemeErrorCode`, o tipo `ThemeError(code, field)` e o modelo serializável `ThemeManifest` v1, ignorando campos desconhecidos | T003 | - | `launcher/android/theme-core/src/main/kotlin/br/tematv/themecore/ThemeManifest.kt` | 🟢 | `[X]` |
| T014 | Implementar `SafeZipExtractor` com normalização de caminho e limites de entradas e de tamanho descompactado (D-05) | T013 | - | `launcher/android/theme-core/src/main/kotlin/br/tematv/themecore/SafeZipExtractor.kt` | 🟢 | `[X]` |
| T015 | Implementar `ImageHeaderReader`: reconhece PNG, JPEG e WEBP pela assinatura e lê largura e altura do cabeçalho (D-06) | T013 | `[//]` | `launcher/android/theme-core/src/main/kotlin/br/tematv/themecore/ImageHeaderReader.kt` | 🟢 | `[X]` |
| T016 | Implementar `ThemeValidator` sobre uma pasta descompactada: regras de campo, existência e tipo de arquivos, proporção da moldura (RF-02, RF-03, RF-12) | T013, T015 | - | `launcher/android/theme-core/src/main/kotlin/br/tematv/themecore/ThemeValidator.kt` | 🟢 | `[X]` |
| T017 | Implementar `ThemePackageLoader`: confere tamanho, extrai, lê o JSON, valida e devolve `ValidatedTheme` com caminhos resolvidos ou `ThemeError` | T014, T016 | - | `launcher/android/theme-core/src/main/kotlin/br/tematv/themecore/ThemePackageLoader.kt` | 🟢 | `[X]` |
| T018 | Rodar `./gradlew :theme-core:test` e corrigir o núcleo até todos os testes das ações T010 a T012 passarem | T010, T011, T012, T017 | - | `launcher/android/theme-core/` | 🟢 | `[X]` |

## Fase 4, Integração

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T019 | Implementar `ThemeStorage`: pasta `themes/`, leitura de `active_theme.txt`, SHA-256 do pacote, pasta de cache `theme-cache/<theme_id>-<sha curto>/` e limpeza de caches antigos (D-07) | T005, T017 | - | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeStorage.kt` | 🟢 | `[X]` |
| T020 | Implementar `ThemeStateStore` em `SharedPreferences` `theme_state`: último pacote válido e último erro com código, campo, pacote e data (D-09, RF-09) | T004 | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeStateStore.kt` | 🟢 | `[X]` |
| T021 | Implementar `ThemeImageLoader`: decodifica imagens com redução de amostragem até 1920×1080 e carrega a fonte própria (D-06, RF-10) | T004 | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeImageLoader.kt` | 🟢 | `[X]` |
| T022 | Implementar `DefaultThemeProvider`: copia `assets/default-theme/` para o cache na primeira execução de cada versão do app e o valida com o `ThemeValidator` (D-11, RF-05) | T008, T019 | - | `launcher/android/app/src/main/java/br/tematv/launcher/theme/DefaultThemeProvider.kt` | 🟢 | `[X]` |
| T023 | Implementar `ThemeRepository`: `StateFlow<ActiveTheme>`, `reload()` serializado, último tema válido mantido em caso de recusa, e logs `TemaTV` "Tema aplicado: <id>" e "Pacote recusado: <código> <campo>" (D-10, D-14, RF-04, RF-06, RF-08) | T019, T020, T021, T022 | - | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeRepository.kt` | 🟡 | `[X]` |
| T024 | Implementar `ReloadThemeReceiver` com `goAsync()` e registrá-lo exportado no manifesto para a ação `br.tematv.launcher.action.RELOAD_THEME` (contrato `interfaces/broadcast-reload-theme.md`) | T023 | - | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ReloadThemeReceiver.kt` | 🟡 | `[X]` |
| T025 | Testes JVM do `ThemeRepository` com armazenamento e estado falsos: pacote válido publica, recusado mantém o anterior, sem pacote usa o padrão, arquivo ativo apagado gera `THEME_ACTIVE_NOT_FOUND`, falha de IO gera `THEME_IO_ERROR` | T023 | - | `launcher/android/app/src/test/java/br/tematv/launcher/theme/ThemeRepositoryTest.kt` | 🟢 | `[X]` |
| T026 | Criar a tarefa Gradle `:app:packSampleThemes` que compacta cada pasta de `sample-themes/` em `app/build/sample-themes/<nome>.tematv` (D-12) | T004, T008 | - | `launcher/android/app/build.gradle.kts` | 🟢 | `[X]` |
| T027 | Teste que valida o tema padrão em `assets/` e as duas pastas de `sample-themes/` com o `ThemeValidator`, e confere que as molduras dos dois exemplos são diferentes (RF-05, RF-11) | T008, T018 | - | `launcher/android/theme-core/src/test/kotlin/br/tematv/themecore/BundledThemesTest.kt` | 🟢 | `[X]` |

## Fase 5, Polimento

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T028 | Escrever o `README.md` do app: o que existe nesta feature, como compilar e testar, link para o `onboarding.md` | - | `[//]` | `launcher/android/README.md` | 🟢 | `[X]` |
| T029 | Rodar `./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug :app:packSampleThemes` e corrigir até tudo passar | T024, T025, T026, T027 | - | `launcher/android/` | 🟢 | `[X]` |
| T030 | Teste na TV de referência (TCL, 192.168.1.2) seguindo o `onboarding.md` §3. **Antes, avisar o usuário para ligar a TV e confirmar que ela responde.** Verificar tema padrão, `biblia-exemplo` aplicado, pacote inválido recusado com `THEME_ARCHIVE_CORRUPT` mantendo `biblia-exemplo`, e pacote ativo inexistente recusado com `THEME_ACTIVE_NOT_FOUND` voltando ao tema padrão | T029, T033 | - | `launcher/android/app/build/outputs/apk/debug/app-debug.apk` | 🟡 | `[X]` |

## Fase 6, Emenda: importação pelo ADB (RF-13, D-15)

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T031 | Criar `ThemeImportNames` (nomes aceitos na importação) e o teste JVM com nomes válidos, com `/`, com `..`, com maiúsculas e com mais de um segmento | T019 | - | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeImportNames.kt` | 🟢 | `[X]` |
| T032 | Criar `ThemeImportProvider` (só uid 2000 ou 0, escrita com truncamento, leitura para conferência) e registrá-lo exportado no manifesto com a autoridade `br.tematv.launcher.themes` (contrato `interfaces/importacao-adb.md`) | T031, T005 | - | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeImportProvider.kt` | 🟢 | `[X]` |
| T033 | Rodar `./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug :app:packSampleThemes` e corrigir até tudo passar | T032 | - | `launcher/android/` | 🟢 | `[X]` |

## Notas de execução

- **Pré-requisito de ferramentas:** a partir da T003, compilar exige JDK 17, Android SDK e Gradle (`onboarding.md` §1). As ações de escrita de código podem ser feitas antes da instalação, mas T018, T029 e T030 só fecham com as ferramentas instaladas.
- **Versículos provisórios (T008):** os pacotes de exemplo e o tema padrão usam versículos marcados como provisórios, com atribuição "Texto provisório, substituir por tradução licenciada". O uso comercial depende da licença da tradução (`_reversa_sdd/sdd/theme-builder.md#14-open-questions`, OQ-01). Substituir antes de distribuir para clientes.
- **T030 exige o usuário** (ligar a TV e aceitar o aviso de depuração). É a única ação que não roda sem interação.
- **Commit:** ao fechar todas as ações, commitar na branch `feature/001-theme-engine` e criar a `feature/002-launcher-home` a partir dela, conforme o fluxo de branches combinado com o usuário.

### Rodada 1 do `/reversa-coding` (2026-10-29)

- **Os `[X]` desta rodada significam código escrito e revisado por leitura, nada compilado nem testado.** O computador não tem JDK 17, Android SDK nem Gradle.
- T018, T029 e T030 ficaram abertas e registradas como `blocked` no `progress.jsonl` (falta de ferramentas). A execução seguiu para as Fases 4 e 5 com a T018 bloqueada, e a T027 foi marcada com a dependência T018 ainda aberta (teste escrito, não executado).
- Desvio do plano 1: o tema padrão é copiado para `filesDir/default-theme-<versionCode>/`, fora de `theme-cache/`, para a limpeza de caches nunca apagá-lo (D-11 e `data-delta.md` §1.4 atualizados).
- Desvio do plano 2: o broadcast de recarga precisa de `-f 32` (FLAG_INCLUDE_STOPPED_PACKAGES), porque o app recém-instalado fica "parado" (`interfaces/broadcast-reload-theme.md` e `onboarding.md` atualizados).
- Correção de roteiro: o `onboarding.md` §3 e a T030 diziam que um pacote ativo inexistente mantém o tema anterior; o requisito aprovado (cenário "Tema ativo apagado") e o código usam o tema padrão. O roteiro agora testa os dois casos separadamente.

### Rodada 2 do `/reversa-coding` (2026-10-30)

- Ferramentas instaladas pelo usuário: JDK 17, Android SDK (plataforma 34 em `~/android-sdk`) e Gradle 8.5. O wrapper do projeto foi gerado na versão 8.7 (exigida pelo AGP 8.5) e o `local.properties` aponta o SDK (fora do git).
- T018: 39 testes do núcleo passaram na primeira execução. T029: build, APK e pacotes de exemplo gerados; 8 testes do app passaram.
- T030 começou parcial: na TCL (Android 11), o shell do ADB (`uid=2000`, sem o grupo `ext_data_rw`) e o `run-as` não gravam em `Android/data/br.tematv.launcher/`; `adb push` respondeu `Permission denied`. A premissa do `roadmap.md` §4 era falsa.
- Decisão do usuário: resolver já na feature 1. Emenda registrada como RF-13 (`requirements.md`), D-15 (`roadmap.md`), contrato `interfaces/importacao-adb.md` e ações T031 a T033 (Fase 6).
- T033: 50 testes passaram (39 do núcleo e 11 do app).
- T030 na TV, 9 de 9 verificações: tema padrão; `biblia-exemplo` importado por `content write` e aplicado; pacote corrompido recusado com `THEME_ARCHIVE_CORRUPT` mantendo `biblia-exemplo`; pacote inexistente recusado com `THEME_ACTIVE_NOT_FOUND` voltando ao padrão; nome inválido recusado pela importação; `hamburguer-exemplo` aplicado pelo mesmo APK. Nenhum travamento.
- Não medido nesta rodada: tempo de aplicação de pacote de 15 MB (RNF de desempenho, Should) e memória do app; ficam para a feature 2, que tem tela.
- O app `br.tematv.launcher` (build de debug) ficou instalado na TV, sem tela e sem alterar o launcher do Google, com o tema `hamburguer-exemplo` ativo.
- Para convergir no `/reversa-sync`: `_reversa_sdd/sdd/device-provisioning.md` (RF-07, RF-08 e OQ-01) ainda descreve a cópia direta para `Android/data`; deve passar a usar a importação pelo ADB.

## Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-29 | Versão inicial gerada por `/reversa-to-do` | reversa |
| 2026-10-30 | Fase 6 (T031 a T033) acrescentada: importação pelo ADB | reversa-coding |
