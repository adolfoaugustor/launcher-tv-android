# Legacy impact: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`
> Feature greenfield, sem legado pré-existente. Âncora: `_reversa_sdd/prd.md` + specs SDD (`_reversa_sdd/sdd/theme-builder.md`) + adendos vigentes 001 a 004.
> Política de edição no momento da execução: `allowLegacyEdits: true`, `allowedPaths: ["launcher/**"]`. Todos os arquivos do projeto tocados casam com `launcher/**`.
> Estado: concluída. 22 de 22 ações; 121 testes automatizados passando; página verificada no navegador do Windows; nada enviado à TV.

## Arquivos afetados

| Arquivo afetado | Componente | Tipo | Severidade | Justificativa |
|-----------------|------------|------|------------|---------------|
| `launcher/android/settings.gradle.kts` | Projeto Gradle | componente-novo | LOW | Módulo `:theme-builder` |
| `launcher/android/theme-builder/build.gradle.kts` | `:theme-builder` (D-01, D-06) | componente-novo | MEDIUM | Comando `tematv`, cópia da lista NVI e das molduras, socket IPv4 |
| `launcher/android/theme-builder/src/main/kotlin/**` | `:theme-builder` (D-02 a D-10, D-13) | componente-novo | HIGH | Projetos, imagens, geração validada pelo `ThemePackageLoader`, senha de ativação, proteção do servidor local |
| `launcher/android/theme-builder/src/main/resources/web/**` | `:theme-builder` (D-04, RF-02 a RF-14) | componente-novo | MEDIUM | Página: clientes, formulário, prévia, "Ativar TV" |
| `launcher/android/theme-builder/src/test/**` | `:theme-builder` | componente-novo | LOW | 27 testes |
| `launcher/android/app/build.gradle.kts` | `:app` (D-11) | regra-alterada | MEDIUM | Falta do segredo só falha ao compilar o app |
| `launcher/android/tools/instalar-tematv.sh` | Instalação (D-12) | componente-novo | LOW | Instala e cria o atalho `tematv` |
| `launcher/android/README.md` | Documentação | componente-novo | LOW | Seção da feature 005 |

## Diff conceitual por componente

- **Ferramenta do notebook:** passa a existir uma página local que gera pacotes `.tematv` sem editar JSON, para igrejas (versículos NVI) e lojas (mensagens da loja), e que gera a senha de ativação. O pacote só é salvo depois de aceito pelo mesmo código que o launcher usa na TV.
- **Compilação:** sem o segredo de ativação, só o app da TV deixa de compilar; antes, nenhum módulo compilava.
- **App da TV:** sem mudança de comportamento.

## Preservadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.

## Modificadas

Não se aplica: feature greenfield, sem regras 🟢 extraídas de código existente.
