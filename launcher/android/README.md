# Launcher Temático (Android TV)

App Android TV que troca a tela inicial da TV por um tema da igreja ou do cliente.

## O que existe hoje (feature 001, motor de temas)

- `theme-core/`: núcleo em Kotlin puro. Lê o pacote `.tematv`, descompacta com segurança e valida o `theme.json` v1.
- `app/`: app `br.tematv.launcher`. Guarda os pacotes na TV, aplica o tema ativo, mantém o último tema válido e recarrega o tema quando recebe o broadcast `br.tematv.launcher.action.RELOAD_THEME`.
- `app/src/main/assets/default-theme/`: tema padrão (bíblico, sem igreja, mensagem "Deus te abençoe!").
- `sample-themes/`: fontes dos pacotes de exemplo (`biblia-exemplo`, `hamburguer-exemplo`).
- `tools/make_placeholder_art.py`: gera a arte provisória (molduras, logotipo e fundos).

Ainda não há tela: a tela inicial chega na feature 002.

## Compilar e testar

Pré-requisitos: JDK 17, Android SDK (plataforma 34) e Gradle 8.7 para gerar o wrapper na primeira vez.

```bash
gradle wrapper --gradle-version 8.7   # só na primeira vez
./gradlew :theme-core:test :app:testDebugUnitTest
./gradlew :app:assembleDebug :app:packSampleThemes
```

Instalação das ferramentas e teste na TV, passo a passo: `_reversa_forward/001-theme-engine/onboarding.md`, na raiz do repositório.

## Formato do pacote de tema

Especificação: `_reversa_forward/001-theme-engine/interfaces/pacote-tematv.md` e `data-delta.md` (regras de cada campo).

## Arte

Os PNGs atuais são provisórios. Para trocar pela arte final, substitua os arquivos mantendo nomes e proporções (moldura em retrato 260×340, com as áreas `icon_slot` e `label_slot` do `theme.json`). Para regerar a arte provisória:

```bash
python3 tools/make_placeholder_art.py
```

Os versículos do tema padrão e dos exemplos também são provisórios: troque por uma tradução com licença de uso comercial antes de distribuir.
