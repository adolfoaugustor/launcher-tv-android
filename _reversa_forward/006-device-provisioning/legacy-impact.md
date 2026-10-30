# Legacy impact: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`
> Feature greenfield, sem legado pré-existente. Âncora: `_reversa_sdd/prd.md` + specs SDD (`_reversa_sdd/sdd/device-provisioning.md`) + adendos vigentes 001 a 005.
> Política de edição no momento da execução: `allowLegacyEdits: true`, `allowedPaths: ["launcher/**"]`. Todos os arquivos do projeto tocados casam com `launcher/**`; a chave e o launcher de produção foram gravados fora do projeto, em `~/.config/tematv/` e `~/tematv/launcher/`.
> Estado: concluída. 23 de 23 ações; 146 testes passando; instalação completa verificada na TV de referência.

## Arquivos afetados

| Arquivo afetado | Componente | Tipo | Severidade | Justificativa |
|-----------------|------------|------|------------|---------------|
| `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/install/**` | `:theme-builder` (D-01, D-02, D-08 a D-16) | componente-novo | HIGH | Etapas da instalação pelo ADB, registro, release do launcher |
| `launcher/android/theme-builder/src/main/kotlin/br/tematv/themebuilder/LocalServer.kt`, `Main.kt` | `:theme-builder` (D-01, D-03) | regra-alterada | MEDIUM | Rotas da instalação e várias linhas de execução |
| `launcher/android/theme-builder/src/main/resources/web/**` | `:theme-builder` (RF-01 a RF-14) | componente-novo | MEDIUM | Aba "Instalar na TV" |
| `launcher/android/theme-builder/src/test/kotlin/br/tematv/themebuilder/install/**` | `:theme-builder` | componente-novo | LOW | 22 testes com ADB simulado |
| `launcher/android/app/src/main/java/br/tematv/launcher/status/AppStatus.kt` e `TemaTvApplication.kt` | `:app` (D-04) | componente-novo | MEDIUM | Estado do app publicado a cada recarga |
| `launcher/android/app/src/main/java/br/tematv/launcher/theme/ThemeImportNames.kt`, `ThemeImportProvider.kt` e teste | `:app` (feature 001) | contrato-alterado | MEDIUM | `app_status.json` somente leitura na importação |
| `launcher/android/app/build.gradle.kts` | `:app` (D-05, D-06) | regra-alterada | HIGH | Assinatura de release com a chave definitiva; versão 7 / 0.6.1 |
| `launcher/android/activation-core/src/main/kotlin/br/tematv/activation/ActivationCode.kt` e teste | `:activation-core` (feature 004) | regra-alterada | HIGH | `normalize` ignora espaços nas pontas: a senha pela importação chegava com quebra de linha e era descartada |
| `launcher/android/tools/gerar-chave-assinatura.sh`, `tools/instalar-tematv.sh` | Scripts (D-05, D-07) | componente-novo | MEDIUM | Chave definitiva; launcher de produção em `~/tematv/launcher/` |
| `launcher/android/sample-themes/biblia-exemplo/theme.json` | Tema de exemplo | regra-alterada | LOW | Mensagem corrigida a pedido do usuário |
| `launcher/android/README.md` | Documentação | componente-novo | LOW | Seção da feature 006 |

## Diff conceitual por componente

- **Ferramenta do notebook:** instala e configura uma TV num clique, etapa por etapa, conferindo cada uma pelo estado publicado pelo app e registrando a tentativa.
- **App da TV:** publica o próprio estado (versão, assinatura, ativação, tema aplicado) pela importação; aceita a senha com quebra de linha.
- **Compilação:** o APK de produção é assinado com a chave definitiva do revendedor; sem ela, só a compilação de release para.

## Preservadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.

## Modificadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.
