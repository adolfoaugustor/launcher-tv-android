# Actions: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`
> Roadmap: `_reversa_forward/005-theme-builder/roadmap.md`
> Branch: `feature/005-theme-builder`
> Raiz do código: `launcher/android/` (área liberada: `launcher/**`). Módulo novo: `:theme-builder` (pacote `br.tematv.themebuilder`).

## Resumo

| Métrica | Valor |
|---------|-------|
| Total de ações | 22 |
| Paralelizáveis (`[//]`) | 14 |
| Maior cadeia de dependência | 9 (T001 → T003 → T008 → T011 → T014 → T016 → T017 → T020 → T021) |

## Fase 1, Preparação

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T001 | Criar o módulo `:theme-builder` (Kotlin JVM 17, plugin `application`, classe principal `br.tematv.themebuilder.MainKt`, executável `tematv`) dependendo de `:theme-core`, `:activation-core`, `kotlinx-serialization-json` e JUnit, e incluí-lo no `settings.gradle.kts` (D-01) | - | - | `launcher/android/theme-builder/build.gradle.kts` | 🟢 | `[X]` |
| T002 | Sem o arquivo do segredo, a configuração do `:app` não falha mais: o `BuildConfig` fica sem valor e a tarefa `preBuild` falha com a mesma mensagem; conferir que `:activation-tool:installDist` compila com `-Ptematv.activationSecretFile=/nao-existe` (D-11) | - | `[//]` | `launcher/android/app/build.gradle.kts` | 🟢 | `[X]` |
| T003 | Tarefa Gradle que copia para os recursos gerados do `:theme-builder` o `default-theme/theme.json` do `:app` e, de `sample-themes/`, as molduras `frame-biblia.png` e `frame-hamburguer.png` com os `theme.json` dos exemplos (D-06) | T001 | - | `launcher/android/theme-builder/build.gradle.kts` | 🟢 | `[X]` |

## Fase 2, Testes

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T004 | Testes de `ProjectStore`: criar igreja e loja (lista inicial e moldura sugerida), gravar e reler, `theme_id` repetido recusado, `theme_id` fora do formato, mensagem acima de 120, 367º item, `updated_at` atualizado | T001 | `[//]` | `launcher/android/theme-builder/src/test/kotlin/br/tematv/themebuilder/ProjectStoreTest.kt` | 🟢 | `[X]` |
| T005 | Testes de `PackageBuilder`: pacote de igreja aceito pelo `ThemePackageLoader` com 20 versículos `NVI` e a atribuição da NVI; loja com `LOJA` e a atribuição do projeto; lista vazia recusada sem deixar arquivo em `temas/`; fundo de 4000×3000 reduzido para até 1920×1080; WEBP grande recusado; logotipo não PNG recusado | T001 | `[//]` | `launcher/android/theme-builder/src/test/kotlin/br/tematv/themebuilder/PackageBuilderTest.kt` | 🟢 | `[X]` |
| T006 | Testes de `ResellerActivation`: vetores fixos do `ActivationCodeTest`, código com traços e espaços, código inválido, segredo ausente, linha de `ativacoes.csv` no mesmo formato do `activation-tool` (incluindo nome com vírgula) | T001 | `[//]` | `launcher/android/theme-builder/src/test/kotlin/br/tematv/themebuilder/ResellerActivationTest.kt` | 🟢 | `[X]` |
| T007 | Testes de `LocalRequestGuard`: `Host` `localhost:<porta>` e `127.0.0.1:<porta>` aceitos, outro `Host` recusado, escrita sem `application/json` recusada, `Origin` de outro endereço recusado, `Origin` ausente aceito | T001 | `[//]` | `launcher/android/theme-builder/src/test/kotlin/br/tematv/themebuilder/LocalRequestGuardTest.kt` | 🟢 | `[X]` |

