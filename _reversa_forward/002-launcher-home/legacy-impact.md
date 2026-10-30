# Legacy impact: Tela inicial do launcher (launcher-home)

> Identificador: `002-launcher-home`
> Data: `2026-10-30`
> Feature greenfield, sem legado pré-existente. Âncora: `_reversa_sdd/prd.md` + specs SDD (`_reversa_sdd/sdd/launcher-home.md`) + adendo vigente `_reversa_sdd/addenda/001-theme-engine.md`.
> Política de edição no momento da execução: `allowLegacyEdits: true`, `allowedPaths: ["launcher/**"]`. Todos os arquivos tocados casam com `launcher/**`.
> Estado: concluída. 23 de 23 ações; 63 testes automatizados passando; teste na TV de referência com capturas de tela; metas de desempenho e memória cumpridas.

## Arquivos afetados

| Arquivo afetado | Componente | Tipo | Severidade | Justificativa |
|-----------------|------------|------|------------|---------------|
| `launcher/android/gradle/libs.versions.toml`, `app/build.gradle.kts` | Projeto Gradle | componente-novo | LOW | `recyclerview`, `activity-ktx`, `lifecycle-runtime-ktx` (D-02) |
| `launcher/android/app/src/main/AndroidManifest.xml` | `:app` | componente-novo | MEDIUM | `HomeActivity` com `LEANBACK_LAUNCHER` e banner, `ThemeOptionsActivity`, `<queries>` |
| `launcher/android/app/src/main/res/` (`values/`, `layout/`, `drawable-xhdpi/banner.png`) | `:app` (`launcher-home.md#8-design-e-interface`) | componente-novo | LOW | Textos, cores, dimensões, layouts e banner |
| `.../home/AppListBuilder.kt` | `:app` (RF-01, RF-03, RN-06) | componente-novo | MEDIUM | Filtro dos utilitários e ordem alfabética |
| `.../home/VerseOfTheDay.kt` | `:app` (RF-07, RN-03) | componente-novo | LOW | Índice do versículo do dia |
| `.../home/SlotGeometry.kt` | `:app` (RF-02) | componente-novo | LOW | Encaixe do banner no painel da moldura |
| `.../home/InstalledAppsSource.kt` | `:app` (D-05, D-06) | componente-novo | MEDIUM | Lista de apps de TV com banner ou ícone |
| `.../home/ThemedAppTileView.kt` | `:app` (RF-02, RF-05) | componente-novo | MEDIUM | Desenho de cada app na moldura, foco com borda e zoom |
| `.../home/AppGridAdapter.kt` | `:app` | componente-novo | LOW | Grade de 5 colunas |
| `.../home/HomeActivity.kt` | `:app` (RF-04, RF-06 a RF-11, RF-13, RF-14, RF-16) | componente-novo | HIGH | Tela inicial: tema, versículo, grade, foco, Voltar, avisos de pacote e de data |
| `.../home/ThemeOptionsActivity.kt` | `:app` (RF-11, RF-12, RN-05) | componente-novo | LOW | Diagnóstico |
| `.../theme/ThemeRepository.kt` e `ThemeRepositoryTest.kt` | `:app` (feature 001; D-16) | regra-alterada | LOW | Só acrescenta o tempo de aplicação no log; comportamento igual |
| `launcher/android/app/src/test/java/.../home/*Test.kt` | `:app` | componente-novo | LOW | Testes da lista, do versículo e da geometria |
| `launcher/android/tools/make_placeholder_art.py` | Arte provisória | componente-novo | LOW | Gera também o banner do launcher |
| `launcher/android/tools/make_large_test_theme.py` | Ferramenta de medida | componente-novo | LOW | Pacote de ~14 MB para medir a aplicação |
| `launcher/android/README.md` | Documentação | componente-novo | LOW | Tela inicial e lista de utilitários escondidos |
| `launcher/android/app/src/main/java/br/tematv/launcher/home/VersePicker.kt` e `VersePickerTest.kt` (E001) | `:app` | regra-alterada | LOW | Sorteio do versículo; substituem `VerseOfTheDay.kt` e o teste, apagados com a confirmação do usuário |
| `launcher/android/app/src/main/java/br/tematv/launcher/home/HomeActivity.kt` (E001) | `:app` | regra-alterada | LOW | Sorteia a cada `onStart` e mantém o versículo enquanto a tela está na frente; sai o receptor de troca de data |
| `launcher/android/app/build.gradle.kts` (E001) | `:app` | regra-alterada | LOW | Versão 8 / 0.6.2 para a TV receber a mudança |

## Diff conceitual por componente

- **`:app` / `br.tematv.launcher.home`:** nasce a tela inicial temática, aberta pela lista de apps do Google TV. Ela observa o tema ativo da feature 001 e redesenha tudo quando ele muda.
- **`ThemeRepository`:** a única mudança é o log `Tema aplicado: <id> em <N> ms`; as regras de escolha do tema não mudaram.
- **Manifesto:** o app passa a declarar `<queries>` para enxergar os apps de TV no Android 11+, e a tela entra na lista de apps do Google TV. A categoria `HOME` continua fora (decisão da feature 003).

## Preservadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente. As regras da feature 001 (W001 a W010) continuam válidas; o teste na TV usou a importação e a recarga sem mudanças.

## Modificadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.
