# Instalação do notebook e conexão com a TV

Guia para preparar um notebook do zero (Windows com WSL ou Ubuntu) e conectar uma TV Android/Google TV pelo ADB. Depois disso, o uso do dia a dia é só a página `tematv`.

## 1. O que instalar e para que serve

| Ferramenta | Versão | Para que serve | Como instalar |
|------------|--------|----------------|---------------|
| WSL com Ubuntu (só no Windows) | Ubuntu 24.04 | Linux dentro do Windows, onde tudo roda | §2 |
| Java (JDK) | 17 | Compila o launcher e roda a ferramenta `tematv` | `sudo apt install -y openjdk-17-jdk` |
| ADB (Android Debug Bridge) | 34 ou mais novo | Conversa com a TV pela rede: instala o app, envia o tema, ativa | `sudo apt install -y adb` |
| Android SDK (linha de comando) | plataforma 34, build-tools 34.0.0 | Bibliotecas para compilar o app da TV e `apksigner` | §3 |
| Gradle | 8.7 | Ferramenta de compilação | **Não instale**: o projeto já traz o `./gradlew`, que baixa o Gradle 8.7 sozinho na primeira vez |
| Git | qualquer | Baixar o projeto do GitHub | `sudo apt install -y git` |
| Python 3 e unzip | os do Ubuntu | Scripts auxiliares | `sudo apt install -y python3 unzip` |

Tudo de uma vez, no terminal do Ubuntu:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk adb git python3 unzip
```

Conferir:

```bash
java -version    # deve mostrar "17"
adb version      # deve mostrar "Android Debug Bridge"
```

## 2. WSL (só no Windows)

No PowerShell do Windows **como administrador**:

```powershell
wsl --install -d Ubuntu-24.04
```

Reinicie o Windows, abra "Ubuntu" no menu Iniciar e crie o usuário e a senha do Linux. Todos os comandos deste guia rodam nesse terminal do Ubuntu.

- Arquivos baixados pelo navegador do Windows ficam em `/mnt/c/Users/<seu usuário>/Downloads/`.
- A página `tematv` abre no navegador do Windows sozinha.

## 3. Android SDK (uma vez)

1. Baixe "Command line tools only" para **Linux** em <https://developer.android.com/studio#command-line-tools-only>.
2. Descompacte no lugar esperado:

   ```bash
   mkdir -p ~/android-sdk/cmdline-tools
   unzip /mnt/c/Users/<seu usuário>/Downloads/commandlinetools-linux-*_latest.zip -d ~/android-sdk/cmdline-tools
   mv ~/android-sdk/cmdline-tools/cmdline-tools ~/android-sdk/cmdline-tools/latest
   ```

3. Acrescente ao fim do `~/.bashrc` (ou `~/.zshrc`, se usar zsh) e abra um terminal novo:

   ```bash
   export ANDROID_HOME=$HOME/android-sdk
   export PATH=$HOME/.local/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
   ```

4. Baixe os pacotes e aceite as licenças:

   ```bash
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   yes | sdkmanager --licenses
   ```

## 4. Projeto

```bash
mkdir -p ~/projetos && cd ~/projetos
git clone git@github.com:adolfoaugustor/launcher-tv-android.git
cd launcher-tv-android/launcher/android
echo "sdk.dir=$HOME/android-sdk" > local.properties
./gradlew --version    # na primeira vez baixa o Gradle 8.7
```

O `git clone` por SSH exige a chave SSH do notebook cadastrada no GitHub (Settings › SSH and GPG keys).

## 5. Segredos do revendedor

São três arquivos em `~/.config/tematv/`, fora do git. Eles definem quais TVs você consegue ativar e atualizar.

| Arquivo | Para que serve |
|---------|----------------|
| `activation-secret` | Gerar as senhas de ativação |
| `assinatura.jks` e `assinatura.properties` | Assinar o launcher; o código do aparelho depende dessa chave |

**Notebook novo de um revendedor que já tem clientes:** copie os três arquivos do backup e proteja-os:

```bash
mkdir -p ~/.config/tematv
cp /caminho/do/backup/{activation-secret,assinatura.jks,assinatura.properties} ~/.config/tematv/
chmod 600 ~/.config/tematv/*
```

Se `assinatura.properties` foi gerado em outro notebook, confira se a linha `storeFile=` aponta para o `assinatura.jks` deste notebook.

**Primeira vez de todas (nenhuma TV instalada ainda):** gere os segredos uma única vez e guarde cópia num pendrive ou cofre de senhas:

```bash
./gradlew -q :activation-tool:installDist
activation-tool/build/install/activation-tool/bin/activation-tool gerar-segredo
tools/gerar-chave-assinatura.sh
```

Gerar segredos novos depois de ter clientes impede ativar e atualizar as TVs já instaladas.

## 6. Instalar a ferramenta `tematv`

```bash
tools/instalar-tematv.sh
tematv
```

O script compila a ferramenta, cria o comando `tematv` e prepara o launcher de produção em `~/tematv/launcher/`. Rode de novo a cada atualização do projeto (`git pull`).

## 7. Preparar a TV (uma vez por TV)

1. **Opções do desenvolvedor:** Configurações › Preferências do dispositivo (ou Sistema) › Sobre › clique 7 vezes em **"Build"** até aparecer "Você é um desenvolvedor".
2. Volte e abra **Opções do desenvolvedor**. Ligue **"Depuração USB"** e **"Depuração por rede"** (em algumas TVs o nome é "ADB pela rede" ou "Depuração sem fio").
3. **IP da TV:** Configurações › Rede e Internet › clique na rede conectada. Anote o endereço, por exemplo `192.168.1.2`.
4. O notebook e a TV precisam estar **na mesma rede** (o mesmo Wi-Fi ou roteador).

Dica: no roteador da igreja, reserve o IP da TV (reserva de DHCP). Assim o IP não muda e a troca de tema depois continua funcionando.

## 8. Conectar e instalar

Pela página (recomendado): `tematv` › aba **"Instalar na TV"** › escolha o cliente › digite o IP › **"Verificar TV"** › **"Instalar"**. Quando a TV mostrar **"Permitir depuração?"**, marque **"Sempre permitir neste computador"** e escolha **OK**.

Pelo terminal, para conferir a conexão à mão:

```bash
adb connect 192.168.1.2:5555
adb devices
```

O resultado esperado é `192.168.1.2:5555   device`.

| O que aparece | Significado e o que fazer |
|---------------|---------------------------|
| `device` | Conectado |
| `unauthorized` | Aceite "Permitir depuração?" na tela da TV |
| `offline` | Rode `adb disconnect 192.168.1.2:5555` e conecte de novo |
| `failed to connect` ou `Connection refused` | TV desligada, IP errado, rede diferente ou depuração por rede desligada |
| Sem resposta com a TV em modo de espera | Na TCL a rede desliga em espera: ligue a TV pelo controle |

## 9. Dia a dia

- **Cliente novo:** `tematv` › Clientes › "Novo cliente" › preencher › "Gerar pacote" › aba "Instalar na TV".
- **Trocar o tema de uma TV:** gere o pacote de novo e rode "Instalar" na mesma TV. O que já está certo é pulado.
- **Ativar uma TV por telefone:** aba "Ativar TV", digite o código que aparece na TV e dite a senha.
- **Registros:** senhas em `~/tematv/ativacoes.csv`; instalações em `~/tematv/instalacoes/`.
