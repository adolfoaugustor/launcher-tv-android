# Roadmap: Troca automática da tela inicial (home-takeover)

> Identificador: `003-home-takeover`
> Data: `2026-10-30`
> Requirements: `_reversa_forward/003-home-takeover/requirements.md`
> Branch: `feature/003-home-takeover`
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA

## 1. Resumo da abordagem

Delta sobre o app das features 001 e 002 (`_reversa_sdd/addenda/001-theme-engine.md`, `_reversa_sdd/addenda/002-launcher-home.md`). Entra um pacote novo, `br.tematv.launcher.takeover`, com:
- um serviço de acessibilidade (`HomeTakeoverService`) que recebe só eventos de troca de janela dos pacotes de launcher do sistema e abre a `HomeActivity` quando a troca está ligada;
- uma política pura e testável (`TakeoverPolicy`), que decide abrir ou ignorar considerando a preferência, a janela de 5 s após desligar, a trava contra loop (3 aberturas em 10 s pausam 30 s) e a licença;
- as preferências da troca (`takeover_prefs`);
- a leitura do estado do serviço nas configurações do Android.

O boot é coberto pelo próprio serviço: ao ser conectado logo após o boot, ele abre a tela temática. Não há receptor de `BOOT_COMPLETED`, porque o Android 10+ bloqueia abrir atividades a partir dele, mas permite a partir de um serviço de acessibilidade ligado. A tela "Tema" ganha as opções de ligar e desligar (desligar exige segurar OK por 3 s) e o diagnóstico real. A tela inicial ganha a faixa discreta de serviço desligado. A categoria `HOME` continua fora do manifesto: na TCL, o launcher do Google tem prioridade 2 e um app comum só tem 0, então declarar não muda nada (OQ-02 respondida).

## 2. Princípios aplicados

`.reversa/principles.md` não existe. Valem as regras globais do usuário.

| Princípio | Como a feature se relaciona | Status |
|-----------|------------------------------|--------|
| Falhar cedo | Falha ao abrir a tela registra no log e no diagnóstico; nada é engolido | respeita |
| YAGNI | Sem receptor de boot nem leitura de conteúdo de tela; só o que a troca precisa | respeita |

## 3. Decisões técnicas

