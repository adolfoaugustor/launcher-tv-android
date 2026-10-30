# Requirements: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`
> Pasta da extração reversa: `_reversa_sdd/` (projeto novo: fontes são o PRD, as specs SDD e os adendos vigentes 001 a 005)
> Branch: `feature/006-device-provisioning` (feature 6 de 6, criada a partir de `feature/005-theme-builder`)
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA / DÚVIDA

## 1. Resumo executivo

Esta feature acrescenta à página `tematv` a aba "Instalar na TV": o revendedor escolhe o cliente, digita o IP da TV e, num clique, a ferramenta conecta pelo ADB, instala o launcher, libera o início automático, envia o tema do cliente, ativa a TV com a senha gerada na hora, liga a troca da tela inicial e confere que a tela temática aparece, mostrando uma lista de verificação e gravando um registro da instalação. Hoje tudo isso exige uma dúzia de comandos digitados, que só funcionaram na TV de teste porque foram descobertos um a um nas features 001 a 004.

## 2. Contexto a partir do legado

Não há sistema legado. Fontes: artefatos do `/reversa-new` (🟡), adendos vigentes e o que foi verificado na TV de referência (🟢).

| Fonte | Trecho relevante | Confidência |
|-------|------------------|-------------|
| `_reversa_sdd/sdd/device-provisioning.md#61-requisitos-principais` | Conectar, autorizar, instalar, primeira abertura, tema, licença, acessibilidade, conferência, lista de verificação e registro | 🟡 |
| `_reversa_sdd/addenda/001-theme-engine.md#impacto-por-artefato-da-extração` | O tema entra por `content write` na importação do ADB, não por cópia para a pasta do app; recarga por broadcast | 🟢 |
| `_reversa_sdd/addenda/003-home-takeover.md#impacto-por-artefato-da-extração` | Na TCL é preciso `appops set br.tematv.launcher APP_AUTO_START allow`; acrescentar o serviço à lista de acessibilidade sem remover os outros; em modo de espera a TV some da rede | 🟢 |
| `_reversa_sdd/addenda/004-license-activation.md#impacto-por-artefato-da-extração` | A licença é uma senha: ler `device_code.txt` e gravar `activation_code.txt` pela importação; logo após `adb install -r` a importação pode demorar alguns segundos | 🟢 |
| `_reversa_sdd/addenda/005-theme-builder.md#impacto-por-artefato-da-extração` | A aba pode reaproveitar o servidor local e os pacotes de `~/tematv/temas/`; a senha vem do mesmo cálculo da aba "Ativar TV" | 🟢 |
| `_reversa_forward/003-home-takeover/interfaces/ativacao-acessibilidade-adb.md` | Comandos para ligar o serviço preservando os outros e conferência por `dumpsys accessibility` | 🟢 |
| `_reversa_forward/004-license-activation/interfaces/ativacao-adb.md` | Comandos da ativação pelo ADB e resposta no log `TemaTV` | 🟢 |

## 3. Personas e cenários de uso

| Persona | Objetivo | Cenário-chave |
|---------|----------|---------------|
| Revendedor e instalador (`_reversa_sdd/personas.md#persona-1-revendedor-e-instalador`) | Instalar e personalizar uma TV em menos de 10 minutos | Na igreja, com o notebook na mesma rede, escolhe "Igreja Batista X", digita o IP, aceita o aviso na TV e acompanha a lista até "TV pronta" |
| Revendedor e instalador | Trocar o tema de uma TV já instalada | Gera o pacote novo e repete a instalação: o que já está certo na TV é mantido |
| Revendedor e instalador | Atender um chamado | Abre o registro da instalação daquela TV e vê o que deu certo e o que falhou |

## 4. Regras de negócio novas ou alteradas

1. **RN-01:** A instalação só executa os comandos desta feature: instalar ou atualizar o launcher, liberar o início automático, enviar tema e senha pela importação, ligar o serviço de acessibilidade e conferir. Nunca apaga dados, desativa apps (o launcher do Google inclusive) nem muda outras configurações da TV. 🟡
   - Origem: `_reversa_sdd/sdd/device-provisioning.md#7-requisitos-não-funcionais` (RNF-02)
   - Tipo: nova
