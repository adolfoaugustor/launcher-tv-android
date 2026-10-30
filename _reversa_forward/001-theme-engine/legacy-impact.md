# Legacy impact: Motor de temas (theme-engine)

> Identificador: `001-theme-engine`
> Data: `2026-10-29`
> Feature greenfield, sem legado pré-existente. Âncora: `_reversa_sdd/prd.md` + specs SDD (`_reversa_sdd/sdd/theme-engine.md`).
> Política de edição no momento da execução: `allowLegacyEdits: true`, `allowedPaths: ["launcher/**"]`. Todos os arquivos tocados casam com `launcher/**`.
> Estado: rodada parcial. Código escrito e revisado por leitura, nada compilado nem testado (T018, T029 e T030 bloqueadas por falta de JDK, Android SDK e Gradle).

## Arquivos afetados

| Arquivo afetado | Componente | Tipo | Severidade | Justificativa |
|-----------------|------------|------|------------|---------------|
| `launcher/.gitignore` | Projeto Gradle `launcher/android/` | componente-novo | LOW | Ignora saídas de build |
| `launcher/android/settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml` | Projeto Gradle `launcher/android/` | componente-novo | LOW | Base do app (D-01, D-02) |
| `launcher/android/theme-core/build.gradle.kts` | `:theme-core` | componente-novo | LOW | Módulo Kotlin JVM (D-03, D-04) |
| `launcher/android/theme-core/src/main/kotlin/br/tematv/themecore/ThemeManifest.kt` | `:theme-core` (`_reversa_sdd/sdd/theme-engine.md#9-modelo-de-dados`) | componente-novo | MEDIUM | Modelo do `theme.json` v1, códigos de erro, `ValidatedTheme` |
| `.../themecore/ManifestParser.kt` | `:theme-core` (`theme-engine.md#6-requisitos-funcionais`, RF-02, RF-03) | componente-novo | MEDIUM | Regras de campo do esquema v1 |
| `.../themecore/SafeZipExtractor.kt` | `:theme-core` (`theme-engine.md#7-requisitos-não-funcionais`, RNF-04) | componente-novo | HIGH | Defesa contra zip slip e zip bomb; pacote vem de fora do app |
| `.../themecore/ImageHeaderReader.kt` | `:theme-core` (D-06) | componente-novo | MEDIUM | Reconhece PNG, JPEG e WEBP sem decodificar |
| `.../themecore/ThemeValidator.kt` | `:theme-core` (RF-02, RF-12) | componente-novo | MEDIUM | Validação da pasta do tema e dos arquivos referenciados |
| `.../themecore/ThemePackageLoader.kt` | `:theme-core` (RF-01, RNF de tamanho) | componente-novo | MEDIUM | Tamanho, extração e validação do `.tematv` |
| `launcher/android/theme-core/src/test/kotlin/...` (5 arquivos) | `:theme-core` | componente-novo | LOW | Testes do núcleo e dos temas embutidos |
| `launcher/android/app/build.gradle.kts` | `:app` | componente-novo | LOW | App `br.tematv.launcher` e tarefa `packSampleThemes` (D-12) |
| `launcher/android/app/src/main/AndroidManifest.xml` | `:app` | componente-novo | MEDIUM | Receiver exportado `RELOAD_THEME` (contrato novo) |
| `.../launcher/TemaTvApplication.kt` | `:app` (D-08) | componente-novo | MEDIUM | Cria `themes/` e o repositório; primeira leitura do tema |
| `.../launcher/theme/ThemeStorage.kt` | `:app` (D-07, RF-04) | componente-novo | MEDIUM | Pasta de temas, `active_theme.txt`, cache por SHA-256 |
| `.../launcher/theme/ThemeStateStore.kt` | `:app` (D-09, RF-09) | componente-novo | LOW | Último tema válido e último erro em `SharedPreferences` |
| `.../launcher/theme/ThemeImageLoader.kt` | `:app` (D-06, RF-10) | componente-novo | LOW | Decodificação limitada a 1920×1080 e fonte |
| `.../launcher/theme/DefaultThemeProvider.kt` | `:app` (D-11, RF-05) | componente-novo | MEDIUM | Tema padrão copiado de `assets/` uma vez por versão |
| `.../launcher/theme/ThemeRepository.kt` | `:app` (D-10, RF-04, RF-06, RF-08) | componente-novo | HIGH | Decide o tema ativo; regra "último válido conhecido" |
| `.../launcher/theme/AndroidThemeLogger.kt` | `:app` (D-14) | componente-novo | LOW | Log com a tag `TemaTV` |
| `.../launcher/theme/ReloadThemeReceiver.kt` | `:app` (contrato `interfaces/broadcast-reload-theme.md`) | componente-novo | MEDIUM | Recarga do tema pelo ADB |
| `launcher/android/app/src/test/java/.../ThemeRepositoryTest.kt` | `:app` | componente-novo | LOW | Testes JVM do repositório |
| `launcher/android/app/src/main/assets/default-theme/*` | Tema padrão (RF-05) | componente-novo | LOW | Manifesto e arte provisória |
| `launcher/android/sample-themes/*` | Pacotes de exemplo (RF-11) | componente-novo | LOW | Bíblia e hambúrguer |
| `launcher/android/tools/make_placeholder_art.py` | Arte provisória (D-13) | componente-novo | LOW | Gera os PNGs sem dependências |
| `launcher/android/README.md` | Documentação | componente-novo | LOW | Como compilar e testar |

## Diff conceitual por componente

- **Projeto Gradle `launcher/android/`:** nasce a base do app Android TV, com dois módulos e o catálogo de versões. As features 2 a 4 vão acrescentar código ao `:app`.
- **`:theme-core`:** nasce o formato `.tematv` v1 como contrato de arquivo. O núcleo é Kotlin puro, usa só APIs disponíveis no Android 9 e devolve recusas como `ThemeError(code, field)`, sem exceções soltas.
- **`:app`:** nasce o repositório de tema com estado observável (`StateFlow<ActiveTheme?>`), a persistência do último válido e do último erro, o tema padrão embutido e o broadcast de recarga. Não há tela nesta feature.

## Preservadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.

## Modificadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.
