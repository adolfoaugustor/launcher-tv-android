# Investigation: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`

## 1. O que já existe e será reaproveitado

| Peça | Onde | Uso na feature |
|------|------|----------------|
| `ThemePackageLoader.load(packageFile, extractionDir)` | `launcher/android/theme-core/.../ThemePackageLoader.kt` | Valida o pacote gerado com as regras do launcher (D-05) |
| `ThemeError` (`code`, `field`, `detail`) | `theme-core/.../ThemeManifest.kt` | Mensagem de erro por campo na página |
| `ImageHeaderReader.read(file)` | `theme-core/.../ImageHeaderReader.kt` | Formato e tamanho de PNG, JPEG e WEBP (D-07) |
| `ActivationDigits`, `ActivationCode` | `activation-core/` | Senha de ativação (D-08) |
| Lista de 20 versículos NVI e atribuição | `app/src/main/assets/default-theme/theme.json` | Lista inicial dos clientes de igreja (D-06) |
| Molduras e áreas | `sample-themes/biblia-exemplo/`, `sample-themes/hamburguer-exemplo/` | Molduras prontas (D-06) |
| Layout da tela inicial | `app/src/main/res/layout/activity_home.xml`, `res/values/dimens.xml` | Medidas da prévia (RF-07) |

`ManifestParser` é `internal` no `:theme-core`; a ferramenta não o chama: monta o JSON com as próprias classes serializáveis e confia no `ThemePackageLoader` para recusar o que estiver errado.

## 2. Verificações feitas

- **Compilar sem o segredo** (2026-10-30): `./gradlew -Ptematv.activationSecretFile=/nao-existe :activation-tool:installDist` falha na configuração do `:app`. Conclusão: D-11 é necessário, senão o comando que gera o segredo não compila num notebook novo.
- **Ambiente do notebook:** WSL2 (`/proc/sys/fs/binfmt_misc/WSLInterop` ativo), `cmd.exe` disponível, `wslview` ausente, `xdg-open` presente (sem navegador no Linux), OpenJDK 17.0.20, Python 3.12.
- **`javax.imageio`:** lê e grava PNG e JPEG; não lê WEBP sem plugin. Daí D-07.

## 3. Alternativas avaliadas

| Tema | Escolhida | Descartadas e motivo |
|------|-----------|----------------------|
| Linguagem | Kotlin JVM | Python: reescrever a validação do pacote e a senha, com risco de divergir do launcher |
| Servidor | `HttpServer` do JDK | Ktor ou Jetty: dependência nova sem ganho para meia dúzia de rotas |
| Front-end | HTML, CSS e JavaScript puro | Framework com npm: etapa de build e instalação extra para o revendedor |
| Envio de imagens | base64 em JSON | multipart: interpretar à mão no `HttpServer` |
| Bíblia API | Fora | Chave não obtida; lista NVI pronta (adendo 001, atualização) |

## 4. Segurança do servidor local

Um servidor em `127.0.0.1` continua alcançável por páginas abertas no mesmo navegador: um site pode enviar `POST` para `http://localhost:8787` e, com DNS rebinding, ler a resposta. As defesas da D-03 são as recomendadas para ferramentas locais: conferir o cabeçalho `Host` (derruba o rebinding), conferir `Origin` em escritas e exigir `Content-Type: application/json` (obriga o navegador a fazer a pré-consulta CORS, que o servidor não autoriza).

## 5. WSL2 e o navegador do Windows

No WSL2 com rede NAT (padrão), o Windows encaminha `localhost` para serviços do Linux, inclusive os presos a `127.0.0.1`. No modo espelhado também funciona. Fica como verificação do onboarding (premissa do roadmap).
