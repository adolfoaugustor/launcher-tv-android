# Roadmap: Ativação por licença (license-activation)

> Identificador: `004-license-activation`
> Data: `2026-10-30`
> Requirements: `_reversa_forward/004-license-activation/requirements.md`
> Branch: `feature/004-license-activation`
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA

## 1. Resumo da abordagem

Delta sobre o app das features 001 a 003. O cálculo do código do aparelho e da senha fica num módulo Kotlin puro novo, `:activation-core`, usado pelos dois lados para não existirem duas implementações:
- pelo app, para conferir a senha;
- por uma ferramenta de linha de comando nova, `:activation-tool`, para gerar o segredo e as senhas no notebook.

A senha é um código de autenticação (HMAC-SHA256) do código do aparelho com o segredo do revendedor, reduzido a 12 números. O segredo fica em `~/.config/tematv/activation-secret` (permissão `600`) e é embutido no app na compilação, sem passar pelo git.

No app:
- a `HomeActivity` mostra um painel de ativação no lugar da grade quando a TV não está ativada;
- a troca da tela inicial deixa de ignorar o Home sem licença e passa a abrir a tela (que mostra a ativação);
- o `LicenseGate` da feature 003 ganha a implementação real;
- a importação do ADB passa a aceitar a leitura do código do aparelho e o envio da senha.

Na TV, seguindo o pedido do usuário, só a instalação do APK é feita; os testes na TV ficam com ele.

## 2. Princípios aplicados

`.reversa/principles.md` não existe. Valem as regras globais do usuário.

| Princípio | Como a feature se relaciona | Status |
|-----------|------------------------------|--------|
| DRY tardio | O algoritmo da senha existe uma vez, em `:activation-core`, usado pelo app e pela ferramenta | respeita |
| Falhar cedo | Compilar o app sem o segredo de ativação falha com instrução clara | respeita |

## 3. Decisões técnicas

| ID | Decisão | Justificativa | Alternativas descartadas | Confidência |
|----|---------|----------------|--------------------------|-------------|
| D-01 | Módulo `:activation-core` (Kotlin JVM, sem dependências) com `DeviceCode`, `ActivationCode` e `ActivationAttempts` | Mesmo algoritmo no app e no notebook; testável na JVM | Implementar duas vezes (Kotlin e Python) | 🟢 |
| D-02 | Código do aparelho: `SHA-256(ANDROID_ID + ":" + applicationId)`, primeiros 8 bytes como número sem sinal, `mod 10^12`, 12 dígitos em `XXXX-XXXX-XXXX` | `ANDROID_ID` (Android 8+) é estável por app e por assinatura até a restauração de fábrica, sem permissão | Número de série (exige permissão), MAC (bloqueado) | 🟢 |
| D-03 | Senha: `HMAC-SHA256(segredo, 12 dígitos do código)`, primeiros 8 bytes `mod 10^12`, 12 dígitos; comparação em tempo constante | RN-01, RN-02; curta para digitar; `javax.crypto.Mac` existe no Android 9 e na JVM sem biblioteca | Assinatura Ed25519 ou ECDSA (64+ bytes, impossível de digitar) | 🟢 |
| D-04 | Segredo: 32 bytes aleatórios em hexadecimal em `~/.config/tematv/activation-secret` (`600`), gerado uma vez pelo `activation-tool gerar-segredo` | RF-03 | Segredo no repositório | 🟢 |
| D-05 | O `:app` lê o segredo na compilação (propriedade `tematv.activationSecretFile`, padrão `~/.config/tematv/activation-secret`) para um campo do `BuildConfig`; sem o arquivo, a compilação para com a instrução do comando que gera o segredo | RF-03; falhar cedo | Segredo em `local.properties` versionável por engano | 🟢 |
| D-06 | `:activation-tool` com o plugin `application` do Gradle: `gerar-segredo` e `senha <código> [--cliente <nome>]`, registrando em `~/tematv/ativacoes.csv` | RF-03, RF-04, RF-11; a página da feature 005 chama o mesmo núcleo | Script Python | 🟢 |
| D-07 | Ativação guardada em `activation_prefs` (`activation_code`, `activated_at`) e reconferida a cada consulta contra o código do aparelho atual | RF-07: restauração de fábrica muda o código e invalida a ativação | Guardar só um booleano | 🟢 |
| D-08 | `ActivationLicenseGate` implementa o `LicenseGate` da feature 003; o `AlwaysValidLicenseGate` deixa de existir | RF-09 | Manter as duas | 🟢 |
| D-09 | A `TakeoverPolicy` deixa de consultar a licença: sempre abre a `HomeActivity`, que decide entre grade e ativação; o serviço usa o `LicenseGate` só para decidir se a capa mostra o logotipo do tema | RN-04 (antes, Home sem licença deixava o Google na tela) | Tela de ativação separada | 🟢 |
| D-10 | Painel de ativação dentro da `HomeActivity`: código em destaque, campo numérico, "Ativar", aviso de erro e espera; o botão "Configurações" do cabeçalho continua | RF-05, RF-06, RF-08 | Atividade separada (a troca e o Home já abrem a `HomeActivity`) | 🟢 |
| D-11 | Tentativas: `ActivationAttempts` puro conta erros seguidos e bloqueia 30 s após o 5º, com relógio monotônico | RN-05 | Sem limite | 🟢 |
| D-12 | Importação do ADB: `device_code.txt` (o app grava ao iniciar; o ADB lê) e `activation_code.txt` (o ADB grava; o app importa ao receber a recarga e apaga o arquivo) | RF-02, RF-10; a pasta do app só é acessível pela importação | Nova autoridade de provider | 🟢 |
| D-13 | Vetores de teste fixos (segredo, código do aparelho, senha esperada) em `:activation-core` | Qualquer outra implementação futura (página da feature 005) confere contra eles | Sem vetores | 🟢 |
| D-14 | Na TV, só `adb install -r`; os testes na TV ficam com o usuário | Pedido do usuário em 2026-10-30 | Testar pelo ADB | 🟢 |

