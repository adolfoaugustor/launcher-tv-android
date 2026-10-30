# Actions: Ativação por licença (license-activation)

> Identificador: `004-license-activation`
> Data: `2026-10-30`
> Roadmap: `_reversa_forward/004-license-activation/roadmap.md`
> Branch: `feature/004-license-activation`
> Raiz do código: `launcher/android/` (área liberada: `launcher/**`). Módulos novos: `:activation-core`, `:activation-tool`. Pacote novo no app: `br.tematv.launcher.activation`.

## Resumo

| Métrica | Valor |
|---------|-------|
| Total de ações | 20 |
| Paralelizáveis (`[//]`) | 7 |
| Maior cadeia de dependência | 7 (T001 → T008 → T010 → T012 → T016 → T018 → T020) |

## Fase 1, Preparação

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T001 | Criar o módulo `:activation-core` (Kotlin JVM 17, JUnit) e incluí-lo no `settings.gradle.kts` (D-01) | - | - | `launcher/android/activation-core/build.gradle.kts` | 🟢 | `[X]` |
| T002 | Criar o módulo `:activation-tool` com o plugin `application` (classe principal `br.tematv.activationtool.MainKt`, executável `activation-tool`) dependendo de `:activation-core` (D-06) | T001 | - | `launcher/android/activation-tool/build.gradle.kts` | 🟢 | `[X]` |
| T003 | Acrescentar os textos do painel de ativação e do diagnóstico (código do aparelho, campo, "Ativar", senha incorreta, espera, "Ativada em", "Não ativada") | - | `[//]` | `launcher/android/app/src/main/res/values/strings.xml` | 🟢 | `[X]` |

## Fase 2, Testes

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T004 | Testes do código do aparelho e da senha: formato `XXXX-XXXX-XXXX`, zeros à esquerda, entrada com traços e espaços, entrada inválida, mesma entrada gera o mesmo resultado, códigos diferentes geram senhas diferentes, e vetores de teste fixos (D-13) | T001 | `[//]` | `launcher/android/activation-core/src/test/kotlin/br/tematv/activation/ActivationCodeTest.kt` | 🟢 | `[X]` |
| T005 | Testes de `ActivationAttempts`: 4 erros não bloqueiam, o 5º bloqueia 30 s, o bloqueio expira, acerto zera a contagem | T001 | `[//]` | `launcher/android/activation-core/src/test/kotlin/br/tematv/activation/ActivationAttemptsTest.kt` | 🟢 | `[X]` |

## Fase 3, Núcleo

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T006 | Implementar `DeviceCode` e `ActivationCode` (D-02, D-03, contrato `interfaces/senha-ativacao.md`) | T001 | `[//]` | `launcher/android/activation-core/src/main/kotlin/br/tematv/activation/ActivationCode.kt` | 🟢 | `[X]` |
| T007 | Implementar `ActivationAttempts` (D-11, RN-05) | T001 | `[//]` | `launcher/android/activation-core/src/main/kotlin/br/tematv/activation/ActivationAttempts.kt` | 🟢 | `[X]` |
| T008 | Implementar a ferramenta: `gerar-segredo` (recusa se já existir; grava com `600`; avisa da cópia de segurança) e `senha <código> [--cliente <nome>]` (imprime a senha e registra em `~/tematv/ativacoes.csv`) (RF-03, RF-04, RF-11) | T002, T006 | - | `launcher/android/activation-tool/src/main/kotlin/br/tematv/activationtool/Main.kt` | 🟢 | `[X]` |
| T009 | Gerar o segredo do revendedor neste notebook com a ferramenta | T008 | - | `~/.config/tematv/activation-secret` | 🟢 | `[X]` |
| T010 | No `:app`, ler o segredo na compilação para o `BuildConfig` (falha clara sem o arquivo) e depender de `:activation-core` (D-05) | T009 | - | `launcher/android/app/build.gradle.kts` | 🟢 | `[X]` |
| T011 | Implementar `ActivationStore` sobre `activation_prefs`: ativação, data, erros e espera; conferência contra o código do aparelho atual (D-07, RF-07) | T006, T007 | - | `launcher/android/app/src/main/java/br/tematv/launcher/activation/ActivationStore.kt` | 🟢 | `[X]` |
| T012 | Implementar `ActivationService` do app: código do aparelho a partir do `ANDROID_ID`, `tryActivate(senha)`, `isActivated()`, gravação de `device_code.txt` e importação de `activation_code.txt`; e o `ActivationLicenseGate` (D-08, D-12, RF-01, RF-02, RF-10) | T010, T011 | - | `launcher/android/app/src/main/java/br/tematv/launcher/activation/ActivationService.kt` | 🟢 | `[X]` |