2. **RN-02:** Os serviços de acessibilidade já ligados na TV continuam ligados depois da instalação. 🟢
   - Origem: `_reversa_forward/003-home-takeover/interfaces/ativacao-acessibilidade-adb.md`
   - Tipo: nova
3. **RN-03:** Repetir a instalação na mesma TV leva ao mesmo estado final, sem duplicar o serviço na lista nem reinstalar o launcher da mesma versão. 🟡
   - Origem: `_reversa_sdd/sdd/device-provisioning.md#7-requisitos-não-funcionais` (RNF-03)
   - Tipo: nova
4. **RN-04:** Todo comando ADB vai para a TV escolhida (`-s <ip>:5555`), nunca para outro aparelho conectado. 🟡
   - Origem: `_reversa_sdd/sdd/device-provisioning.md#61-requisitos-principais` (RF-05)
   - Tipo: nova
5. **RN-05:** O registro da instalação guarda cliente, IP, fabricante, modelo, versão do Android, código do aparelho e o resultado de cada etapa; nunca o segredo de ativação nem a senha. 🟢
   - Origem: `_reversa_forward/005-theme-builder/requirements.md` (RN-04)
   - Tipo: alterada (a spec previa a chave privada e a chave da Bíblia API)
6. **RN-06:** O APK de produção é assinado com a chave definitiva do revendedor, criada uma vez e guardada fora do git (como o segredo de ativação), com aviso de cópia de segurança; o código do aparelho depende dessa chave. 🟢
   - Origem: esclarecimento de 2026-10-30
   - Tipo: nova
7. **RN-07:** A depuração por rede fica ligada depois da instalação, para trocar tema e atualizar o launcher pela mesma aba. 🟢
   - Origem: esclarecimento de 2026-10-30 (`_reversa_sdd/sdd/device-provisioning.md#14-open-questions`, OQ-03)
   - Tipo: nova

