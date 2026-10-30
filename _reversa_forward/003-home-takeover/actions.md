# Actions: Troca automática da tela inicial (home-takeover)

> Identificador: `003-home-takeover`
> Data: `2026-10-30`
> Roadmap: `_reversa_forward/003-home-takeover/roadmap.md`
> Branch: `feature/003-home-takeover`
> Raiz do código: `launcher/android/` (área liberada: `launcher/**`). Pacote novo: `br.tematv.launcher.takeover`.

## Resumo

| Métrica | Valor |
|---------|-------|
| Total de ações | 22 |
| Paralelizáveis (`[//]`) | 11 |
| Maior cadeia de dependência | 6 (T001 → T003 → T012 → T015 → T018 → T019) |

## Fase 1, Preparação

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T001 | Acrescentar os textos: nome e descrição do serviço, opções "Voltar ao launcher original do Google" e "Usar o tema como tela inicial", "Segure OK por 3 segundos", faixa "O tema não está abrindo sozinho. Veja em Tema.", passo a passo do serviço, estados ligada/desligada e rótulos do diagnóstico | - | `[//]` | `launcher/android/app/src/main/res/values/strings.xml` | 🟢 | `[X]` |
| T002 | Criar a configuração do serviço: `typeWindowStateChanged`, `packageNames` dos launchers do sistema, `canRetrieveWindowContent="false"`, `notificationTimeout="100"`, descrição (D-01, D-02) | - | `[//]` | `launcher/android/app/src/main/res/xml/home_takeover_service.xml` | 🟢 | `[X]` |
| T003 | Registrar o `HomeTakeoverService` no manifesto com `BIND_ACCESSIBILITY_SERVICE` e a configuração da T002 (RF-01) | T001, T002 | - | `launcher/android/app/src/main/AndroidManifest.xml` | 🟢 | `[X]` |

## Fase 2, Testes

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T004 | Testes da `TakeoverPolicy`: abre com troca ligada; ignora pacote que não é launcher, troca desligada, janela de 5 s e licença inválida; 4ª abertura em 10 s pausa 30 s; pausa expira e retoma; pausa não mexe na preferência; valores antigos de `elapsedRealtime` após reinício contam como expirados | - | `[//]` | `launcher/android/app/src/test/java/br/tematv/launcher/takeover/TakeoverPolicyTest.kt` | 🟢 | `[X]` |
| T005 | Testes da leitura da lista de serviços: `null`, vazia, só o nosso, vários separados por `:`, nome parecido que não é o nosso, forma curta `pacote/.Classe` | - | `[//]` | `launcher/android/app/src/test/java/br/tematv/launcher/takeover/AccessibilityServiceStateTest.kt` | 🟢 | `[X]` |
| T006 | Testes do `HoldToConfirm`: progresso proporcional, conclusão aos 3 s, soltar antes cancela, nova pressão recomeça do zero | - | `[//]` | `launcher/android/app/src/test/java/br/tematv/launcher/takeover/HoldToConfirmTest.kt` | 🟢 | `[X]` |

## Fase 3, Núcleo

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T007 | Criar `SystemLauncherPackages` e o ponto de licença `LicenseGate` com `AlwaysValidLicenseGate` (D-02, D-10, RF-12, RN-05) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/LicenseGate.kt` | 🟢 | `[X]` |
| T008 | Implementar `TakeoverPolicy` pura (D-03, D-04, RF-02, RF-08, RF-09, RF-12, RN-04) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/TakeoverPolicy.kt` | 🟢 | `[X]` |
| T009 | Implementar `AccessibilityServiceState`: interpretação pura da lista e leitura de `Settings.Secure` (D-09, RF-10) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/AccessibilityServiceState.kt` | 🟢 | `[X]` |
| T010 | Implementar `HoldToConfirm` puro (D-11, RF-13) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/HoldToConfirm.kt` | 🟢 | `[X]` |
| T011 | Implementar `TakeoverPreferences` sobre `takeover_prefs`: preferência, janelas, datas e as operações "desligar" (grava supressão de 5 s) e "religar" (D-08, D-12, RF-05, RF-06, RF-07, RF-08) | - | - | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/TakeoverPreferences.kt` | 🟢 | `[X]` |
| T012 | Implementar `HomeTakeoverService`: evento de janela → política → abre `HomeActivity` com as flags da D-07; trata pausa contra loop e falha ao abrir; no `onServiceConnected`, abre a tela se a TV ligou há menos de 3 min (D-05); registra tudo no log `TemaTV` (RF-02, RF-03, RF-04, RF-09) | T003, T007, T008, T011 | - | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/HomeTakeoverService.kt` | 🟡 | `[X]` |

## Fase 4, Integração

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T013 | Na `HomeActivity`, mostrar a faixa discreta no rodapé quando a troca está ligada e o serviço desligado, e registrar `last_service_off_at` (D-14, RF-10) | T009, T011 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/HomeActivity.kt` | 🟢 | `[X]` |
| T014 | Na `ThemeOptionsActivity` e no seu layout: opção de desligar com segurar OK 3 s e barra de progresso, opção de religar, estado do serviço com passo a passo e botão para as Configurações de acessibilidade, e diagnóstico com as datas de RF-11 | T009, T010, T011 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/ThemeOptionsActivity.kt` | 🟢 | `[X]` |
| T015 | Rodar `./gradlew :theme-core:test :app:testDebugUnitTest :app:assembleDebug` e corrigir até tudo passar | T004, T005, T006, T012, T013, T014 | - | `launcher/android/` | 🟢 | `[X]` |

