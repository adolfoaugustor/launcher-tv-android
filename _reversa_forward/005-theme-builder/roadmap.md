# Roadmap: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`
> Requirements: `_reversa_forward/005-theme-builder/requirements.md`
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA

## 1. Resumo da abordagem

A ferramenta vira um módulo novo `:theme-builder` (Kotlin, JVM) no mesmo projeto Gradle do launcher. Um servidor HTTP do próprio JDK, preso a `127.0.0.1`, serve uma página estática (HTML, CSS e JavaScript sem etapa de build) e uma API JSON local. A geração do pacote monta o `.tematv` num arquivo temporário e o valida com o próprio `ThemePackageLoader` do `:theme-core`, o mesmo caminho do launcher; só depois o arquivo recebe o nome final. A senha de ativação usa `ActivationCode` do `:activation-core`, lendo o segredo do arquivo do revendedor a cada pedido. A lista NVI e as molduras prontas são copiadas pela compilação a partir dos temas já existentes, sem uma terceira cópia à mão. A prévia é desenhada no navegador, reproduzindo a tela inicial da feature 002. Um script instala tudo e cria o comando `tematv`. A compilação do `:app` deixa de exigir o segredo para compilar os módulos do notebook.

## 2. Princípios aplicados

Não há `.reversa/principles.md` no projeto. As regras de desenvolvimento do usuário (YAGNI, DRY na terceira ocorrência, falhar cedo, simples primeiro) guiam D-01, D-05, D-08 e D-11.

| Princípio | Como a feature se relaciona | Status |
|-----------|------------------------------|--------|
| n/a | Sem `principles.md` | n/a |

## 3. Decisões técnicas

| ID | Decisão | Justificativa | Alternativas descartadas | Confidência |
|----|---------|----------------|--------------------------|-------------|
| D-01 | Módulo `:theme-builder` (Kotlin JVM, plugin `application`, comando `tematv`), dependendo de `:theme-core`, `:activation-core` e `kotlinx-serialization-json` | RN-01: validar com o código do launcher; mesma linguagem e testes das features anteriores | Python (reimplementaria a validação), Node com build de front-end | 🟢 |
| D-02 | Servidor `com.sun.net.httpserver.HttpServer` do JDK em `127.0.0.1`, porta 8787 ou a próxima livre até 8797 | RF-01, RN-03; sem dependência nova | Ktor, Jetty | 🟢 |
| D-03 | Proteção contra outros sites no mesmo navegador: recusar pedidos cujo `Host` não seja `localhost:<porta>` ou `127.0.0.1:<porta>` e pedidos de escrita com `Origin` de outro endereço ou sem `Content-Type: application/json` | Prender a `127.0.0.1` não impede um site aberto no navegador de chamar a porta (incluindo DNS rebinding); a senha de ativação é sensível | Token na URL | 🟢 |
| D-04 | Página estática em `src/main/resources/web/` (HTML, CSS e JavaScript puro); imagens enviadas como base64 em JSON | RNF-05: nada a compilar no front-end; evita interpretar multipart à mão | React com npm; multipart | 🟢 |
| D-05 | Geração: montar `theme.json` e as imagens num ZIP temporário em `~/tematv/temas/`, rodar `ThemePackageLoader.load()` sobre ele numa pasta temporária e só então renomear para `<theme_id>.tematv`; recusado, o temporário é apagado e o erro (`ThemeError` com campo) volta para a página | RN-01, RF-08: mesmas regras, inclusive 15 MB, 200 entradas e 40 MB descompactados | Validar só os campos no formulário | 🟢 |
| D-06 | Lista NVI e molduras prontas copiadas na compilação: `default-theme/theme.json` (versículos e atribuição) do `:app` e `frame-biblia.png`, `frame-hamburguer.png` e as áreas dos `theme.json` de `sample-themes/` vão para os recursos do `:theme-builder` | Fonte única dos 20 versículos (já há duas cópias) e das molduras | Terceira cópia à mão | 🟢 |
| D-07 | Imagens: PNG e JPEG maiores que 1920×1080 são reduzidos com `javax.imageio` mantendo a proporção; WEBP só é aceito se já tiver até 1920×1080 (lido pelo `ImageHeaderReader`), senão a página pede PNG ou JPEG | RF-09; `javax.imageio` não lê WEBP | Biblioteca de WEBP | 🟢 |
| D-08 | Senha de ativação: `ActivationCode.expectedFor` com o segredo lido de `~/.config/tematv/activation-secret` a cada pedido; registro em `~/tematv/ativacoes.csv` no mesmo formato do `activation-tool` (segunda ocorrência, sem extrair) e teste que compara o formato | RF-12, RF-13, RN-04 | Ler do `BuildConfig`; extrair biblioteca comum agora | 🟢 |
| D-09 | Projeto do cliente em `~/tematv/clientes/<theme_id>/projeto.json` mais as imagens na mesma pasta; `theme_id` fixo depois de criado | RF-10, RF-11; o `theme_id` nomeia a pasta e o pacote | Renomear pasta ao editar | 🟢 |
| D-10 | Tipos de cliente `igreja` e `loja` (RN-06): definem a moldura sugerida, a lista inicial, a versão gravada (`NVI` ou `LOJA`) e se a atribuição é fixa | Esclarecimento de 2026-10-30 | Tipos livres | 🟢 |
| D-11 | `app/build.gradle.kts`: sem o arquivo do segredo, a configuração não falha; o `BuildConfig` fica sem valor e a tarefa `preBuild` do `:app` falha com a mesma mensagem | Hoje o erro na configuração impede compilar `:activation-tool` (que gera o segredo) e `:theme-builder` sem o segredo; verificado em 2026-10-30 | Projeto Gradle separado para o notebook | 🟢 |
| D-12 | Script `tools/instalar-tematv.sh`: compila `:theme-builder:installDist` e cria o atalho `~/.local/bin/tematv` | RNF-05 | Pacote `.deb` | 🟡 |
| D-13 | Abrir o navegador: no WSL, `cmd.exe /c start <url>`; fora dele, `xdg-open`; a URL sempre aparece no terminal | WSL2 confirmado no notebook; `wslview` ausente | Só imprimir a URL | 🟢 |
| D-14 | Na TV, nada: a validação pelo `ThemePackageLoader` cobre o formato; aplicar o pacote na TV fica com o usuário, pelos comandos de RF-14 | Pedido do usuário (só instalar na TV); importar tema muda o app em execução | Testar pelo ADB | 🟢 |

