# Roadmap: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`
> Requirements: `_reversa_forward/006-device-provisioning/requirements.md`
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA

## 1. Resumo da abordagem

A aba "Instalar na TV" entra na página do `:theme-builder`. A página conduz as etapas uma a uma, cada uma num pedido à API local; o servidor executa o `adb` por trás de uma interface de comandos (testável sem TV) e grava uma linha de registro por etapa. Para conferir o resultado sem depender do log da TV, o app passa a publicar, pela importação do ADB, um arquivo de estado (`app_status.json`) com ativação, tema aplicado, versão e assinatura. O APK de produção passa a ser assinado com a chave definitiva do revendedor, criada por um script e guardada fora do git; o script de instalação da ferramenta compila esse APK e o deixa pronto em `~/tematv/launcher/`. O servidor ganha várias linhas de execução, para uma etapa longa (autorização na TV) não travar a página.

## 2. Princípios aplicados

Não há `.reversa/principles.md`. Regras do usuário aplicadas: falhar cedo (D-05, D-08), simples primeiro (D-01, D-09), nunca engolir erros (cada etapa devolve ok, pulada ou erro com mensagem).

| Princípio | Como a feature se relaciona | Status |
|-----------|------------------------------|--------|
| n/a | Sem `principles.md` | n/a |

## 3. Decisões técnicas

| ID | Decisão | Justificativa | Alternativas descartadas | Confidência |
|----|---------|----------------|--------------------------|-------------|
| D-01 | Aba no `:theme-builder`; a página chama uma etapa por vez (`POST /api/instalacao/etapas/<etapa>`) levando IP, cliente e o que já foi descoberto (código do aparelho); o servidor não guarda sessão | "Tentar de novo a partir desta etapa" vira só chamar a etapa de novo; nada a sincronizar | Instalação inteira numa thread com consulta de progresso | 🟢 |
| D-02 | `CommandRunner` (executa um comando com tempo limite e entrada opcional) e `Adb` (sempre `adb -s <ip>:5555`); testes com um executor falso | RN-04; lógica das etapas testável sem TV | Chamar `ProcessBuilder` direto nas etapas | 🟢 |
| D-03 | `HttpServer` com um conjunto de threads | A espera da autorização (até 120 s) não pode travar imagens e outras chamadas | Servidor de uma thread | 🟢 |
| D-04 | O app publica `app_status.json` na pasta de importação (leitura pelo ADB): ativada, tema aplicado, `versionCode` e SHA-256 do certificado de assinatura; regravado a cada recarga | Conferência determinística do tema, da ativação e da decisão de pular a instalação, sem ler o log da TV (que guarda linhas de execuções antigas) | `logcat` com marca de tempo | 🟢 |
| D-05 | Chave definitiva: `tools/gerar-chave-assinatura.sh` cria `~/.config/tematv/assinatura.jks` e `assinatura.properties` (senhas aleatórias, permissão `600`), recusa se já existirem e avisa do backup. `app/build.gradle.kts` lê o arquivo na configuração e, sem ele, só as tarefas de release falham | RN-06, RF-15; mesma lição da D-11 da feature 005 | Chave no repositório; assinar à mão | 🟢 |
| D-06 | `versionCode` 6 e `versionName` "0.6.0" no app | Versão maior que a do APK de teste (1) | Manter 1 | 🟢 |
| D-07 | `tools/instalar-tematv.sh` também compila `:app:assembleRelease` (se a chave existir) e copia para `~/tematv/launcher/` o APK e um `launcher.json` com `version_code`, `version_name` e `signer_sha256` (lido com `apksigner` do SDK) | A aba sabe qual APK instalar e com o que comparar | Procurar o APK na pasta de compilação | 🟢 |
| D-08 | Pula a instalação só se `versionCode` e assinatura da TV (`app_status.json`) forem iguais aos do `launcher.json`; `INSTALL_FAILED_UPDATE_INCOMPATIBLE` vira o erro "assinatura diferente" com a oferta "Desfazer e instalar de novo" | RF-05; a TV de teste tem o APK de depuração | Pular só pela versão | 🟢 |
| D-09 | "TV encontrada": conexão TCP na porta 5555 com 5 s de limite, feita pelo servidor | RF-03; dispensa `ping` (que pode exigir permissão) | `ping` | 🟢 |
| D-10 | Conectar e autorizar: `adb connect`, depois `get-state` a cada 1 s até `device` (120 s); `offline` provoca um `disconnect` e `connect` | RF-04; TCL depois do modo de espera (EC-05 da spec) | Só `wait-for-device` | 🟢 |
| D-11 | Lista de acessibilidade: função pura que junta a lista atual (`null` = vazia) com o serviço, comparando o nome completo de cada componente, sem duplicar; valor gravado entre aspas simples no shell da TV; conferência em `dumpsys accessibility` por até 15 s | RN-02, RF-10; nomes com `$` seriam expandidos pelo shell da TV | Comparar por trecho de texto | 🟢 |
| D-12 | Início automático: `appops set ... APP_AUTO_START allow` e conferência com `appops get`; só "operação desconhecida" conta como "não se aplica" | RF-06 | Ignorar qualquer erro | 🟢 |
| D-13 | Tema e senha pela importação, com repetição por até 30 s enquanto ela não responde; a senha vai pela entrada padrão do `adb`, nunca na linha de comando nem no registro; conferência pelo `app_status.json` depois da recarga | RF-07 a RF-09, RN-05 | Senha como argumento | 🟢 |
| D-14 | Conferência final: espera o serviço ligado, envia Home e procura por 5 s a linha `mCurrentFocus` não nula com `br.tematv.launcher` (correção da feature 003) | RF-11 | Conferir antes de o serviço ligar | 🟢 |
| D-15 | Registro: `~/tematv/instalacoes/<AAAA-MM-DD>-<ip>.log`, uma linha JSON por etapa com o identificador da tentativa (gerado pela página) | RF-13, RN-05 | Um arquivo por tentativa | 🟢 |
| D-16 | Desfazer: tira o serviço da lista (se vazia, `accessibility_enabled 0`) e `adb uninstall br.tematv.launcher` | RF-14; também usado por "Desfazer e instalar de novo" | Desativar o app | 🟢 |
| D-17 | Na TV: o `/reversa-coding` para antes do primeiro comando ADB e pede ao usuário que ligue a TV e confirme, lembrando que a primeira instalação desfaz o launcher de teste (a ativação atual se perde e a TV recebe código e senha novos) | Regra do usuário e esclarecimento de 2026-10-30 | Rodar sem avisar | 🟢 |

