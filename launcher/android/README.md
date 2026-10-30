# Launcher Temático (Android TV)

App Android TV que troca a tela inicial da TV por um tema da igreja ou do cliente.

**Começando do zero?** Veja [INSTALACAO.md](INSTALACAO.md): o que instalar no notebook (Java 17, ADB, Android SDK; o Gradle vem pelo `./gradlew`), os segredos do revendedor e como preparar e conectar a TV.

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

O segredo fica em `~/.config/tematv/activation-secret`, fora do repositório, e é embutido no app na compilação (a compilação do app para se ele não existir; as ferramentas do notebook compilam sem ele). **Faça uma cópia de segurança desse arquivo**: sem ele não é possível ativar novas TVs com o mesmo APK. As senhas geradas ficam registradas em `~/tematv/ativacoes.csv`.

Ativar pelo ADB, sem digitar: `_reversa_forward/004-license-activation/interfaces/ativacao-adb.md`.

### Feature 005, ferramenta de preparo de tema

- `theme-builder/`: comando `tematv`, que abre no navegador uma página só do próprio notebook. Nela o revendedor cadastra cada cliente (igreja ou loja), envia logotipo, fundo e moldura, escolhe as cores, escreve a mensagem e os versículos (NVI) ou as mensagens da loja, confere a prévia da tela da TV e gera o pacote `.tematv`. O pacote só é salvo se passar pela mesma conferência do launcher.
- A aba "Ativar TV" gera a senha de ativação a partir do código do aparelho, com o mesmo cálculo e o mesmo registro do `activation-tool`.
- Projetos em `~/tematv/clientes/<identificador>/`, pacotes em `~/tematv/temas/<identificador>.tematv`.
- A lista NVI e as molduras prontas vêm do tema padrão e de `sample-themes/`, copiadas na compilação.

```bash
tools/instalar-tematv.sh   # uma vez, e de novo a cada atualização
tematv                     # abre a página (no WSL, no navegador do Windows)
```

Depois de gerar o pacote, a página mostra os comandos do ADB para enviá-lo à TV.

### Feature 006, instalação na TV

- Aba "Instalar na TV" da página `tematv`: escolha o cliente, digite o IP da TV e clique em "Instalar". A ferramenta conecta pelo ADB, instala o launcher de produção, libera o início automático (TCL), envia o tema, ativa a TV com a senha gerada na hora, liga a troca da tela inicial (preservando outros serviços de acessibilidade) e confere a tela temática depois do Home. Cada etapa pode ser repetida; cada tentativa fica registrada em `~/tematv/instalacoes/<data>-<ip>.log` (sem a senha).
- O app publica `app_status.json` pela importação do ADB (versão, assinatura, ativação e tema aplicado), usado para conferir cada etapa.
- O APK de produção é assinado com a **chave definitiva do revendedor**, criada uma vez:

```bash
tools/gerar-chave-assinatura.sh   # uma única vez
tools/instalar-tematv.sh          # compila a ferramenta e o launcher de produção (~/tematv/launcher/)
```

**Faça cópia de segurança** de `~/.config/tematv/assinatura.jks`, `~/.config/tematv/assinatura.properties` e `~/.config/tematv/activation-secret`. O código do aparelho depende da chave: perdê-la impede atualizar as TVs instaladas, e trocá-la exige reativar todas. Sem a chave, só a compilação de release (`:app:assembleRelease`) para.

Na TV, uma vez: Configurações › Preferências do dispositivo › Sobre › 7 cliques em "Build"; em Opções do desenvolvedor, ligar "Depuração USB" e "Depuração por rede". A depuração fica ligada depois da instalação, para trocar o tema pela mesma aba.

## Compilar e testar

Pré-requisitos: JDK 17 e Android SDK (plataforma 34). O Gradle 8.7 vem pelo wrapper do projeto; crie `local.properties` com `sdk.dir=<caminho do SDK>`. Passo a passo em [INSTALACAO.md](INSTALACAO.md).

```bash
./gradlew :theme-core:test :activation-core:test :theme-builder:test :app:testDebugUnitTest
./gradlew :app:assembleDebug :app:assembleRelease :app:packSampleThemes
```

Instalação das ferramentas e conexão com a TV, passo a passo: [INSTALACAO.md](INSTALACAO.md).

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

	
Código do aparelho (aparece na TV)	2544-3298-7734
Senha para digitar	8065-4698-2216