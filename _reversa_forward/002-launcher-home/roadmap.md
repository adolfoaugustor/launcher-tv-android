# Roadmap: Tela inicial do launcher (launcher-home)

> Identificador: `002-launcher-home`
> Data: `2026-10-30`
> Requirements: `_reversa_forward/002-launcher-home/requirements.md`
> Branch: `feature/002-launcher-home`
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA

## 1. Resumo da abordagem

Delta sobre o app entregue na feature 001 (`launcher/android/`, ver `_reversa_sdd/addenda/001-theme-engine.md`). Entra uma atividade de tela inicial (`HomeActivity`) com Views clássicas do Android:
- cabeçalho com logotipo, mensagem e os botões "Configurações" e "Tema";
- faixa do versículo do dia;
- grade de 5 colunas (`RecyclerView`).

Cada app é desenhado por uma View própria (`ThemedAppTileView`), que pinta a moldura do tema, o banner do app dentro de `icon_slot` e o nome dentro de `label_slot`. A tela observa o `ThemeRepository.activeTheme` da feature 001 e redesenha quando o tema muda.

A lógica sem Android fica em classes puras com testes JVM: filtrar e ordenar apps, escolher o versículo do dia e calcular onde o banner cabe no painel. A lista de apps vem do `PackageManager` (com a declaração `<queries>` exigida no Android 11+) e é atualizada por avisos de instalação e remoção. A tela "Tema" é uma segunda atividade com o diagnóstico. O launcher aparece na lista de apps do Google TV pela categoria `LEANBACK_LAUNCHER`, com banner próprio. Nesta feature ele **não** se declara `HOME`: isso é decidido na feature 003 (`_reversa_sdd/sdd/launcher-home.md#14-open-questions`, OQ-02).

## 2. Princípios aplicados

`.reversa/principles.md` não existe. Valem as regras globais do usuário.

| Princípio | Como a feature se relaciona | Status |
|-----------|------------------------------|--------|
| YAGNI | Sem biblioteca de UI nova além de `RecyclerView` e `activity`/`lifecycle`; sem tela de configurações própria | respeita |
| Falhar cedo | Falha ao abrir app vira aviso visível e log; tema inválido já é tratado pela 001 | respeita |

## 3. Decisões técnicas

| ID | Decisão | Justificativa | Alternativas descartadas | Confidência |
|----|---------|----------------|--------------------------|-------------|
| D-01 | Views clássicas (`ComponentActivity`, `RecyclerView` com `GridLayoutManager` de 5 colunas) | Menor consumo de memória e partida mais rápida numa TV de 2 GB (RNF ≤ 100 MB, ≤ 2 s) | Jetpack Compose for TV (runtime maior); biblioteca Leanback (grade presa ao visual do Google) | 🟢 |
| D-02 | Dependências novas: `androidx.recyclerview:recyclerview:1.3.2`, `androidx.activity:activity-ktx:1.9.3`, `androidx.lifecycle:lifecycle-runtime-ktx:2.8.7` | Coletar o `StateFlow` só com a tela visível (`repeatOnLifecycle`) e tratar o Voltar com `OnBackPressedDispatcher` | AppCompat (desnecessário) | 🟢 |
| D-03 | `ThemedAppTileView` desenha tudo em `onDraw`: moldura escalada para o tamanho do item, banner em `icon_slot` com "caber sem distorcer" (letterbox), nome em `label_slot` cortado com reticências | Uma View por item, sem hierarquia aninhada; geometria testável em classe pura `SlotGeometry` | Layout XML com `ImageView` e `TextView` empilhados (mais Views por item) | 🟢 |
| D-04 | Tamanho do item: 130×170 dp (260×340 px na TCL, densidade 320); grade com 5 colunas e espaçamento para o zoom de 10% | Medido na TV: 1920×1080, densidade 320; referência da moldura definida na 001 | Tamanho proporcional à tela (complexidade sem ganho no MVP) | 🟢 |
| D-05 | Banner do app: `ActivityInfo.loadBanner`, depois `ApplicationInfo.loadBanner`, por último `loadIcon` | Clarificação de 2026-10-30: banner de TV, ícone como reserva | Só ícone | 🟢 |
| D-06 | Lista de apps: `queryIntentActivities(MAIN + LEANBACK_LAUNCHER)`, filtrada e ordenada por `AppListBuilder` (puro): remove o próprio pacote e a lista fixa `HiddenSystemApps`; ordena com `Collator` pt-BR de força primária (ignora acentos e maiúsculas) | RF-01, RF-03, RN-06 | `LauncherApps` (lista `CATEGORY_LAUNCHER`, não a de TV) | 🟢 |
| D-07 | Manifesto declara `<queries>` para `MAIN` + `LEANBACK_LAUNCHER` | Visibilidade de pacotes no Android 11+ com targetSdk 34; sem isso a lista vem vazia | Permissão `QUERY_ALL_PACKAGES` (ampla demais) | 🟢 |
| D-08 | Atualização da grade por `BroadcastReceiver` registrado enquanto a tela está iniciada (`PACKAGE_ADDED`, `PACKAGE_REMOVED`, `PACKAGE_CHANGED`, `PACKAGE_REPLACED`) e releitura completa da lista | RF-09; releitura de 14 a 30 apps é barata | Diferenças item a item (complexidade sem ganho) | 🟢 |
| D-09 | Versículo do dia por `VerseOfTheDay.indexFor(data, quantidade) = floorMod(epochDay, quantidade)` no fuso da TV; recalculado ao voltar à tela e nos avisos `DATE_CHANGED`, `TIME_SET` e `TIMEZONE_CHANGED` | RN-03, RF-07; a tela pode ficar aberta na virada do dia | Alarme agendado (desnecessário) | 🟢 |
| D-10 | Botões "Configurações" e "Tema" no canto superior direito; direcional para cima a partir da primeira linha da grade leva a eles (`nextFocusUp`) | RF-10, RF-11; clarificação de 2026-10-30 | Itens no fim da grade | 🟢 |
| D-11 | `HomeActivity` com `launchMode="singleTask"`, fundo da janela na cor do tema padrão `#FFF6E0` e categoria `LEANBACK_LAUNCHER` com banner próprio; sem categoria `HOME` | RF-15, RF-16; categoria HOME fica para a feature 003 | Declarar HOME já (risco de seletor de tela inicial no Google TV) | 🟡 |
| D-12 | Voltar na grade é consumido por um `OnBackPressedCallback` sem ação | RF-14, RN-04 | Sobrescrever `onKeyDown` | 🟢 |
| D-13 | Falha ao abrir app: aviso próprio na tela por 4 s (faixa no rodapé) e log `TemaTV` com o pacote | EC-02 pede 4 s; `Toast` só tem 2 s ou 3,5 s | `Toast` | 🟢 |
| D-14 | Último app aberto guardado em `SharedPreferences` `launcher_prefs` (`last_focused_package`) | RF-13; sobrevive ao processo ser encerrado | Só em memória | 🟢 |
| D-15 | Tela "Tema" (`ThemeOptionsActivity`): diagnóstico com versão, tema ativo e origem, último erro e os textos "disponível em versão futura" | RF-11, RF-12, RN-05 | Diálogo sobre a tela inicial | 🟢 |
| D-16 | `ThemeRepository.reload()` passa a registrar no log o tempo da aplicação (`Tema aplicado: <id> em <N> ms`) | Medir o RNF de 3 s pendente da 001 | Medir só por cronômetro externo | 🟢 |
| D-17 | Banner do launcher (320×180) gerado pelo script de arte provisória da 001 | Mesmo processo da arte existente | Desenho manual | 🟡 |