| ID | Decisão | Justificativa | Alternativas descartadas | Confidência |
|----|---------|----------------|--------------------------|-------------|
| D-01 | `HomeTakeoverService` (`AccessibilityService`) configurado com `typeWindowStateChanged`, `packageNames` restrito aos launchers do sistema, `canRetrieveWindowContent="false"` e `notificationTimeout` de 100 ms | RF-01, RNF de privacidade e consumo | Ler janelas com `getWindows()` (exige acesso ao conteúdo) | 🟢 |
| D-02 | `SystemLauncherPackages`: `com.google.android.apps.tv.launcherx` (Google TV), `com.google.android.tvlauncher` e `com.google.android.leanbacklauncher` (Android TV mais antigos) | RF-02; EC-03 (outras marcas) | Detectar dinamicamente pelo HOME padrão (o serviço não pode mudar `packageNames` sem reconfigurar) | 🟡 |
| D-03 | `TakeoverPolicy` pura: entrada (pacote, preferência, licença, instante monotônico, janelas e histórico de aberturas) e saída `Open`, `Ignore(motivo)` ou `PauseForLoop` | RF-02, RF-08, RF-09, RF-12; testável na JVM | Lógica dentro do serviço (sem teste) | 🟢 |
| D-04 | Instantes da política com `SystemClock.elapsedRealtime()` (monotônico); datas do diagnóstico com `Instant` | Janelas de 5 s e 30 s não podem quebrar com mudança de relógio | `System.currentTimeMillis()` | 🟢 |
| D-05 | Boot: em `onServiceConnected`, se a troca estiver ligada e o aparelho tiver ligado há menos de 3 min, o serviço abre a `HomeActivity` | O Android 10+ bloqueia abrir atividade a partir de `BOOT_COMPLETED`; serviço de acessibilidade ligado é exceção da regra | Receptor de `BOOT_COMPLETED`; serviço em primeiro plano | 🟡 |
| D-06 | Saída do modo de espera: coberta pelo mesmo evento de janela quando a TV volta mostrando o launcher do Google | RF-04; se a TV voltar direto num app, não há troca (só quando o launcher do sistema aparece) | Receptor de `SCREEN_ON` (não sabe o que está na tela) | 🟡 |
| D-07 | A `HomeActivity` é aberta pelo serviço com `FLAG_ACTIVITY_NEW_TASK`, `CLEAR_TOP` e `SINGLE_TOP` | `singleTask` já declarado; evita empilhar instâncias | Abrir sem flags | 🟢 |
| D-08 | Preferências `takeover_prefs`: `takeover_enabled` (padrão `true`), `suppress_until_elapsed_ms`, `paused_until_elapsed_ms`, `last_takeover_at`, `last_service_off_at`, `last_loop_pause_at`, `last_open_failure_at` | RF-05, RF-08, RF-09, RF-11 | Banco de dados | 🟢 |
| D-09 | Estado do serviço lido de `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` por uma função pura que interpreta a lista separada por `:` | RF-10; `AccessibilityManager` pode atrasar após mudanças | Só `AccessibilityManager` | 🟢 |
| D-10 | Ponto de licença `LicenseGate` (`fun interface`) com implementação `AlwaysValidLicenseGate` nesta feature | RF-12, RN-05; a feature 004 troca a implementação | Ignorar a licença agora | 🟢 |
| D-11 | Segurar OK 3 s: `HoldToConfirm` puro calcula progresso e conclusão; a tela usa `onKeyDown`/`onKeyUp` de `DPAD_CENTER` e `ENTER` com uma barra de progresso | RF-13; testável na JVM | `onLongClick` (dispara em ~0,5 s, não 3 s) | 🟢 |
| D-12 | Desligar a troca: grava `takeover_enabled=false`, `suppress_until = agora + 5 s` e abre `Intent(MAIN, HOME)`, que resolve para o launcher do Google | RF-06, RF-08; religar (RF-07) só grava `takeover_enabled=true` e fica na tela temática | Abrir o pacote do Google por nome (quebra em outras marcas) | 🟢 |
| D-13 | Sem categoria `HOME` no manifesto | Na TCL o launcher do Google tem prioridade 2 e app comum tem 0: declarar não muda a resolução (verificado em 2026-10-30) | Declarar `HOME` | 🟢 |
| D-14 | Faixa discreta na tela inicial e passo a passo em "Tema" quando a troca está ligada e o serviço desligado; registra `last_service_off_at` | RF-10; esclarecimento de 2026-10-30 | Faixa completa no topo | 🟢 |
| D-16 | Capa do tema: o serviço adiciona na hora uma janela `TYPE_ACCESSIBILITY_OVERLAY` (não focável, não tocável) com o fundo e o logotipo do tema ativo, e a remove quando a `HomeActivity` ganha foco ou após 8 s | RF-14; janelas de sobreposição de acessibilidade não passam pela proteção de troca de app | Capturar o botão Home pelo filtro de teclas (testado: a TCL não entrega o Home ao serviço); aceitar os ~4 s | 🟢 |
| D-17 | Liberar a operação `APP_AUTO_START` da TCL para o app (`appops set br.tematv.launcher APP_AUTO_START allow`) | Sem ela, o gerenciador TclAppBoot proíbe o Android de vincular o serviço (`forbid bind service`, motivo `callee_does't_have_OP_AUTO_START_permission`) | Nenhuma | 🟢 |
| D-15 | Teste acelerado de 30 ciclos por script `tools/takeover_cycle_test.sh` (reinicializações com `adb reboot` e modo de espera com `KEYCODE_SLEEP`/`KEYCODE_WAKEUP`), conferindo `mCurrentFocus` = `HomeActivity` | RNF de confiabilidade; esclarecimento de 2026-10-30 | Teste manual sem registro | 🟡 |

