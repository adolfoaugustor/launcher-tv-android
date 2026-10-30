# Launcher Temático (Android TV)

App Android TV que troca a tela inicial da TV por um tema da igreja ou do cliente.

## O que existe hoje

### Feature 001, motor de temas

- `theme-core/`: núcleo em Kotlin puro. Lê o pacote `.tematv`, descompacta com segurança e valida o `theme.json` v1.
- `app/`: app `br.tematv.launcher`. Guarda os pacotes na TV, aplica o tema ativo, mantém o último tema válido e recarrega o tema quando recebe o broadcast `br.tematv.launcher.action.RELOAD_THEME`. Os pacotes entram pela importação do ADB (`content write --uri content://br.tematv.launcher.themes/<nome>`), porque o ADB não grava direto na pasta do app.
- `app/src/main/assets/default-theme/`: tema padrão (bíblico, sem igreja, mensagem "Deus te abençoe!").
- `sample-themes/`: fontes dos pacotes de exemplo (`biblia-exemplo`, `hamburguer-exemplo`).
- `tools/make_placeholder_art.py`: gera a arte provisória (molduras, logotipo e fundos).

### Feature 002, tela inicial

- `app/src/main/java/br/tematv/launcher/home/`: tela inicial (`HomeActivity`) com cabeçalho (logotipo, mensagem, botões "Configurações" e "Tema"), versículo do dia e grade de 5 colunas. Cada app é desenhado dentro da moldura do tema, com o banner de TV (ou o ícone) no painel e o nome na faixa.
- A tela "Tema" (`ThemeOptionsActivity`) mostra o diagnóstico: versão, tema ativo, origem e último erro.
- O launcher aparece na lista de apps do Google TV como "Launcher Temático". Ainda não assume o botão Home: isso chega na feature 003.
- Utilitários técnicos da TCL ficam fora da grade (`HiddenSystemApps`): `com.tcl.guard`, `com.tcl.suspension`, `com.tcl.partnercustomizer`, `com.tcl.notereminder`, `com.tcl.dashboard`, `com.tcl.waterfall.overseas` e `com.tcl.t_solo`. Para mostrar ou esconder outro app, edite essa lista.

Abrir a tela pelo ADB:

```bash
adb shell am start -n br.tematv.launcher/.home.HomeActivity
```

## Compilar e testar

Pré-requisitos: JDK 17 e Android SDK (plataforma 34). O Gradle 8.7 vem pelo wrapper do projeto; crie `local.properties` com `sdk.dir=<caminho do SDK>`.

```bash
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

Para medir a aplicação de um pacote perto do limite de 15 MB, gere `app/build/large-test-theme/grande-teste.tematv` com:

```bash
python3 tools/make_large_test_theme.py
```

Os versículos do tema padrão e dos exemplos também são provisórios: troque por uma tradução com licença de uso comercial antes de distribuir.
