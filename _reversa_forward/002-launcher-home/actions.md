# Actions: Tela inicial do launcher (launcher-home)

> Identificador: `002-launcher-home`
> Data: `2026-10-30`
> Roadmap: `_reversa_forward/002-launcher-home/roadmap.md`
> Branch: `feature/002-launcher-home`
> Raiz do código: `launcher/android/` (área liberada: `launcher/**`). Pacote novo: `br.tematv.launcher.home`.

## Resumo

| Métrica | Valor |
|---------|-------|
| Total de ações | 23 |
| Paralelizáveis (`[//]`) | 11 |
| Maior cadeia de dependência | 8 (T001 → T011 → T013 → T015 → T016 → T019 → T022 → T023) |

## Fase 1, Preparação

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T001 | Acrescentar ao catálogo de versões e ao `:app` as dependências `recyclerview` 1.3.2, `activity-ktx` 1.9.3 e `lifecycle-runtime-ktx` 2.8.7 (D-02) | - | - | `launcher/android/app/build.gradle.kts` | 🟢 | `[X]` |
| T002 | Estender o script de arte provisória para gerar o banner do launcher (320×180, logotipo sobre fundo do tema padrão) em `res/drawable-xhdpi/banner.png` e rodar o script (D-17) | - | `[//]` | `launcher/android/tools/make_placeholder_art.py` | 🟡 | `[X]` |
| T003 | Registrar no manifesto a `HomeActivity` (`MAIN` + `LEANBACK_LAUNCHER`, banner, `singleTask`, tema com fundo `#FFF6E0`), a `ThemeOptionsActivity` e o bloco `<queries>` para `MAIN` + `LEANBACK_LAUNCHER` (D-07, D-11, RF-15, RF-16) | T001, T002, T004 | - | `launcher/android/app/src/main/AndroidManifest.xml` | 🟢 | `[X]` |
| T004 | Criar os recursos de valores: cor de fundo padrão, estilo da janela sem barra e textos em português (botões, avisos, diagnóstico, "disponível em versão futura", "Nenhum app instalado") | - | `[//]` | `launcher/android/app/src/main/res/values/strings.xml` | 🟢 | `[X]` |

## Fase 2, Testes

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T005 | Testes do `AppListBuilder`: remove o próprio pacote e os 7 utilitários, mantém `com.tcl.tv`, ordena "Disney+" < "Netflix" < "YouTube" ignorando acentos e maiúsculas ("Álbum" junto de "album") | - | `[//]` | `launcher/android/app/src/test/java/br/tematv/launcher/home/AppListBuilderTest.kt` | 🟢 | `[X]` |
| T006 | Testes do `VerseOfTheDay`: mesmo índice no mesmo dia, avança um por dia, volta ao primeiro depois do último, funciona com 1 versículo e com datas antes de 1970 | - | `[//]` | `launcher/android/app/src/test/java/br/tematv/launcher/home/VerseOfTheDayTest.kt` | 🟢 | `[X]` |
| T007 | Testes do `SlotGeometry`: área do slot em pixels para um item de 260×340; banner 16:9 encaixado na largura com faixas; ícone quadrado encaixado na altura; nunca sai do slot | - | `[//]` | `launcher/android/app/src/test/java/br/tematv/launcher/home/SlotGeometryTest.kt` | 🟢 | `[X]` |

## Fase 3, Núcleo

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T008 | Implementar `HiddenSystemApps` (lista de RN-06) e `AppListBuilder` (filtra e ordena com `Collator` pt-BR de força primária) (RF-01, RF-03, RN-06) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/home/AppListBuilder.kt` | 🟢 | `[X]` |
| T009 | Implementar `VerseOfTheDay.indexFor(data, quantidade)` com `floorMod(epochDay, quantidade)` (RF-07, RN-03) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/home/VerseOfTheDay.kt` | 🟢 | `[X]` |
| T010 | Implementar `SlotGeometry`: converte `FrameSlot` em retângulo de pixels e calcula o encaixe "cabe sem distorcer" | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/home/SlotGeometry.kt` | 🟢 | `[X]` |
| T011 | Implementar `InstalledAppsSource`: consulta `MAIN` + `LEANBACK_LAUNCHER`, carrega nome e banner (atividade, depois aplicativo, depois ícone) e entrega `AppEntry` pela `AppListBuilder` (D-05, D-06) | T001, T008 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/InstalledAppsSource.kt` | 🟢 | `[X]` |
| T012 | Implementar `ThemedAppTileView`: desenha moldura escalada, banner ou ícone no `icon_slot`, nome em uma linha com reticências no `label_slot`, e no foco borda na cor `accent` e zoom de 10% (D-03, RF-02, RF-05) | T010 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/ThemedAppTileView.kt` | 🟢 | `[X]` |
| T013 | Implementar `AppGridAdapter`: lista de `AppEntry`, moldura e cores compartilhadas, clique com OK repassado à tela | T011, T012 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/AppGridAdapter.kt` | 🟢 | `[X]` |
| T014 | Criar o layout da tela inicial: fundo, cabeçalho (logotipo, mensagem em 2 linhas, botões "Configurações" e "Tema" à direita), faixa do versículo com referência e atribuição, grade de 5 colunas, texto de grade vazia e faixa de aviso no rodapé (D-04, D-10) | T004 | - | `launcher/android/app/src/main/res/layout/activity_home.xml` | 🟢 | `[X]` |
| T015 | Implementar `HomeActivity`: coleta `activeTheme`, aplica fundo, logotipo, mensagem, fonte, cores e versículo; monta a grade; foco inicial no primeiro app ou no último aberto; direcional para cima leva aos botões; Voltar consumido; abertura de app com aviso de 4 s em caso de falha (RF-04, RF-06 a RF-08, RF-10, RF-11, RF-13, RF-14, RF-16) | T009, T013, T014 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/HomeActivity.kt` | 🟢 | `[X]` |

## Fase 4, Integração

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T016 | Na `HomeActivity`, registrar e remover em `onStart`/`onStop` os avisos de pacote (instalado, removido, alterado, substituído) para recarregar a grade mantendo o foco, e os avisos de data, hora e fuso para recalcular o versículo (D-08, D-09, RF-09, EC-01) | T015 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/HomeActivity.kt` | 🟢 | `[X]` |
| T017 | Implementar `ThemeOptionsActivity` e seu layout: versão do app, tema ativo e origem, último erro (código, campo, pacote, data e hora) e os itens de acessibilidade e licença como "disponível em versão futura" (D-15, RF-11, RF-12, RN-05) | T003, T004 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/ThemeOptionsActivity.kt` | 🟢 | `[X]` |
| T018 | No `ThemeRepository`, medir e registrar no log o tempo de cada aplicação (`Tema aplicado: <id> em <N> ms`), ajustando o teste existente (D-16) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeRepository.kt` | 🟢 | `[X]` |
| T019 | Rodar `./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug :app:packSampleThemes` e corrigir até tudo passar | T005, T006, T007, T016, T017, T018 | - | `launcher/android/` | 🟢 | `[X]` |

