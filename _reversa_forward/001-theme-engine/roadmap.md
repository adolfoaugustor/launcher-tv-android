# Roadmap: Motor de temas (theme-engine)

> Identificador: `001-theme-engine`
> Data: `2026-10-29`
> Requirements: `_reversa_forward/001-theme-engine/requirements.md`
> Branch: `feature/001-theme-engine`
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA

## 1. Resumo da abordagem

Projeto novo: não há legado, então o "delta" é a criação da base do app Android TV e do motor de temas. O código vive em `launcher/` (único caminho liberado em `.reversa/reversa-config.json`). O app Android fica em `launcher/android/`, um projeto Gradle em Kotlin com dois módulos:
- `:theme-core`: Kotlin puro (JVM). Concentra o formato do pacote, a descompactação segura e a validação. Roda nos testes sem TV nem emulador.
- `:app`: o app Android (`br.tematv.launcher`). Liga o núcleo ao armazenamento do Android, decodifica as imagens, guarda o último tema válido e o último erro, e avisa os inscritos por um fluxo observável.

O tema padrão vai embutido em `assets/` do app, validado pelas mesmas regras nos testes. Os dois pacotes de exemplo (Bíblia e hambúrguer) nascem de pastas-fonte em `launcher/android/sample-themes/`, empacotadas por uma tarefa do Gradle. Nesta feature não há tela; a verificação na TV é feita pelo log do sistema e por um aviso (broadcast) de recarga que a feature 6 também vai usar.

## 2. Princípios aplicados

`.reversa/principles.md` não existe neste projeto. Não há princípios formais a verificar; seguem as regras globais do usuário (YAGNI, falhar cedo e de forma explícita, nomes descritivos), refletidas nas decisões abaixo.

| Princípio | Como a feature se relaciona | Status |
|-----------|------------------------------|--------|
| Falhar cedo (regra global do usuário) | Toda recusa de pacote gera código de erro explícito e registro; nenhum erro é engolido | respeita |
| YAGNI (regra global do usuário) | Sem editor, sem download, sem animações; só o que a feature 2 consome | respeita |

## 3. Decisões técnicas

| ID | Decisão | Justificativa | Alternativas descartadas | Confidência |
|----|---------|----------------|--------------------------|-------------|
| D-01 | Kotlin, Gradle com Kotlin DSL, JDK 17, Android Gradle Plugin 8.5.x, Kotlin 2.0.x | Padrão atual de Android; JDK 17 é o exigido pelo AGP 8 | Java; Flutter; React Native (pesados para TV de 2 GB) | 🟢 |
| D-02 | `minSdk 28`, `targetSdk 34`, `compileSdk 34` | RNF de compatibilidade (Android TV 9+); TCL de referência é Android 11 (API 30) | minSdk 30 (excluiria TVs com Android 9 e 10) | 🟡 |
| D-03 | Módulo `:theme-core` em Kotlin puro + módulo `:app` Android | Testes do formato e da validação rodam na JVM, sem TV (RNF de testabilidade) | Tudo no `:app` com Robolectric (testes mais lentos e frágeis) | 🟢 |
| D-04 | JSON com `kotlinx.serialization` no `:theme-core` | Funciona na JVM e no Android, com erros de campo precisos | `org.json` (só existe no Android; nos testes JVM é stub) | 🟢 |
| D-05 | Descompactação com `java.util.zip.ZipInputStream`, normalizando cada caminho e recusando `..`, caminho absoluto, total descompactado acima de 40 MB e mais de 200 entradas | RNF de segurança (zip slip) e proteção contra "zip bomb" | Descompactar sem checagem | 🟢 |
| D-06 | Validação de imagem em duas camadas: `:theme-core` confere existência e assinatura do arquivo (PNG, JPEG, WEBP) e lê largura e altura do cabeçalho; `:app` decodifica com redução de amostragem para no máximo 1920×1080 | Núcleo testável na JVM; memória controlada na TV (RNF de memória) | Decodificar tudo no núcleo (exige bibliotecas de imagem fora do Android) | 🟢 |
| D-07 | Pastas na TV: pacotes em `getExternalFilesDir(null)/themes/`, `active_theme.txt` na mesma pasta; pacote descompactado em `filesDir/theme-cache/<theme_id>-<sha256 curto>/` | Pasta externa do app é gravável por ADB e legível pelo app sem permissão; cache interno é privado | Pasta Download pública (exige permissão no Android 11) | 🟢 |
| D-08 | O app cria `themes/` no `Application.onCreate` | Resolve o problema apontado na revisão: a pasta externa só existe depois que o app a pede | Criar a pasta pelo ADB (dono e permissões podem ficar errados) | 🟢 |
| D-09 | Último tema válido e último erro guardados em `SharedPreferences` (`theme_state`) | Sobrevivem a reinicialização sem banco de dados | Banco SQLite/Room (excessivo para dois valores) | 🟢 |
| D-10 | Repositório `ThemeRepository` expõe `StateFlow<ActiveTheme>` e uma função `reload()`; um `BroadcastReceiver` exportado responde à ação `br.tematv.launcher.action.RELOAD_THEME` chamando `reload()` | RF-08 (avisar inscritos) e RF-04 (pedir nova leitura); o broadcast permite recarregar pelo ADB na feature 6 | Observar a pasta com `FileObserver` (instável em armazenamento externo) | 🟡 |
| D-11 | Tema padrão como pasta descompactada em `app/src/main/assets/default-theme/`, lido sem passar pelo ZIP | Sempre disponível, sem IO de pacote; o teste valida a pasta com o mesmo validador | Embutir o padrão como `.tematv` em `assets/` (descompactação desnecessária a cada boot) | 🟢 |
| D-12 | Pacotes de exemplo gerados pela tarefa Gradle `:app:packSampleThemes` a partir de `launcher/android/sample-themes/<nome>/` | Fonte legível no git, pacote reproduzível | Guardar `.tematv` binário no git | 🟢 |
| D-13 | Arte provisória (molduras de Bíblia e hambúrguer, logotipo do produto, fundo) gerada por um script Python sem dependências em `launcher/android/tools/make_placeholder_art.py` | Não há ferramenta de imagem instalada; arte final pode substituir os PNGs depois sem mudar código | Esperar arte de designer (bloquearia a feature) | 🟡 |
| D-14 | Logs com a tag `TemaTV` e registro de recusa com código, campo e data e hora | RNF de observabilidade e RF-09 | Sem log estruturado | 🟢 |