## 4. Premissas

| Premissa | Origem (`requirements.md` seção) | Risco se errada |
|----------|----------------------------------|-----------------|
| O navegador do Windows alcança o servidor preso a `127.0.0.1` dentro do WSL2 (encaminhamento de `localhost`) | §6, Plataforma | Página não abre no Windows; mitigação em §9 |
| O notebook do revendedor tem JDK 17 e o SDK Android (já usados nas features anteriores) | §6, Instalação | Script de instalação falha; ele confere e explica |

## 5. Delta arquitetural

| Componente | Arquivo de origem no legado | Tipo de mudança | Resumo |
|------------|------------------------------|-----------------|--------|
| `:theme-builder` | `_reversa_sdd/sdd/theme-builder.md` | componente-novo | Servidor local, página, projetos de cliente, geração e validação de pacote, senha de ativação |
| `:theme-core` | `_reversa_sdd/addenda/001-theme-engine.md` | regra-alterada (só consumo) | Passa a ser usado também no notebook; sem mudança de código prevista |
| `:activation-core` | `_reversa_sdd/addenda/004-license-activation.md` | regra-alterada (só consumo) | Passa a ser usado pela página |
| `:app` (build) | `_reversa_sdd/addenda/004-license-activation.md` | regra-alterada | A falta do segredo só falha ao compilar o app (D-11) |
| `settings.gradle.kts` | `_reversa_sdd/addenda/004-license-activation.md` | regra-alterada | Inclui `:theme-builder` |

## 6. Delta no modelo de dados

- Resumo das mudanças: novo `projeto.json` por cliente com tipo `igreja`/`loja`; somem a configuração da Bíblia API, a versão escolhida e a confirmação de licença da spec. O pacote gerado segue o `theme.json` v1 sem mudança.
- Detalhe completo em: `_reversa_forward/005-theme-builder/data-delta.md`

## 7. Delta de contratos externos

| Contrato | Tipo | Arquivo de detalhe |
|----------|------|--------------------|
| API local da página | HTTP (só `127.0.0.1`) | `_reversa_forward/005-theme-builder/interfaces/api-local.md` |
| Pacote `.tematv` v1 (produzido) | arquivo | `_reversa_forward/001-theme-engine/interfaces/pacote-tematv.md` (sem mudança) |
| Importação pelo ADB (comandos mostrados) | ADB | `_reversa_forward/001-theme-engine/interfaces/importacao-adb.md` (sem mudança) |
| Senha de ativação | algoritmo | `_reversa_forward/004-license-activation/interfaces/senha-ativacao.md` (sem mudança) |

## 8. Plano de migração

n/a: ferramenta nova; nada a migrar. Os temas de exemplo continuam sendo gerados pela tarefa `:app:packSampleThemes`.

## 9. Riscos e mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| Navegador do Windows não alcança `127.0.0.1` do WSL2 (modo de rede espelhado ou encaminhamento desligado) | alto | baixo | Conferir no onboarding; se falhar, documentar `localhostForwarding=true` no `.wslconfig` |
| Prévia diferente da TV | médio | médio | Reproduzir as medidas do `activity_home.xml` em proporção; a TV é a referência final |
| Pacote acima de 15 MB com imagens grandes | baixo | médio | Redução para 1920×1080 (D-07) e erro com o tamanho |
| Outro site chamar a API local | alto | baixo | D-03 com testes |
| Mudança em `app/build.gradle.kts` deixar o APK sem segredo | alto | baixo | `preBuild` falha sem o segredo; teste manual com o arquivo ausente |

## 10. Critério de pronto

- [ ] Todas as ações do `actions.md` marcadas `[X]`
- [ ] Testes automatizados de todos os módulos passando, incluindo pacote gerado aceito pelo `ThemePackageLoader` e senha igual aos vetores fixos
- [ ] Página aberta no navegador do Windows e um pacote de igreja e um de loja gerados
- [ ] `regression-watch.md` gerado
- [ ] Re-extração reversa executada e sem regressão vermelha (recomendado, não obrigatório)

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-plan` | reversa |