## Fase 5, Polimento

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T020 | Atualizar o `README.md` do app com a tela inicial, como abri-la e a lista de utilitários escondidos | - | `[//]` | `launcher/android/README.md` | 🟢 | `[X]` |
| T021 | Criar script que gera um pacote de teste perto de 15 MB (fundo com ruído, que não comprime) para medir o tempo de aplicação | - | `[//]` | `launcher/android/tools/make_large_test_theme.py` | 🟢 | `[X]` |
| T022 | Teste na TV de referência seguindo o `onboarding.md` §2, com captura de tela: tema bíblico, ordem dos apps, utilitários escondidos, botões no cabeçalho, OK abre app, Voltar não sai, diagnóstico, troca para hambúrguer com a tela aberta. **Antes, avisar o usuário para ligar a TV e confirmar que ela responde.** | T019 | - | `launcher/android/app/build/outputs/apk/debug/app-debug.apk` | 🟡 | `[X]` |
| T023 | Medir na TCL: abertura a frio (meta ≤ 2 s), PSS em primeiro plano (meta ≤ 100 MB) e aplicação do pacote de ~15 MB (meta ≤ 3 s); registrar os números nas Notas | T021, T022 | - | `_reversa_forward/002-launcher-home/actions.md` | 🟡 | `[X]` |

## Notas de execução

- **RF-09 na TV:** para conferir a atualização da grade por instalação sem mexer nos apps do usuário, só com um APK de teste. Se não houver um, RF-09 fica verificado por código e por reinstalar o próprio launcher; registrar aqui o que foi feito.
- **Commit:** ao fechar todas as ações, commitar na `feature/002-launcher-home` (junto com o adendo `_reversa_sdd/addenda/001-theme-engine.md`, que está pendente) e criar a `feature/003-home-takeover` a partir dela.

### Rodada 1 do `/reversa-coding` (2026-10-30)

- T019: 63 testes passando (39 do núcleo, 24 do app). Um teste falhou na primeira rodada por erro do próprio teste (`SlotGeometryTest`, tolerância absoluta numa proporção 1000:1); corrigido para tolerância relativa.
- T022 na TCL, com capturas de tela:
  - Tema bíblico completo: fundo, logotipo, mensagem em 2 linhas, versículo com referência e atribuição.
  - 14 apps em ordem alfabética, sem os utilitários de RN-06; a TV ao vivo aparece.
  - Nomes longos com reticências ("Box player OFFI...").
  - Foco com borda e zoom; direcional para cima leva aos botões; Voltar 10 vezes mantém a tela; "Tema" abre o diagnóstico.
  - OK abriu o YouTube; após encerrar o processo, o foco voltou ao YouTube (RF-13).
  - Troca para o hambúrguer com a tela aberta, aplicada em 60 ms.
- Defeitos achados no teste na TV e corrigidos na mesma rodada:
  1. Foco inicial ia para o botão "Configurações", que pegava o foco antes de a grade carregar. Agora a primeira carga sempre leva o foco à grade.
  2. Com a grade rolada, a primeira linha invadia a linha de atribuição do versículo. Agora a grade recorta no padding (a margem comporta o zoom).
  3. O diagnóstico mostrava espaço duplo quando o erro não tinha campo.
- T023, medidas na TCL:
  - Abertura a frio: 1455, 1498 e 1455 ms (meta ≤ 2000 ms). A medida feita logo após reinstalar o APK deu 2005 ms e não é representativa.
  - Memória: 80,7 MB de PSS com o tema bíblico; 92,5 MB com o pacote de 14 MB (meta ≤ 100 MB).
  - Aplicação do pacote de 14 MB: 570 ms na primeira vez, 96 ms com cache (meta ≤ 3000 ms; pendência da feature 001 fechada).
- RF-09 (grade atualizada ao instalar ou remover app) não foi testado na TV: não há APK de teste, e o próprio launcher fica fora da grade. A implementação usa os avisos de pacote do sistema (D-08).
- Observação para a feature 6: logo após `adb install -r`, o `content write` respondeu `Could not find provider` por alguns segundos. A instalação precisa repetir a importação até o provider responder.
- O app (build de debug) ficou instalado na TV com o tema `biblia-exemplo` ativo; abre pela lista de apps do Google TV.

## Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-to-do` | reversa |