## 4. Premissas

| Premissa | Origem (`requirements.md` seção) | Risco se errada |
|----------|----------------------------------|-----------------|
| O `apksigner` do SDK (`build-tools/34.0.0`) roda no notebook | §6, Compatibilidade | Sem a assinatura no `launcher.json`, a instalação nunca é pulada (só mais lenta) |
| A compilação de release passa no `lintVitalRelease` | §5, RF-15 | Conferir cedo, na preparação |

## 5. Delta arquitetural

| Componente | Arquivo de origem no legado | Tipo de mudança | Resumo |
|------------|------------------------------|-----------------|--------|
| `:theme-builder` | `_reversa_sdd/addenda/005-theme-builder.md` | componente-novo | Aba, etapas, `Adb`, registro, threads no servidor |
| `:app` (importação) | `_reversa_sdd/addenda/001-theme-engine.md` | contrato-alterado | Novo nome somente leitura `app_status.json` |
| `:app` (estado) | `_reversa_sdd/addenda/004-license-activation.md` | regra-alterada | A recarga grava o estado depois de importar senha e tema |
| `:app` (build) | `_reversa_sdd/addenda/005-theme-builder.md` | regra-alterada | Assinatura de release com a chave definitiva; versão 6 |
| Scripts | `_reversa_sdd/addenda/005-theme-builder.md` | componente-novo | `gerar-chave-assinatura.sh`; `instalar-tematv.sh` também prepara o APK |

## 6. Delta no modelo de dados

- Resumo: arquivos `app_status.json` (TV), `assinatura.properties`/`assinatura.jks` (notebook), `launcher.json` (notebook) e o registro de instalação.
- Detalhe completo em: `_reversa_forward/006-device-provisioning/data-delta.md`

## 7. Delta de contratos externos

| Contrato | Tipo | Arquivo de detalhe |
|----------|------|--------------------|
| API da aba "Instalar na TV" | HTTP local | `_reversa_forward/006-device-provisioning/interfaces/api-instalacao.md` |
| Estado do app pela importação | ContentProvider | `_reversa_forward/006-device-provisioning/interfaces/estado-do-app.md` |
| Importação, ativação e acessibilidade pelo ADB | ADB | contratos das features 001, 003 e 004 (sem mudança) |

## 8. Plano de migração

1. Gerar a chave definitiva no notebook (uma vez) e guardar cópia de segurança.
2. Rodar `tools/instalar-tematv.sh` para compilar o APK de produção.
3. Na TV de teste, a primeira instalação encontra o APK de depuração: "Desfazer e instalar de novo" e nova ativação (automática pela aba).

## 9. Riscos e mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| Perder a chave de assinatura | alto | baixo | Aviso de backup na criação e no README; sem ela, novas versões não atualizam as TVs instaladas |
| TV entra em modo de espera no meio | médio | médio | Etapa vermelha com "Tentar de novo"; reconexão uma vez (D-10) |
| Serviço demora a ligar na TCL | médio | médio | Espera em `dumpsys accessibility` antes da conferência (D-14) |
| Shell da TV interpretar caracteres da lista de acessibilidade | alto | baixo | Aspas simples e testes da junção (D-11) |
| `lintVitalRelease` falhar | médio | médio | Compilar o release na preparação |

## 10. Critério de pronto

- [ ] Todas as ações do `actions.md` marcadas `[X]`
- [ ] Testes automatizados passando, incluindo as etapas com executor falso
- [ ] Instalação completa na TV de referência, com aviso prévio ao usuário, terminando com a tela temática ativada
- [ ] `regression-watch.md` gerado

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-plan` | reversa |