## 4. Premissas

| Premissa | Origem (`requirements.md` seção) | Risco se errada |
|----------|----------------------------------|-----------------|
| O serviço de acessibilidade ligado pode abrir atividades em segundo plano (exceção do Android 10+) | §5 RF-02, RF-03 | A troca não abre a tela; seria preciso outra estratégia (por exemplo, sobreposição) |
| Ao sair do modo de espera, a TCL volta mostrando o launcher do Google | §5 RF-04 | A troca só acontece no próximo Home; registrar como limitação |
| O serviço é conectado logo após o boot na TCL | §5 RF-03 | A primeira tela após o boot seria a do Google até o primeiro Home |

## 5. Delta arquitetural

| Componente | Arquivo de origem | Tipo de mudança | Resumo |
|------------|-------------------|-----------------|--------|
| `:app` / `br.tematv.launcher.takeover` | `_reversa_sdd/sdd/home-takeover.md` | componente-novo | `HomeTakeoverService`, `TakeoverPolicy`, `TakeoverPreferences`, `AccessibilityServiceState`, `SystemLauncherPackages`, `LicenseGate`, `HoldToConfirm` |
| `HomeActivity` (feature 002) | `_reversa_sdd/addenda/002-launcher-home.md` | regra-alterada | Faixa discreta quando o serviço está desligado |
| `ThemeOptionsActivity` (feature 002) | `_reversa_sdd/addenda/002-launcher-home.md` | regra-alterada | Opções de ligar e desligar a troca, segurar OK 3 s, passo a passo do serviço e diagnóstico real |
| Manifesto do `:app` | `_reversa_sdd/sdd/home-takeover.md#8-design-e-interface` | contrato-novo | Serviço de acessibilidade com `BIND_ACCESSIBILITY_SERVICE` e configuração XML |

## 6. Delta no modelo de dados

- Resumo das mudanças: nasce `takeover_prefs` (preferência e datas da troca). Nada muda no tema nem em `launcher_prefs`.
- Detalhe completo em: `_reversa_forward/003-home-takeover/data-delta.md`

## 7. Delta de contratos externos

| Contrato | Tipo | Arquivo de detalhe |
|----------|------|--------------------|
| Ligar o serviço de acessibilidade pelo ADB (usado pela feature 006) | configuração do Android (`settings`) | `_reversa_forward/003-home-takeover/interfaces/ativacao-acessibilidade-adb.md` |

## 8. Plano de migração

n/a. Na atualização do APK, `takeover_enabled` nasce ligado; a troca só acontece depois que o serviço é ligado (pelo ADB ou nas Configurações).

## 9. Riscos e mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| O Android ou o Google TV desliga o serviço após atualização | alto | média | Faixa discreta e passo a passo (RF-10); registro de `last_service_off_at`; teste de 30 ciclos |
| O launcher do Google "pisca" antes da tela temática | médio | alta | Meta de ≤ 1,5 s; evento com `notificationTimeout` baixo; aceito pelo PRD |
| Loop entre a tela temática e a do Google | alto | baixa | Trava de 3 aberturas em 10 s com pausa de 30 s (RF-09) |
| Background activity start bloqueado em alguma versão | alto | baixa | Registro de falha no diagnóstico (EC-05) e teste na TCL |

## 10. Critério de pronto

- [ ] Todas as ações do `actions.md` marcadas `[X]`
- [ ] `./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug` passando
- [ ] Na TCL: Home em app volta à tela temática em ≤ 1,5 s; desligar segurando OK 3 s; religar; faixa de serviço desligado
- [ ] Teste acelerado de 30 ciclos sem falha
- [ ] `regression-watch.md` gerado
- [ ] Commit na `feature/003-home-takeover` e criação da `feature/004-license-activation`

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-plan` | reversa |