## Fase 3, Núcleo

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T008 | `Presets`: molduras prontas (`biblia`, `hamburguer`: imagem e áreas), lista NVI inicial e atribuição da NVI lidas dos recursos copiados (D-06, D-10) | T003 | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/Presets.kt` | 🟢 | `[X]` |
| T009 | `ClientProject` (serializável, conforme `data-delta.md` §1.1) e `ProjectStore` em `~/tematv/clientes/<theme_id>/` com escrita atômica, criação por tipo e validação de campos com mensagens em português (D-09, D-10, RF-02, RF-03, RF-05, RF-11) | T001 | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/ProjectStore.kt` | 🟢 | `[X]` |
| T010 | `ImageProcessor`: confere tipo pelo `ImageHeaderReader`, logotipo PNG até 5 MB, reduz PNG e JPEG acima de 1920×1080 mantendo a proporção, aceita WEBP só até 1920×1080 (D-07, RF-09) | T001 | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/ImageProcessor.kt` | 🟢 | `[X]` |
| T011 | `PackageBuilder`: monta `theme.json` v1 e as imagens num ZIP temporário em `~/tematv/temas/`, valida com `ThemePackageLoader.load()` numa pasta temporária, renomeia para `<theme_id>.tematv` só se aceito e devolve o `ThemeError` se recusado (D-05, RF-08, RF-10) | T008, T009, T010 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/PackageBuilder.kt` | 🟢 | `[X]` |
| T012 | `ResellerActivation`: lê o segredo de `~/.config/tematv/activation-secret` a cada pedido, gera a senha com `ActivationCode` e registra em `~/tematv/ativacoes.csv` (D-08, RF-12, RF-13, RN-04) | T001 | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/ResellerActivation.kt` | 🟢 | `[X]` |
| T013 | `LocalRequestGuard`: regras de `Host`, `Origin` e `Content-Type` da D-03 | T001 | `[//]` | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/LocalRequestGuard.kt` | 🟢 | `[X]` |

## Fase 4, Integração

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T014 | `LocalServer`: `HttpServer` em `127.0.0.1`, arquivos da página e rotas de `interfaces/api-local.md` (clientes, imagens, pacote, presets, ativação) passando pelo `LocalRequestGuard`, com erros `{error, field}` (D-02, D-03, D-04) | T011, T012, T013 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/LocalServer.kt` | 🟢 | `[X]` |
| T015 | `Main`: escolhe a porta de 8787 a 8797, mostra a URL no terminal, abre o navegador (`cmd.exe /c start` no WSL, `xdg-open` fora) e registra pacote gerado e erros sem segredo nem senha (D-02, D-13, RF-01) | T014 | - | `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/Main.kt` | 🟢 | `[X]` |
| T016 | Página: telas "Clientes" (lista e "Novo cliente" com o tipo) e "Projeto" (formulário, envio de imagens em base64, lista de versículos ou mensagens com editar, remover, reordenar e acrescentar, erro ao lado do campo) (RF-02 a RF-06, RF-11) | T014 | - | `launcher/android/theme-builder/src/main/resources/web/app.js` | 🟢 | `[X]` |
| T017 | Prévia 16:9 da tela inicial com as medidas do `activity_home.xml` (cabeçalho com logotipo e mensagem, versículo, grade de apps de exemplo na moldura), atualizada a cada alteração (RF-07) | T016 | `[//]` | `launcher/android/theme-builder/src/main/resources/web/preview.js` | 🟡 | `[X]` |
| T018 | Tela "Ativar TV" (código, cliente, senha em destaque, orientação sem segredo) e resultado de "Gerar pacote" com caminho, tamanho e comandos do ADB para copiar (RF-12, RF-13, RF-14) | T016 | `[//]` | `launcher/android/theme-builder/src/main/resources/web/activation.js` | 🟢 | `[X]` |
| T019 | Script de instalação: confere o JDK 17, compila `:theme-builder:installDist` e cria `~/.local/bin/tematv` (D-12, RNF-05) | T015 | `[//]` | `launcher/android/tools/instalar-tematv.sh` | 🟡 | `[X]` |

## Fase 5, Polimento

| ID | Descrição | Dependências | Paralelismo | Arquivo alvo | Confidência | Status |
|----|-----------|--------------|-------------|--------------|-------------|--------|
| T020 | Rodar todos os testes (`:theme-core`, `:activation-core`, `:theme-builder`, `:app`) e compilar o APK com o segredo para conferir que nada da TV mudou | T015, T017, T018, T019 | - | `launcher/android/` | 🟢 | `[X]` |
| T021 | Roteiro do `onboarding.md` no notebook: instalar, abrir a página no navegador do Windows, gerar um pacote de igreja e um de loja, conferir a senha `8065-4698-2216` para o código `2544-3298-7734`; nada na TV (D-14) | T020 | - | `_reversa_forward/005-theme-builder/onboarding.md` | 🟡 | `[X]` |
| T022 | Seção da feature 005 no README: instalar, abrir `tematv`, gerar pacote e ativar TV pela página | T015 | `[//]` | `launcher/android/README.md` | 🟢 | `[X]` |

## Notas de execução

### Rodada 1 do `/reversa-coding` (2026-10-30)

- T020: 121 testes passando (39 do núcleo de temas, 13 de `:activation-core`, 27 de `:theme-builder`, 42 do app). O APK continua com o segredo embutido; a mudança da D-11 não alterou o app.
- T002 (D-11) conferido: `-Ptematv.activationSecretFile=/nao-existe :activation-tool:installDist` compila, e `:app:preBuild` falha com a orientação.
- T021: a premissa do WSL2 falhou na primeira tentativa. O Java abria o socket como IPv6 mapeado (`::ffff:127.0.0.1`), que o encaminhamento de `localhost` do WSL2 não enxerga (um servidor Python em `127.0.0.1` funcionava). Correção: `-Djava.net.preferIPv4Stack=true` no comando `tematv` (`theme-builder/build.gradle.kts`). Depois disso, a página abriu no Chrome do Windows.
- T021, no navegador do Windows: cliente de igreja editado e salvo, cliente de loja criado pela página (moldura de hambúrguer, atribuição da loja), pacote gerado pela página com os comandos do ADB, e "Ativar TV" mostrando `8065-4698-2216` para `2544 3298 7734`, igual ao `activation-tool`. Os testes usaram uma pasta pessoal temporária, sem tocar em `~/tematv/` nem na TV (D-14).
- T021 achou um erro corrigido na hora: "Gerar pacote" enviava o pedido sem corpo e a proteção da D-03 o recusava ("Envie os dados em JSON"); a página passou a enviar `{}`.
- T019: script testado com uma pasta pessoal temporária (instala em `~/.local/share/tematv` e cria `~/.local/bin/tematv`). A instalação real no notebook fica com o usuário. A abertura automática do navegador (`cmd.exe /c start`) não foi exercitada, para não abrir janelas sem aviso; a URL sempre aparece no terminal.
- A escolha do logotipo pelo seletor de arquivos não foi testada no navegador (a automação não opera a janela de arquivos); o envio foi testado pela API e pelos testes de `ImageProcessor`.

## Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-to-do` | reversa |