## 4. Premissas

| Premissa | Origem (`requirements.md` seção) | Risco se errada |
|----------|----------------------------------|-----------------|
| A lista de utilitários escondidos (7 pacotes da TCL) cobre o que o cliente considera técnico | §4 RN-06; §9 | Algum app útil escondido ou utilitário visível; ajuste é trocar a constante |
| Os apps de TV relevantes têm banner; os que não têm ficam bem com o ícone centralizado | §5 RF-02 | Visual irregular na grade |
| Ficar fora da categoria HOME nesta feature não atrapalha o teste na TV | §1 | Nenhum: a tela é aberta pela lista de apps |

## 5. Delta arquitetural

| Componente | Arquivo de origem | Tipo de mudança | Resumo |
|------------|-------------------|-----------------|--------|
| `:app` / `br.tematv.launcher.home` | `_reversa_sdd/sdd/launcher-home.md` | componente-novo | `HomeActivity`, `ThemedAppTileView`, `AppGridAdapter`, `InstalledAppsSource`, `ThemeOptionsActivity` |
| `:app` / lógica pura da tela | `_reversa_sdd/sdd/launcher-home.md#6-requisitos-funcionais` | componente-novo | `AppListBuilder`, `HiddenSystemApps`, `VerseOfTheDay`, `SlotGeometry` |
| `ThemeRepository` (feature 001) | `_reversa_sdd/addenda/001-theme-engine.md` | regra-alterada | Só acrescenta o tempo de aplicação no log; comportamento igual |
| Manifesto do `:app` | `_reversa_sdd/sdd/launcher-home.md#8-design-e-interface` | contrato-novo | Atividade com `LEANBACK_LAUNCHER`, banner, `<queries>` |

## 6. Delta no modelo de dados

- Resumo das mudanças: nasce `launcher_prefs` com `last_focused_package`. O formato `.tematv` não muda.
- Detalhe completo em: `_reversa_forward/002-launcher-home/data-delta.md`

## 7. Delta de contratos externos

Nenhum contrato externo novo. A entrada na lista de apps do Google TV (`LEANBACK_LAUNCHER`) é um contrato de plataforma, registrado no manifesto e coberto pelo RF-15.

## 8. Plano de migração

n/a. A atualização do APK mantém os temas e o estado da feature 001.

## 9. Riscos e mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| Memória acima de 100 MB por causa do fundo 1920×1080 e dos banners | médio | médio | Fundo decodificado em RGB_565 quando não tem transparência; banners decodificados no tamanho do painel; medir com `dumpsys meminfo` na TCL |
| Rolagem travando na grade | médio | baixo | Uma View por item, sem layout aninhado; bitmaps da moldura compartilhados entre os itens |
| Banner de alguns apps com proporção estranha | baixo | médio | `SlotGeometry` sempre "cabe sem distorcer" |
| Foco se perdendo ao reconstruir a grade após instalação | médio | médio | Guardar o pacote em foco antes da atualização e restaurar depois (EC-01 move para o seguinte) |

## 10. Critério de pronto

- [ ] Todas as ações do `actions.md` marcadas `[X]`
- [ ] `./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug` passando
- [ ] Na TCL: tela temática com o tema bíblico e o hambúrguer (captura de tela), navegação, abertura de app, Voltar, diagnóstico, utilitários escondidos
- [ ] Medidas na TCL: abertura a frio ≤ 2 s, PSS ≤ 100 MB, aplicação de tema ≤ 3 s
- [ ] `regression-watch.md` gerado
- [ ] Commit na branch `feature/002-launcher-home` e criação da `feature/003-home-takeover`

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-plan` | reversa |
