# Actions: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`
> Roadmap: `_reversa_forward/006-device-provisioning/roadmap.md`
> Branch: `feature/006-device-provisioning`
> Raiz do código: `launcher/android/` (área liberada: `launcher/**`). Pacotes novos: `br.tematv.themebuilder.install` e `br.tematv.launcher.status`.
> Regra da TV: nenhum comando ADB antes da T023, e a T023 começa pedindo ao usuário para ligar a TV e confirmar.

## Resumo

| Métrica | Valor |
|---------|-------|
| Total de ações | 23 |
| Paralelizáveis (`[//]`) | 12 |
| Maior cadeia de dependência | 8 (T010 → T014 → T015 → T016 → T017 → T019 → T021 → T023) |

## Fase 1, Preparação

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T001 | Script que cria a chave definitiva (`~/.config/tematv/assinatura.jks` e `assinatura.properties`, senhas aleatórias, permissão `600`), recusa se já existirem e avisa da cópia de segurança (D-05, RF-15) | - | `[//]` | `launcher/android/tools/gerar-chave-assinatura.sh` | 🟢 | `[X]` |
| T002 | Assinatura de release lida de `assinatura.properties` (sem o arquivo, só as tarefas de release falham, com a orientação) e versão 6 / "0.6.0" (D-05, D-06) | - | `[//]` | `launcher/android/app/build.gradle.kts` | 🟢 | `[X]` |
| T003 | Gerar a chave definitiva neste notebook com o script | T001 | - | `~/.config/tematv/assinatura.jks` | 🟢 | `[X]` |
| T004 | Compilar `:app:assembleRelease` e conferir a assinatura com `apksigner`; conferir que debug, `:activation-tool` e `:theme-builder` compilam sem a chave | T002, T003 | - | `launcher/android/app/build.gradle.kts` | 🟡 | `[X]` |

## Fase 2, Testes

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T005 | Testes de `AccessibilityList`: lista `null` ou vazia, acrescentar sem duplicar, comparar o componente inteiro (não trecho), remover mantendo os outros, valor entre aspas simples com `'` e `$` protegidos | - | `[//]` | `launcher/android/theme-builder/src/test/kotlin/br/tematv/themebuilder/install/AccessibilityListTest.kt` | 🟢 | `[X]` |
| T006 | Testes das etapas com executor falso: autorização (`unauthorized` até `device`, tempo esgotado, `offline` reconecta), instalar (pula com versão e assinatura iguais, instala sem estado, `INSTALL_FAILED_UPDATE_INCOMPATIBLE` vira `assinatura-diferente`), início automático (allow, não se aplica, erro), código com repetição, tema e ativação conferidos pelo estado (senha pela entrada padrão e fora das mensagens), acessibilidade, conferência pelo foco, desfazer | - | `[//]` | `launcher/android/theme-builder/src/test/kotlin/br/tematv/themebuilder/install/InstallStepsTest.kt` | 🟢 | `[X]` |
| T007 | Testes do estado do app (`AppStatus` para JSON) e do nome `app_status.json` na importação | - | `[//]` | `launcher/android/app/src/test/java/br/tematv/launcher/status/AppStatusTest.kt` | 🟢 | `[X]` |