## Fase 5, Polimento

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T016 | Criar o script do teste acelerado de ciclos (reinicializações com `adb reboot` e modo de espera com `KEYCODE_SLEEP`/`KEYCODE_WAKEUP`), que confere `mCurrentFocus` = `HomeActivity`, mede o tempo e imprime o total (D-15) | - | `[//]` | `launcher/android/tools/takeover_cycle_test.sh` | 🟡 | `[X]` |
| T017 | Atualizar o `README.md` com a troca da tela inicial, como ligar o serviço e como voltar ao launcher original | - | `[//]` | `launcher/android/README.md` | 🟢 | `[X]` |
| T018 | Teste funcional na TV seguindo o `onboarding.md` §2 e §3: ligar o serviço, Home em app volta à tela temática (medir o tempo), segurar OK 3 s desliga, toque curto não desliga, religar, faixa com o serviço desligado, diagnóstico. **Antes, avisar o usuário para ligar a TV e confirmar que ela responde.** | T015 | - | `launcher/android/app/build/outputs/apk/debug/app-debug.apk` | 🟡 | `[X]` |
| T019 | Rodar o teste acelerado de 30 ciclos (15 reinicializações e 15 saídas do modo de espera) e registrar o resultado nas Notas. **Avisar o usuário antes: a TV vai reiniciar várias vezes.** | T016, T018 | - | `_reversa_forward/003-home-takeover/actions.md` | 🟡 | `[X]` |

## Fase 6, Emenda: capa do tema e correção do segurar OK

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T020 | Corrigir o segurar OK: depois de desligar, ignorar as teclas até soltar o OK, para o "soltar" não virar clique que religa a troca | T014 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/ThemeOptionsActivity.kt` | 🟢 | `[X]` |
| T021 | Implementar a capa do tema no serviço (D-16, RF-14) e removê-la quando a `HomeActivity` ganha foco | T012 | - | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/ThemeCover.kt` | 🟢 | `[X]` |
| T022 | Rodar build e testes e repetir na TV o Home com a capa e o segurar OK com o controle (usuário) | T020, T021 | - | `launcher/android/` | 🟡 | `[X]` |

## Notas de execução

- **Commit:** ao fechar todas as ações, commitar na `feature/003-home-takeover` (junto com o adendo `_reversa_sdd/addenda/002-launcher-home.md`, pendente) e criar a `feature/004-license-activation`.

### Rodada 1 do `/reversa-coding` (2026-10-30)

- T015: 82 testes passando (39 do núcleo, 43 do app).
- **Achado 1, início automático da TCL:** com o serviço ligado, o Android não o vinculava. Log: `forbid bind service` e `TclAppBoot ... reason = callee_does't_have_OP_AUTO_START_permission`. Resolvido liberando a operação da TCL só para o app: `appops set br.tematv.launcher APP_AUTO_START allow` (D-17). A feature 6 precisa fazer isso na instalação.
- **Achado 2, atraso após o Home:** a tela temática só abria cerca de 4 s depois do Home (o Android segura trocas de app logo após o Home). Um protótipo tentou receber o botão Home pelo filtro de teclas do serviço, mas a TCL não entrega o Home a serviços de acessibilidade; o protótipo foi desfeito. Decisão do usuário: cobrir na hora com uma capa do tema (RF-14, D-16). Com a capa, a tela temática real abriu em menos de 1 s nas 3 rodadas (a janela visível do app também libera a abertura).
- **Achado 3, defeito do segurar OK:** depois de desligar, o "soltar" do OK virava clique e religava a troca. Corrigido (T020) e confirmado pelo usuário com o controle.
- T018 na TCL: serviço vinculado; Home a partir do YouTube abre a tela temática em menos de 1 s; toque curto mostra a dica; segurar OK por 3 s desliga (usuário, com o controle) e o Home fica no Google; religar pela tela "Tema"; com o serviço desligado aparecem a faixa discreta e o passo a passo.
- T019, teste acelerado de ciclos:
  - **15 de 15 reinicializações** com a tela temática voltando sozinha, de 0 a 7 s após o fim do boot (meta ≤ 10 s).
  - **Modo de espera, 6 de 6 válidos ok:** 1 ciclo automático e 5 ciclos feitos pelo usuário com o controle (partindo da tela temática e do YouTube; a TCL mostra a tela inicial ao acordar e a troca funcionou em todos).
  - 14 ciclos automáticos de espera ficaram inválidos: em espera, a TCL desliga a rede, e o ADB não consegue acordá-la. Não é falha do app. O script também foi corrigido para ler a tela ativa quando a TCL lista mais de uma tela.
  - Total válido: 21 ciclos, todos ok. Os 30 dias reais ficam para o cliente piloto.
- Observações para a feature 6: liberar `APP_AUTO_START`; ligar o serviço nas configurações seguras (contrato `interfaces/ativacao-acessibilidade-adb.md`); a TV em modo de espera fica fora da rede.

## Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-to-do` | reversa |