## 4. Premissas

Não há `[DÚVIDA]` pendente no `requirements.md`. As premissas abaixo vêm de decisões deste plano:

| Premissa | Origem (`requirements.md` seção) | Risco se errada |
|----------|----------------------------------|-----------------|
| Android TV 9 (API 28) basta como mínimo para os clientes | §6, Compatibilidade | TVs mais antigas ficam de fora |
| O ADB consegue gravar em `Android/data/br.tematv.launcher/files/themes/` na TCL (Android 11) | §5, RF-04; `_reversa_sdd/sdd/device-provisioning.md#14-open-questions` (OQ-01) | A feature 6 precisaria de importação alternativa |
| A arte provisória é aceitável até a arte final | §5, RF-11 | Só estética; não afeta o motor |

## 5. Delta arquitetural

Sem `_reversa_sdd/architecture.md` (projeto novo). Os componentes vêm de `_reversa_sdd/sdd/theme-engine.md`.

| Componente | Arquivo de origem | Tipo de mudança | Resumo |
|------------|-------------------|-----------------|--------|
| Projeto Gradle `launcher/android/` | `_reversa_sdd/prd.md#4-escopo-in` | componente-novo | Base do app Android TV, compartilhada pelas features 2 a 4 |
| `:theme-core` (`br.tematv.themecore`) | `_reversa_sdd/sdd/theme-engine.md#6-requisitos-funcionais` | componente-novo | Modelo `ThemeManifest`, `SafeZipExtractor`, `ThemeValidator`, códigos `ThemeError` |
| `:app` / `br.tematv.launcher.theme` | `_reversa_sdd/sdd/theme-engine.md#8-design-e-interface` | componente-novo | `ThemeRepository`, `ThemeStorage`, `ThemeImageLoader`, `ReloadThemeReceiver`, tema padrão em `assets/` |
| Pacote `.tematv` v1 | `_reversa_sdd/sdd/theme-engine.md#9-modelo-de-dados` | contrato-novo | Formato de arquivo consumido aqui e produzido pela feature 5 |
| Broadcast `RELOAD_THEME` | Decisão D-10 | contrato-novo | Pedido de recarga do tema, usado pela feature 6 |

## 6. Delta no modelo de dados

- Resumo das mudanças: nasce o esquema `theme.json` v1, o estado persistido do tema (`theme_state`) e o layout de pastas na TV.
- Detalhe completo em: `_reversa_forward/001-theme-engine/data-delta.md`

## 7. Delta de contratos externos

| Contrato | Tipo | Arquivo de detalhe |
|----------|------|--------------------|
| Pacote de tema `.tematv` v1 e pasta de temas | arquivo | `_reversa_forward/001-theme-engine/interfaces/pacote-tematv.md` |
| Broadcast `br.tematv.launcher.action.RELOAD_THEME` | intent Android | `_reversa_forward/001-theme-engine/interfaces/broadcast-reload-theme.md` |

## 8. Plano de migração

n/a. Primeira versão do app e do formato; não há dados anteriores.

## 9. Riscos e mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| Faltam JDK 17, Android SDK e Gradle no notebook, bloqueando compilação e testes | alto | alto | `onboarding.md` traz a instalação passo a passo; o coding para na primeira ação que exigir compilar até o usuário instalar |
| Imagens grandes estourarem a memória da TV de 2 GB | médio | médio | Decodificação com redução de amostragem (D-06) e limite de 15 MB por pacote |
| Pacote malicioso gravar fora da pasta do app | alto | baixo | Extrator seguro com testes dedicados (D-05) |
| Broadcast exportado ser disparado por outro app | baixo | baixo | Só provoca recarga do tema; não aceita dados no intent |
| Arte provisória parecer amadora para o cliente | baixo | alto | Substituição por arte final sem mudança de código |

## 10. Critério de pronto

- [ ] Todas as ações do `actions.md` marcadas `[X]`
- [ ] `./gradlew :theme-core:test :app:testDebugUnitTest` passando
- [ ] `./gradlew :app:assembleDebug :app:packSampleThemes` gerando o APK e os dois `.tematv`
- [ ] Na TCL de referência: aplicar o pacote de exemplo pelo ADB e ver no log `TemaTV` o tema carregado; apontar para um pacote inválido e ver o código de erro e o tema anterior mantido
- [ ] `regression-watch.md` gerado
- [ ] Commit na branch `feature/001-theme-engine`

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-29 | Versão inicial gerada por `/reversa-plan` | reversa |
