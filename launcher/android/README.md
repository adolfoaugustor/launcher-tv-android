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

### Feature 003, troca da tela inicial

- `app/src/main/java/br/tematv/launcher/takeover/`: serviço de acessibilidade (`HomeTakeoverService`) que abre a tela temática sempre que o launcher do Google aparece (Home, boot, volta do modo de espera). Ele só percebe qual app está em primeiro plano; não lê o conteúdo das telas.
- O launcher do Google nunca é desativado. Para voltar a ele: tela "Tema" › segure OK por 3 s em "Voltar ao launcher original do Google". Para religar: abra o Launcher Temático pela lista de apps › "Tema" › "Usar o tema como tela inicial".
- Se o Android desligar o serviço, a tela inicial mostra uma faixa discreta e a tela "Tema" mostra o passo a passo.

Ligar o serviço pelo ADB (preservando outros serviços já ligados: ver `_reversa_forward/003-home-takeover/interfaces/ativacao-acessibilidade-adb.md`):

```bash
adb shell settings put secure enabled_accessibility_services br.tematv.launcher/br.tematv.launcher.takeover.HomeTakeoverService
adb shell settings put secure accessibility_enabled 1
```

Teste acelerado de ciclos (a TV reinicia várias vezes):

```bash
tools/takeover_cycle_test.sh 192.168.1.2:5555 15 15
```

### Feature 004, ativação

- Sem ativação, a tela inicial mostra o painel de ativação com o **código do aparelho** (12 números). O revendedor gera a **senha de ativação** (12 números) desse código e a pessoa digita a senha na TV. A ativação é perpétua e funciona sem internet.
- `activation-core/`: cálculo do código e da senha (algoritmo em `_reversa_forward/004-license-activation/interfaces/senha-ativacao.md`).
- `activation-tool/`: ferramenta do revendedor no notebook.

```bash
./gradlew :activation-tool:installDist
activation-tool/build/install/activation-tool/bin/activation-tool gerar-segredo        # uma única vez
activation-tool/build/install/activation-tool/bin/activation-tool senha 1234-5678-9012 --cliente "Igreja X"
```

O segredo fica em `~/.config/tematv/activation-secret`, fora do repositório, e é embutido no app na compilação (o build para se ele não existir). **Faça uma cópia de segurança desse arquivo**: sem ele não é possível ativar novas TVs com o mesmo APK. As senhas geradas ficam registradas em `~/tematv/ativacoes.csv`.

Ativar pelo ADB, sem digitar: `_reversa_forward/004-license-activation/interfaces/ativacao-adb.md`.

## Compilar e testar

Pré-requisitos: JDK 17 e Android SDK (plataforma 34). O Gradle 8.7 vem pelo wrapper do projeto; crie `local.properties` com `sdk.dir=<caminho do SDK>`.

```bash
./gradlew :theme-core:test :activation-core:test :app:testDebugUnitTest
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

Versículos: o tema padrão e o exemplo bíblico trazem uma lista pronta de 20 versículos da Nova Versão Internacional (NVI), de vários livros, com a atribuição exigida pela Biblica. O texto foi copiado da BibleGateway (NVI-PT, © 1993, 2000 Biblica, Inc.). A Biblica permite citar até 500 versículos sem pedido formal, desde que com a atribuição; confirme com a Biblica antes de vender o produto em escala.