## Fase 4, Integração

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T013 | Ampliar `ThemeImportNames` para `device_code.txt` e `activation_code.txt` e o teste correspondente | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeImportNames.kt` | 🟢 | `[X]` |
| T014 | Na recarga (`ReloadThemeReceiver`) e ao iniciar o app, gravar `device_code.txt` e importar `activation_code.txt` | T012, T013 | - | `launcher/android/app/src/main/java/br/tematv/launcher/theme/ReloadThemeReceiver.kt` | 🟢 | `[X]` |
| T015 | Na troca da tela inicial: a `TakeoverPolicy` deixa de consultar a licença (e seus testes), o serviço usa o `ActivationLicenseGate` só para a capa sem logotipo, e o `AlwaysValidLicenseGate` sai (D-08, D-09, RN-04, RF-09) | T012 | - | `launcher/android/app/src/main/java/br/tematv/launcher/takeover/TakeoverPolicy.kt` | 🟢 | `[X]` |
| T016 | Painel de ativação na `HomeActivity` e no layout: código em destaque, campo numérico, "Ativar", erro, espera de 30 s, troca para a grade ao ativar (D-10, RF-05, RF-06, RF-08) | T003, T012 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/HomeActivity.kt` | 🟢 | `[X]` |
| T017 | Diagnóstico da ativação na tela "Tema" (RF-12) | T003, T012 | - | `launcher/android/app/src/main/java/br/tematv/launcher/home/ThemeOptionsActivity.kt` | 🟢 | `[X]` |
| T018 | Rodar `./gradlew :theme-core:test :activation-core:test :app:testDebugUnitTest :app:assembleDebug :activation-tool:installDist` e corrigir até tudo passar | T004, T005, T008, T014, T015, T016, T017 | - | `launcher/android/` | 🟢 | `[X]` |

## Fase 5, Polimento

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T019 | Atualizar o `README.md` com a ativação, o segredo (cópia de segurança) e a ferramenta | - | `[//]` | `launcher/android/README.md` | 🟢 | `[X]` |
| T020 | **Só instalar** o APK na TV de teste (`adb install -r`, sem mexer no app em execução), ler o código do aparelho pela importação do ADB e entregar ao usuário a senha daquela TV para ele testar depois. **Antes, avisar o usuário.** | T018 | - | `launcher/android/app/build/outputs/apk/debug/app-debug.apk` | 🟡 | `[X]` |

## Notas de execução

- **TV:** por pedido do usuário (2026-10-30), a TV só recebe a instalação; os testes na TV ficam com ele.
- **Commit:** ao fechar todas as ações, commitar na `feature/004-license-activation` (junto com os versículos NVI e os adendos pendentes) e criar a `feature/005-theme-builder`.

### Rodada 1 do `/reversa-coding` (2026-10-30)

- T018: 94 testes passando (39 do núcleo de temas, 13 de `:activation-core`, 42 do app). Os vetores fixos de `ActivationCodeTest` foram calculados de forma independente em Python e bateram.
- T009: segredo do revendedor gerado em `~/.config/tematv/activation-secret` (permissão 600). Conferido: o valor não aparece em nenhum arquivo versionável; só no `BuildConfig` gerado na compilação.
- A `TakeoverPolicy` deixou de consultar a licença; o teste de licença inválida da feature 003 saiu, porque agora o Home sem ativação abre a tela de ativação (RN-04).
- T020: por pedido do usuário, a TV só recebeu o `adb install -r`, sem abrir telas. O código do aparelho da TV de teste foi lido pela importação do ADB: `2544-3298-7734`. A senha foi gerada com o `activation-tool` (registrada em `~/tematv/ativacoes.csv`) e entregue ao usuário. Os testes na TV ficam com ele.

## Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-to-do` | reversa |