## Fase 3, Núcleo

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T008 | `AppStatus`: monta o JSON (versão, SHA-256 do certificado de assinatura, ativada, tema aplicado, horário) e o grava na pasta de importação (D-04) | - | `[//]` | `launcher/android/app/src/main/java/br/tematv/launcher/status/AppStatus.kt` | 🟢 | `[X]` |
| T009 | Publicar o estado ao fim de `reloadImports()` e aceitar `app_status.json` (só leitura) na importação | T008 | - | `launcher/android/app/src/main/java/br/tematv/launcher/TemaTvApplication.kt` | 🟢 | `[X]` |
| T010 | `CommandRunner` (comando, entrada opcional, tempo limite, saída e código de saída) e `Adb` (sempre `-s <ip>:5555`, `shell`, `content read/write` com repetição, `getprop`) (D-02) | - | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/Adb.kt` | 🟢 | `[X]` |
| T011 | `AccessibilityList`: junção e remoção por componente inteiro e o valor com aspas para o shell da TV (D-11) | - | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/AccessibilityList.kt` | 🟢 | `[X]` |
| T012 | `LauncherRelease`: lê `~/tematv/launcher/launcher.json` e o caminho do APK (D-07) | - | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/LauncherRelease.kt` | 🟢 | `[X]` |
| T013 | `InstallLog`: uma linha JSON por etapa em `~/tematv/instalacoes/<AAAA-MM-DD>-<ip>.log`, sem senha (D-15, RF-13, RN-05) | - | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/InstallLog.kt` | 🟢 | `[X]` |
| T014 | `InstallSteps`, primeira parte: TV encontrada (porta 5555 em 5 s), conectar e autorizar com dados da TV, instalar (pular, instalar, assinatura diferente) e desfazer (D-08, D-09, D-10, D-16) | T010, T011, T012 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/InstallSteps.kt` | 🟢 | `[X]` |
| T015 | `InstallSteps`, segunda parte: início automático, código do aparelho, tema e ativação conferidos pelo `app_status.json` (D-12, D-13) | T014 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/InstallSteps.kt` | 🟢 | `[X]` |
| T016 | `InstallSteps`, terceira parte: acessibilidade com conferência em `dumpsys accessibility` e conferência final pelo foco depois do Home (D-11, D-14) | T015 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/InstallSteps.kt` | 🟢 | `[X]` |

## Fase 4, Integração

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T017 | `LocalServer`: conjunto de threads e rotas de `interfaces/api-instalacao.md` (ambiente, verificar, etapas, desfazer), com registro de cada etapa (D-01, D-03) | T013, T016 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/LocalServer.kt` | 🟢 | `[X]` |
| T018 | `Main`: liga `Adb`, `LauncherRelease` e `InstallLog` aos caminhos de `~/tematv/` | T017 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/Main.kt` | 🟢 | `[X]` |
| T019 | Página: aba "Instalar na TV" (cliente com pacote, IP lembrado no navegador, aviso para ligar a TV, "Verificar TV", lista de etapas com tempo, aviso de autorização em destaque, "Tentar de novo a partir desta etapa", "Desfazer e instalar de novo", "Desfazer instalação" com confirmação) (RF-01 a RF-14) | T017 | - | `launcher/android/theme-builder/src/main/resources/web/install.js` | 🟢 | `[X]` |
| T020 | `instalar-tematv.sh`: com a chave presente, compila o release e copia APK e `launcher.json` (versão e assinatura pelo `apksigner`) para `~/tematv/launcher/`; sem a chave, avisa e instala só a ferramenta (D-07) | T004 | `[//]` | `launcher/android/tools/instalar-tematv.sh` | 🟢 | `[X]` |

## Fase 5, Polimento

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T021 | Rodar todos os testes, compilar debug e release e conferir que o segredo, a chave e as senhas da chave não estão em arquivos versionados | T009, T018, T019, T020 | - | `launcher/android/` | 🟢 | `[X]` |
| T022 | Seção da feature 006 no README: criar a chave, cópia de segurança, instalar a ferramenta, preparar a TV e usar a aba | T020 | `[//]` | `launcher/android/README.md` | 🟢 | `[X]` |
| T023 | **Parar e pedir ao usuário** que ligue a TV e confirme, lembrando que a primeira instalação desfaz o launcher de teste (D-17). Com a confirmação: instalar a ferramenta, rodar a instalação completa pela página na TCL (assinatura diferente → desfazer e instalar de novo → todas as etapas verdes) e repetir a instalação para ver "já instalado" | T021 | - | `_reversa_forward/006-device-provisioning/onboarding.md` | 🟡 | `[X]` |

## Notas de execução

### Rodada 1 do `/reversa-coding` (2026-10-30)

- T020/T021: 146 testes passando (39 do núcleo de temas, 13 de `:activation-core`, 49 de `:theme-builder`, 45 do app); debug e release compilam; o segredo de ativação e a senha da chave não aparecem em arquivos do repositório.
- T003: chave definitiva criada em `~/.config/tematv/assinatura.jks` (PKCS12, RSA 4096) e `assinatura.properties`, permissão `600`; SHA-256 do certificado `235db11a…2182`. Sem a chave, só `:app:assembleRelease` para (conferido).
- T019: a aba foi conferida no Chrome do Windows sem TV; na T023 a automação do navegador parou de responder (janela em segundo plano) e as etapas foram executadas pela mesma API que o botão "Instalar" chama.
- T023, com o usuário avisado e a TV ligada: o usuário perguntou se o texto ajustado da tela inicial tinha sido descartado. Não tinha: estava em `sample-themes/biblia-exemplo/theme.json` (commit `9a7e0a4`). Por escolha dele, a mensagem foi corrigida ("Recomeçar", "Senhor") e o tema da igreja foi recriado na ferramenta como o cliente `igreja-recomecar-em-cristo` (mensagem, destaque azul `#1E88E5`, logotipo e fundo do exemplo), que foi o instalado. O cliente de teste criado antes foi removido.
- T023, primeira passada: `instalar` acusou `assinatura-diferente` (APK de teste); "desfazer" removeu o launcher; instalação, início automático, código (novo: `5328-1392-3336`) e tema ok; **ativação falhou**. Causa: a senha chega pela importação com quebra de linha (como no contrato `ativacao-adb.md` da feature 004, `echo "$SENHA"`), e `ActivationDigits.normalize` recusava a quebra de linha sem registrar nada. Defeito da feature 004, que na TV só tinha sido instalada. Correção: `normalize` ignora espaços nas pontas (com testes) e a versão subiu para 7 / "0.6.1".
- T023, segunda passada: as 8 etapas ok (launcher 0.6.1 instalado, TV ativada, serviço ligado, tela temática na frente depois do Home). Terceira passada para idempotência: 15 s, `instalar` e `ativacao` puladas, a lista de acessibilidade continua com o serviço uma só vez, `APP_AUTO_START: allow`. O registro `~/tematv/instalacoes/2026-10-30-192.168.1.2.log` tem uma linha por etapa e não contém a senha nem os segredos.
- O usuário digitou na TV só os números da senha antiga (8065-4698-2216), recusada porque o código do aparelho mudou na reinstalação; digitar sem traços é aceito.

## Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-to-do` | reversa |
