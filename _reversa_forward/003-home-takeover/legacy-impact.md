# Legacy impact: Troca automática da tela inicial (home-takeover)

> Identificador: `003-home-takeover`
> Data: `2026-10-30`
> Feature greenfield, sem legado pré-existente. Âncora: `_reversa_sdd/prd.md` + specs SDD (`_reversa_sdd/sdd/home-takeover.md`) + adendos vigentes `001-theme-engine.md` e `002-launcher-home.md`.
> Política de edição no momento da execução: `allowLegacyEdits: true`, `allowedPaths: ["launcher/**"]`. Todos os arquivos tocados casam com `launcher/**`.
> Estado: concluída. 22 de 22 ações (19 do plano e 3 da emenda T020 a T022); 82 testes automatizados passando; teste funcional e teste acelerado de ciclos na TV de referência (resultado nas Notas do `actions.md`).

## Arquivos afetados

| Arquivo afetado | Componente | Tipo | Severidade | Justificativa |
|-----------------|------------|------|------------|---------------|
| `launcher/android/app/src/main/AndroidManifest.xml` | `:app` | contrato-novo | HIGH | Serviço de acessibilidade com `BIND_ACCESSIBILITY_SERVICE` |
| `launcher/android/app/src/main/res/xml/home_takeover_service.xml` | `:app` (D-01, D-02) | componente-novo | HIGH | Escopo do serviço: só troca de janela dos launchers do sistema, sem conteúdo de tela |
| `launcher/android/app/src/main/res/values/strings.xml`, `themes.xml` | `:app` | componente-novo | LOW | Textos e estilos da troca e do diagnóstico |
| `.../takeover/LicenseGate.kt` | `:app` (D-02, D-10, RF-12) | componente-novo | MEDIUM | Ponto de licença e lista de launchers do sistema |
| `.../takeover/TakeoverPolicy.kt` | `:app` (D-03, D-04, RF-02, RF-08, RF-09) | componente-novo | HIGH | Regras de quando abrir, ignorar ou pausar |
| `.../takeover/TakeoverPreferences.kt` | `:app` (D-08) | componente-novo | MEDIUM | `takeover_prefs` |
| `.../takeover/AccessibilityServiceState.kt` | `:app` (D-09, RF-10) | componente-novo | LOW | Se o serviço está ligado |
| `.../takeover/HoldToConfirm.kt` | `:app` (D-11, RF-13) | componente-novo | LOW | Segurar OK por 3 s |
| `.../takeover/HomeTakeoverService.kt` | `:app` (RF-01 a RF-04) | componente-novo | HIGH | Serviço que abre a tela temática |
| `.../takeover/ThemeCover.kt` | `:app` (D-16, RF-14) | componente-novo | MEDIUM | Capa do tema sobre a tela do Google |
| `.../home/HomeActivity.kt`, `res/layout/activity_home.xml` | `:app` (feature 002) | regra-alterada | LOW | Faixa de serviço desligado; avisa o serviço para tirar a capa |
| `.../home/ThemeOptionsActivity.kt`, `res/layout/activity_theme_options.xml` | `:app` (feature 002) | regra-alterada | MEDIUM | Ligar e desligar a troca, segurar OK, passo a passo e diagnóstico real |
| `launcher/android/app/src/test/java/.../takeover/*Test.kt` | `:app` | componente-novo | LOW | Testes da política, do estado do serviço e do segurar OK |
| `launcher/android/tools/takeover_cycle_test.sh` | Ferramenta de teste | componente-novo | LOW | Teste acelerado de ciclos |
| `launcher/android/README.md` | Documentação | componente-novo | LOW | Troca da tela inicial |

## Diff conceitual por componente

- **`:app` / `br.tematv.launcher.takeover`:** nasce a troca automática da tela inicial. O serviço observa só as janelas dos launchers do sistema, decide pela `TakeoverPolicy` e abre a `HomeActivity`, cobrindo a tela do Google com a capa do tema no mesmo instante.
- **Telas da feature 002:** a tela inicial ganha a faixa discreta de serviço desligado e avisa o serviço quando fica visível; a tela "Tema" troca os "disponível em versão futura" de troca e acessibilidade pelos controles e estados reais.
- **Requisito de instalação na TCL:** o app precisa da operação `APP_AUTO_START` liberada (D-17), além do serviço ligado nas configurações seguras.

## Preservadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente. As regras das features 001 e 002 continuam válidas.

## Modificadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.