## 5. Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de aceite | Confidência |
|----|-----------|------------|--------------------|-------------|
| RF-01 | A aba deve conferir se o `adb` está instalado no notebook e, se não estiver, mostrar `sudo apt install -y adb` e desabilitar "Instalar". | Must | Sem `adb`, a aba mostra o comando e "Instalar" fica desabilitado. | 🟡 |
| RF-02 | O usuário deve escolher um cliente que já tenha pacote gerado e digitar o IP da TV, validado como IPv4. | Must | `1922.168.1.2` é recusado com "IP inválido"; cliente sem pacote pede para gerar o pacote primeiro. | 🟢 |
| RF-03 | Antes de qualquer etapa, a aba deve pedir "Ligue a TV com o controle e deixe-a na tela inicial" e testar a porta 5555 da TV (até 5 s), mostrando "TV encontrada em <ip>" ou "TV desligada, fora da rede ou sem depuração por rede", com o passo a passo para ligar a depuração e o botão "Verificar de novo". | Must | Com a TV em modo de espera, a aba mostra a mensagem de não encontrada e "Instalar" fica desabilitado. | 🟢 |
| RF-04 | A instalação deve conectar (`adb connect <ip>:5555`), pedir em destaque que o usuário aceite "Permitir depuração?" na TV marcando "Sempre permitir" e aguardar a autorização por até 120 s. | Must | Após aceitar na TV, a etapa fica verde; sem aceitar em 120 s, mostra o erro de autorização. | 🟡 |
| RF-05 | A instalação deve instalar ou atualizar o launcher com `adb install -r` usando o APK assinado com a chave definitiva (RN-06), pulando a etapa quando a mesma versão já estiver instalada; se a TV tiver o launcher assinado com outra chave, a etapa para com a explicação e oferece "Desfazer e instalar de novo" (a TV precisará de nova ativação). | Must | Na segunda instalação seguida, a etapa aparece como "já instalado"; na TV com o APK de teste, aparece a oferta de reinstalar. | 🟢 |
| RF-06 | A instalação deve liberar o início automático do launcher (`appops set br.tematv.launcher APP_AUTO_START allow`), marcando a etapa como "não se aplica" nas TVs que não têm essa opção. | Must | Na TCL, `appops get` mostra `allow`; em TV sem a opção, a instalação segue. | 🟢 |
| RF-07 | A instalação deve iniciar o app sem abrir telas, ler o código do aparelho pela importação (`device_code.txt`) e repetir a leitura por até 30 s enquanto a importação não responde. | Must | O código lido é igual ao da tela de ativação da TV. | 🟢 |
| RF-08 | A instalação deve enviar o `.tematv` do cliente e o `active_theme.txt` pela importação e recarregar o tema. | Must | Ao final, a tela inicial da TV mostra o tema do cliente. | 🟢 |
| RF-09 | A instalação deve gerar a senha do código lido (mesmo cálculo e registro da aba "Ativar TV", com o nome do cliente), enviá-la por `activation_code.txt` e conferir no log `TemaTV` a mensagem "Ativação: aceita". | Must | Ao final, a TV está ativada e `~/tematv/ativacoes.csv` tem a linha do cliente. | 🟢 |
| RF-10 | A instalação deve ligar o serviço de acessibilidade acrescentando-o à lista sem remover os existentes nem duplicá-lo, gravar `accessibility_enabled 1` e conferir por `dumpsys accessibility` que o serviço está ligado. | Must | Um serviço que já estava ligado continua na lista; reinstalar não duplica a entrada. | 🟢 |
| RF-11 | A instalação deve conferir o resultado enviando a tecla Home e verificando, em até 5 s, que a janela em foco é do `br.tematv.launcher`. | Must | A última etapa só fica verde com a tela temática em primeiro plano; senão mostra o app encontrado. | 🟢 |
| RF-12 | A aba deve mostrar uma linha por etapa (TV encontrada, conectar e autorizar, instalar, início automático, código do aparelho, tema, ativação, troca da tela inicial, conferência) com pendente, em andamento, ok, pulada ou erro, o tempo decorrido e, no erro, a mensagem e "Tentar de novo a partir desta etapa". | Must | Ao desligar a TV no meio, a etapa em curso fica vermelha com a mensagem e o botão de repetir. | 🟡 |
| RF-13 | Cada tentativa deve gravar `~/tematv/instalacoes/<AAAA-MM-DD>-<ip>.log`, uma linha JSON por etapa, com os dados de RN-05 e o tempo. | Must | O arquivo existe após cada tentativa, com sucesso ou erro, e não contém a senha. | 🟡 |
| RF-14 | O usuário deve poder desfazer a instalação numa TV: remover o serviço da lista de acessibilidade (mantendo os outros) e desinstalar o launcher. | Should | Após desfazer, apertar Home mostra o launcher do Google. | 🟡 |
| RF-15 | A compilação do APK de produção deve usar a chave definitiva do revendedor em `~/.config/tematv/`; a ferramenta cria a chave uma única vez (recusa se já existir) e avisa da cópia de segurança. Sem a chave, a compilação de produção para com a orientação, como o segredo de ativação. | Must | O APK de produção é assinado com a chave definitiva; a chave não aparece em nenhum arquivo versionado. | 🟢 |

## 6. Requisitos Não Funcionais

| Tipo | Requisito | Evidência ou justificativa | Confidência |
|------|-----------|----------------------------|-------------|
| Desempenho | Do clique em "Instalar" até a conferência em até 5 min em Wi-Fi local, sem contar a autorização na TV | `_reversa_sdd/sdd/device-provisioning.md#3-goals-objetivos` | 🟡 |
| Segurança | Só os comandos de RN-01; segredo e senha fora do registro | RN-01, RN-05 | 🟢 |
| Idempotência | Repetir a instalação não quebra nem duplica nada | RN-03 | 🟡 |
| Rastreabilidade | 100% das tentativas com registro | `_reversa_sdd/sdd/device-provisioning.md#7-requisitos-não-funcionais` (RNF-04) | 🟡 |
| Usabilidade | Nenhum comando digitado pelo revendedor; mensagens em português, sem termos técnicos | Persona iniciante | 🟢 |
| Compatibilidade | TV de referência TCL (Android 11); nas demais marcas, as etapas específicas da TCL viram "não se aplica" | PRD: funcionar em outras marcas | 🟡 |

