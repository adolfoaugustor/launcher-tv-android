# Legacy impact: Ativação por licença (license-activation)

> Identificador: `004-license-activation`
> Data: `2026-10-30`
> Feature greenfield, sem legado pré-existente. Âncora: `_reversa_sdd/prd.md` + specs SDD (`_reversa_sdd/sdd/license-activation.md`) + adendos vigentes 001, 002 e 003.
> Política de edição no momento da execução: `allowLegacyEdits: true`, `allowedPaths: ["launcher/**"]`. Todos os arquivos do projeto tocados casam com `launcher/**`; o segredo foi gravado fora do projeto, em `~/.config/tematv/`, pela ferramenta.
> Estado: concluída. 20 de 20 ações; 94 testes automatizados passando; APK instalado na TV de teste (testes na TV com o usuário).

## Arquivos afetados

| Arquivo afetado | Componente | Tipo | Severidade | Justificativa |
|-----------------|------------|------|------------|---------------|
| `launcher/android/settings.gradle.kts` | Projeto Gradle | componente-novo | LOW | Módulos `:activation-core` e `:activation-tool` |
| `launcher/android/activation-core/**` | `:activation-core` (D-01 a D-03, D-11, D-13) | componente-novo | HIGH | Código do aparelho, senha e tentativas, com vetores fixos |
| `launcher/android/activation-tool/**` | `:activation-tool` (D-04, D-06) | componente-novo | MEDIUM | Ferramenta do revendedor |
| `launcher/android/app/build.gradle.kts` | `:app` (D-05) | componente-novo | HIGH | Segredo na compilação; build falha sem ele |
| `.../activation/ActivationStore.kt`, `ActivationService.kt` | `:app` (D-07, D-08, D-12) | componente-novo | HIGH | Estado da ativação, tentativas, `ActivationLicenseGate`, troca de arquivos pelo ADB |
| `.../TemaTvApplication.kt`, `.../theme/ReloadThemeReceiver.kt` | `:app` (feature 001) | regra-alterada | MEDIUM | A recarga também publica o código e importa a senha |
| `.../theme/ThemeImportNames.kt` e teste | `:app` (feature 001) | contrato-alterado | MEDIUM | Nomes `device_code.txt` e `activation_code.txt` |
| `.../takeover/TakeoverPolicy.kt`, `HomeTakeoverService.kt`, `LicenseGate.kt` e teste | `:app` (feature 003) | regra-alterada | MEDIUM | Home sem ativação abre a tela de ativação; capa sem logotipo; `AlwaysValidLicenseGate` removido |
| `.../home/HomeActivity.kt`, `res/layout/activity_home.xml`, `res/values/colors.xml` | `:app` (feature 002) | regra-alterada | MEDIUM | Painel de ativação no lugar da grade |
| `.../home/ThemeOptionsActivity.kt` | `:app` (feature 002/003) | regra-alterada | LOW | Diagnóstico da ativação |
| `res/values/strings.xml`, `README.md` | `:app`, documentação | componente-novo | LOW | Textos e instruções |

## Diff conceitual por componente

- **Ativação:** a licença virou uma senha de 12 números calculada do código do aparelho com o segredo do revendedor (HMAC-SHA256), digitada na TV ou enviada pelo ADB. Perpétua, offline, reconferida a cada consulta.
- **Troca da tela inicial:** deixou de ignorar o Home sem licença; agora sempre abre a tela inicial, que mostra o painel de ativação quando a TV não está ativada.
- **Importação pelo ADB:** passou a servir também para ler o código do aparelho e enviar a senha.

## Preservadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.

## Modificadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.