## 4. Premissas

| Premissa | Origem (`requirements.md` seção) | Risco se errada |
|----------|----------------------------------|-----------------|
| `ANDROID_ID` é estável na TCL entre reinícios e reinstalações com a mesma assinatura | §5 RF-01 | A TV pediria ativação de novo após atualizar |
| O teclado da TV abre em modo numérico para o campo da senha | §5 RF-05 | Digitação mais lenta, mas possível |
| A TV de teste ficará sem ativação após a atualização, até o usuário digitar a senha | §1 | Esperado: a tela de ativação aparece; a senha será entregue ao usuário |

## 5. Delta arquitetural

| Componente | Arquivo de origem | Tipo de mudança | Resumo |
|------------|-------------------|-----------------|--------|
| `:activation-core` | `_reversa_sdd/sdd/license-activation.md` | componente-novo | Código do aparelho, senha e tentativas |
| `:activation-tool` | `_reversa_sdd/sdd/license-activation.md#6-requisitos-funcionais` | componente-novo | Linha de comando do revendedor |
| `:app` / `br.tematv.launcher.activation` | `_reversa_sdd/sdd/license-activation.md#8-design-e-interface` | componente-novo | Estado da ativação, `ActivationLicenseGate`, importação pelo ADB |
| `HomeActivity` (feature 002) | `_reversa_sdd/addenda/002-launcher-home.md` | regra-alterada | Painel de ativação no lugar da grade |
| `TakeoverPolicy`, `HomeTakeoverService`, `ThemeCover` (feature 003) | `_reversa_sdd/addenda/003-home-takeover.md` | regra-alterada | Home sem ativação abre a tela de ativação; capa sem logotipo sem ativação |
| `ThemeImportNames`, `ReloadThemeReceiver` (feature 001) | `_reversa_sdd/addenda/001-theme-engine.md` | contrato-alterado | Nomes `device_code.txt` e `activation_code.txt`; a recarga também importa a senha |
| `ThemeOptionsActivity` (feature 002/003) | `_reversa_sdd/addenda/003-home-takeover.md` | regra-alterada | Diagnóstico da ativação: "Ativada em <data>" ou "Não ativada", com o código do aparelho (RF-12) |

## 6. Delta no modelo de dados

- Resumo das mudanças: nascem `activation_prefs` (na TV), o segredo e o registro de senhas (no notebook).
- Detalhe completo em: `_reversa_forward/004-license-activation/data-delta.md`

## 7. Delta de contratos externos

| Contrato | Tipo | Arquivo de detalhe |
|----------|------|--------------------|
| Algoritmo do código do aparelho e da senha | algoritmo compartilhado | `_reversa_forward/004-license-activation/interfaces/senha-ativacao.md` |
| Ativação pelo ADB (`device_code.txt`, `activation_code.txt`) | ContentProvider (importação) | `_reversa_forward/004-license-activation/interfaces/ativacao-adb.md` |

## 8. Plano de migração

TVs já instaladas com a feature 003 passam a mostrar a tela de ativação depois da atualização, até receberem a senha. Na TV de teste, a senha será informada ao usuário junto com o resultado da instalação.

## 9. Riscos e mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| Segredo extraído do APK por engenharia reversa | médio | baixa | Risco aceito (NG-03); o segredo pode ser trocado numa versão futura, exigindo nova ativação |
| Perda do segredo no notebook | alto | baixa | A ferramenta avisa para fazer cópia de segurança ao gerar; sem o segredo, as TVs ativadas continuam ativadas, mas não há como ativar novas com o mesmo APK |
| Segredo enviado ao git por engano | alto | baixa | O segredo mora em `~/.config/tematv/`, fora do repositório; o build só o lê de lá e o valor não é impresso em logs |
| `ANDROID_ID` mudar após atualização do sistema | médio | baixa | A tela de ativação reaparece com o novo código; o revendedor gera nova senha |

## 10. Critério de pronto

- [ ] Todas as ações do `actions.md` marcadas `[X]`
- [ ] `./gradlew :theme-core:test :activation-core:test :app:testDebugUnitTest :app:assembleDebug :activation-tool:installDist` passando
- [ ] Vetores de teste do algoritmo fixados
- [ ] APK instalado na TV de teste (sem mexer no app em execução) e a senha da TV de teste entregue ao usuário
- [ ] `regression-watch.md` gerado
- [ ] Commit na `feature/004-license-activation` e criação da `feature/005-theme-builder`

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-plan` | reversa |