## 7. Critérios de Aceitação

```gherkin
Cenário: Instalação completa numa TV nova
  Dado o pacote do cliente gerado e a TV ligada na mesma rede, com a depuração por rede ligada
  Quando o revendedor escolhe o cliente, digita o IP e clica em "Instalar"
  E aceita o aviso de depuração na TV
  Então todas as etapas ficam verdes em até 5 minutos
  E a TV mostra a tela temática do cliente, ativada
  E o registro da instalação é gravado

Cenário: TV desligada
  Dado a TV em modo de espera
  Quando o revendedor verifica a TV
  Então a aba mostra que a TV não foi encontrada e "Instalar" fica desabilitado

Cenário: IP inválido
  Dado a aba aberta
  Quando o revendedor digita 1922.168.1.2
  Então aparece "IP inválido"

Cenário: Autorização não aceita
  Dado a TV pedindo "Permitir depuração?"
  Quando ninguém aceita em 120 s
  Então a etapa fica vermelha com a explicação e o botão de repetir

Cenário: Outros serviços de acessibilidade preservados
  Dado a TV com outro serviço de acessibilidade ligado
  Quando a instalação termina
  Então os dois serviços estão ligados

Cenário: Reinstalar na mesma TV
  Dado uma TV já instalada com este launcher
  Quando o revendedor instala de novo com um tema novo
  Então a instalação do app aparece como "já instalado", o serviço não é duplicado e o tema novo aparece

Cenário: Registro sem a senha
  Dado uma instalação concluída
  Quando o revendedor abre o registro
  Então ele tem o código do aparelho e o resultado de cada etapa, sem a senha nem o segredo

Cenário: TV com o APK de teste
  Dado a TV de teste com o launcher assinado com a chave de depuração
  Quando o revendedor instala com o APK de produção
  Então a etapa explica a assinatura diferente e oferece desfazer e instalar de novo
  E depois de reinstalar a TV mostra um código do aparelho novo e é ativada com a senha nova

Cenário: Desfazer
  Dado uma TV instalada
  Quando o revendedor escolhe "Desfazer instalação"
  Então o launcher é removido e o Home volta a mostrar o launcher do Google
```

## 8. Prioridade MoSCoW

| Item | MoSCoW | Justificativa |
|------|--------|---------------|
| RF-01 a RF-13 | Must | Sem eles a instalação não é feita num clique |
| RF-14 | Should | Suporte e devolução da TV |
| RF-15 | Must | Sem a chave definitiva, cada troca de chave invalida as ativações |
| Kit de pendrive | Won't (nesta versão) | Sem o ADB o tema do cliente não entra no app (esclarecimento de 2026-10-30) |
| RNF de desempenho | Should | Meta de 10 minutos por TV |

## 9. Esclarecimentos

### Sessão 2026-10-30

- **Q:** Kit de pendrive: sem o ADB o tema do cliente não entra no app. O que fazer?
  **R:** Fora por agora; a instalação é só pelo ADB na rede. Aplicado em §8 (Won't) e na troca do RF-15.
- **Q:** Ao final da instalação, desligar a depuração por rede da TV?
  **R:** Deixar ligada, para trocar tema e atualizar depois pela mesma aba. Aplicado em RN-07.
- **Q:** Criar agora a chave definitiva de assinatura do APK?
  **R:** Criar agora, fora do git e com aviso de backup. A TV de teste muda de código uma vez e recebe senha nova. Aplicado em RN-06, RF-05, RF-15 e no cenário "TV com o APK de teste".
- **Q:** Posso rodar a instalação completa na TV de teste?
  **R:** Pode, avisando antes: pedir para ligar a TV e confirmar antes de começar. Vale para o teste desta feature; nas demais atualizações continua a regra de só instalar.

## 10. Lacunas

- Nenhuma lacuna aberta.

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-requirements` | reversa |
| 2026-10-30 | Sessão de esclarecimentos: sem kit de pendrive, depuração ligada, chave definitiva de assinatura, teste completo na TV com aviso | reversa-clarify |
