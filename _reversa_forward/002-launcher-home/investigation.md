# Investigation: Tela inicial do launcher (launcher-home)

> Identificador: `002-launcher-home`
> Data: `2026-10-30`

## 1. Pesquisa de fundo

- 🟢 **TV de referência (medido em 2026-10-30):** 1920×1080, densidade 320 (2,0), ou seja 960×540 dp. A moldura de 260×340 px corresponde a 130×170 dp.
- 🟢 **Apps com lançador de TV na TCL:** 21. Sem os 7 utilitários técnicos de RN-06 e sem o próprio launcher, a grade fica com 14 apps (3 linhas de 5).
- 🟢 **Visibilidade de pacotes (Android 11+):** com targetSdk 30 ou superior, `queryIntentActivities` só enxerga outros apps declarados em `<queries>` (ou com `QUERY_ALL_PACKAGES`). Declarar o intent `MAIN` + `LEANBACK_LAUNCHER` basta para a grade.
- 🟢 **Banners de TV:** apps para Android TV declaram `android:banner` (320×180, 16:9) na atividade ou no `<application>`; `loadBanner` devolve nulo quando não há.
- 🟢 **Memória de bitmaps:** o fundo 1920×1080 em ARGB_8888 ocupa cerca de 8 MB; em RGB_565, cerca de 4 MB. Uma moldura de 260×340 ocupa cerca de 350 KB e pode ser compartilhada entre todos os itens.
- 🟢 **Voltar no Android 13+:** `onBackPressed` está obsoleto; `OnBackPressedDispatcher` funciona da API 28 em diante pela biblioteca `activity`.

## 2. Alternativas avaliadas

| Tema | Alternativa | Por que foi descartada |
|------|-------------|------------------------|
| Toolkit de UI | Jetpack Compose for TV | Runtime e partida a frio maiores; a meta é ≤ 2 s e ≤ 100 MB numa TV de 2 GB |
| Toolkit de UI | Biblioteca Leanback (`BrowseSupportFragment`) | Visual e navegação presos ao padrão do Google; a moldura temática exige desenho próprio |
| Item da grade | Layout XML com `ImageView` e `TextView` | Mais Views por item e mais trabalho de layout na rolagem |
| Lista de apps | `LauncherApps` | Retorna a categoria `LAUNCHER` do celular, não a `LEANBACK_LAUNCHER` da TV |
| Aviso de falha | `Toast` | Duração fixa de 2 s ou 3,5 s; o requisito pede 4 s |
| Categoria HOME já nesta feature | Declarar `HOME` | Pode fazer o Google TV mostrar seletor de tela inicial; decisão pertence à feature 003 |

## 3. Padrões aplicáveis

- **Estado observável:** a tela coleta `ThemeRepository.activeTheme` com `repeatOnLifecycle(STARTED)` e redesenha a cada tema novo.
- **Lógica pura testável:** filtro e ordem dos apps, versículo do dia e geometria do painel não dependem do Android.
- **Bitmap compartilhado:** a moldura do tema é decodificada uma vez e desenhada por todos os itens.

## 4. Fontes externas

- 🟢 Visibilidade de pacotes: https://developer.android.com/training/package-visibility
- 🟢 Apps para TV, banner e lançador: https://developer.android.com/training/tv/start/start
- 🟢 Voltar com `OnBackPressedDispatcher`: https://developer.android.com/guide/navigation/custom-back
- 🟢 Carregamento eficiente de bitmaps: https://developer.android.com/topic/performance/graphics/load-bitmap
