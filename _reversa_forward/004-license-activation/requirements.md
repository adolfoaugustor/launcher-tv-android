# Requirements: Ativação por licença (license-activation)

> Identificador: `004-license-activation`
> Data: `2026-10-30`
> Pasta da extração reversa: `_reversa_sdd/` (projeto novo: fontes são o PRD, as specs SDD e os adendos vigentes 001, 002 e 003)
> Branch: `feature/004-license-activation` (feature 4 de 6, criada a partir de `feature/003-home-takeover`)
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA / DÚVIDA

## 1. Resumo executivo

Esta feature faz cada TV exibir o tema só depois de ativada. A TV mostra um código do aparelho de 12 números; o revendedor gera no notebook a senha de ativação daquele código (também de 12 números) e a pessoa digita a senha na TV com o controle, ativando na hora, sem internet. A ativação é perpétua. Sem ativação, a TV mostra a tela de ativação no lugar da grade temática, inclusive ao apertar Home. A geração da senha nesta feature é um comando no notebook; a página da feature 005 passa a oferecê-la depois.

## 2. Contexto a partir do legado

Não há sistema legado. Fontes: artefatos do `/reversa-new` (🟡), adendos vigentes e respostas do usuário (🟢).

| Fonte | Trecho relevante | Confidência |
|-------|------------------|-------------|
| `_reversa_sdd/prd.md#4-escopo-in` | Ativação por licença no MVP | 🟡 |
| `_reversa_sdd/sdd/license-activation.md#6-requisitos-funcionais` | Código do aparelho, ativação offline, tela de ativação, registro das ativações | 🟡 |
| `_reversa_sdd/sdd/license-activation.md#4-non-goals-fora-do-escopo` | NG-03: sem proteção contra engenharia reversa do APK (risco aceito) | 🟡 |
| `_reversa_sdd/addenda/001-theme-engine.md#impacto-por-artefato-da-extração` | Arquivos entram na TV pela importação do ADB (`content write`/`content read`) | 🟢 |
| `_reversa_sdd/addenda/003-home-takeover.md#impacto-por-artefato-da-extração` | Ponto de licença `LicenseGate` existe e responde sempre "válida" | 🟢 |
| Sessão de esclarecimentos de 2026-10-30 (seção 9) | Ativação perpétua por senha de 12 números digitada na TV, gerada no notebook | 🟢 |

## 3. Personas e cenários de uso

| Persona | Objetivo | Cenário-chave |
|---------|----------|---------------|
| Revendedor e instalador (`_reversa_sdd/personas.md#persona-1-revendedor-e-instalador`) | Liberar só as TVs vendidas | Recebe o código do aparelho (no local ou por telefone), gera a senha no notebook e a passa ao cliente |
| Público da TV (responsável da igreja ou da família) | Ativar a TV na hora | Digita com o controle a senha recebida e a tela temática aparece |

## 4. Regras de negócio novas ou alteradas

1. **RN-01:** A senha de ativação só vale para a TV cujo código do aparelho a originou. 🟢
   - Origem: `_reversa_sdd/sdd/license-activation.md#3-goals-objetivos` (G-01)
   - Tipo: nova
2. **RN-02:** Só o revendedor gera senhas válidas, com um segredo guardado no notebook e embutido no app na compilação; o segredo nunca vai para o git, para pacotes de tema nem para logs. Quem desmontar o app pode extrair o segredo, risco aceito em `_reversa_sdd/sdd/license-activation.md#4-non-goals-fora-do-escopo` (NG-03). 🟢
   - Origem: esclarecimento de 2026-10-30
   - Tipo: alterada (a spec previa assinatura assimétrica com arquivo de licença; trocada por senha curta digitável)
3. **RN-03:** A ativação é perpétua e funciona sem internet. 🟢
   - Origem: esclarecimento de 2026-10-30
   - Tipo: alterada (a spec previa validade opcional)
4. **RN-04:** Sem ativação, apertar Home mostra a tela de ativação, nunca a grade temática nem a tela do Google. 🟡
   - Origem: `_reversa_sdd/sdd/license-activation.md#6-requisitos-funcionais` (RF-08)
   - Tipo: alterada (na feature 003 a troca ignorava o Home com licença inválida)
5. **RN-05:** Depois de 5 senhas erradas seguidas, a tela de ativação espera 30 s antes de aceitar nova tentativa. 🟡
   - Origem: proteção contra tentativa e erro com o controle
   - Tipo: nova

## 5. Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de aceite | Confidência |
|----|-----------|------------|--------------------|-------------|
| RF-01 | O app deve calcular o código do aparelho, com 12 números no formato `XXXX-XXXX-XXXX`, a partir de um identificador do Android estável para o app naquela TV. | Must | O código é o mesmo após reiniciar e após reinstalar o app com a mesma assinatura. | 🟢 |
| RF-02 | O app deve exibir o código do aparelho na tela de ativação e no diagnóstico, e permitir lê-lo pelo ADB (`content read` da importação, nome `device_code.txt`). | Must | O código lido pelo ADB é igual ao exibido na tela. | 🟢 |
| RF-03 | A ferramenta do notebook deve gerar o segredo de ativação do revendedor uma única vez, guardá-lo com acesso só do usuário e entregá-lo à compilação do app sem colocá-lo no git. | Must | Gerar de novo com o segredo já existente é recusado; o arquivo tem permissão `600`; o segredo não aparece em nenhum arquivo versionado. | 🟢 |
| RF-04 | A ferramenta do notebook deve gerar a senha de ativação de 12 números (`XXXX-XXXX-XXXX`) para um código do aparelho informado, aceitando o código com ou sem traços. | Must | A senha gerada ativa a TV dona do código. | 🟢 |
| RF-05 | O usuário deve poder digitar a senha na tela de ativação com o controle (teclado numérico da TV) e confirmar com "Ativar". | Must | Digitar a senha correta e confirmar mostra a grade temática em até 2 s. | 🟢 |
| RF-06 | O app deve recusar senha errada com "Senha incorreta. Confira os números com o seu revendedor." e, após 5 erros seguidos, esperar 30 s (RN-05). | Must | A 6ª tentativa logo após 5 erros mostra a contagem de espera. | 🟡 |
| RF-07 | O app deve guardar a ativação e reconferi-la a cada abertura, recalculando a senha esperada para o código do aparelho. | Must | Após reiniciar a TV, ela continua ativada; se o código do aparelho mudar (restauração de fábrica), volta a pedir ativação. | 🟡 |
| RF-08 | O app deve mostrar, sem ativação, a tela de ativação no lugar da grade temática, com o código do aparelho em destaque, o campo da senha, a instrução "Peça a senha de ativação ao seu revendedor informando este código" e o botão "Configurações". | Must | Sem ativação, a tela inicial mostra a ativação; ativada, a grade temática. | 🟢 |
| RF-09 | A troca da tela inicial deve abrir a tela de ativação quando a TV não estiver ativada (RN-04), usando a verificação real no lugar da licença sempre válida. | Must | Sem ativação, apertar Home num app mostra a tela de ativação em até 1,5 s. | 🟡 |
| RF-10 | O app deve aceitar a senha também pela importação do ADB (nome `activation_code.txt`), para a instalação pelo notebook ativar sem digitar. | Should | Após `content write` da senha e recarga, a TV fica ativada. | 🟡 |
| RF-11 | A ferramenta do notebook deve registrar cada senha gerada num arquivo de controle (código do aparelho, cliente, data). | Should | Cada geração acrescenta uma linha ao arquivo. | 🟡 |
| RF-12 | O diagnóstico deve mostrar "Ativada em <data>" ou "Não ativada" no lugar de "disponível em versão futura". | Should | O diagnóstico mostra o estado atual da ativação. | 🟡 |

## 6. Requisitos Não Funcionais

| Tipo | Requisito | Evidência ou justificativa | Confidência |
|------|-----------|----------------------------|-------------|
| Desempenho | Conferência da ativação em no máximo 200 ms na TCL | `_reversa_sdd/sdd/license-activation.md#7-requisitos-não-funcionais` | 🟡 |
| Segurança | Segredo fora do git, dos pacotes de tema e dos logs; arquivo do segredo com permissão `600` | RN-02 | 🟢 |
| Segurança | Senha de 12 números (10^12 combinações) com espera após 5 erros: tentativa e erro pelo controle fica inviável | RN-05 | 🟡 |
| Usabilidade | Código e senha só com números, em grupos de 4, para ditar por telefone e digitar no controle | Esclarecimento de 2026-10-30 | 🟢 |
| Offline | 100% da ativação sem internet | RN-03 | 🟢 |
| Compatibilidade | Funcionar no Android TV 9 (nível de API 28) ou superior sem biblioteca extra | Adendo 001 | 🟡 |
| Observabilidade | Ativação, senha recusada e espera registradas no log `TemaTV`, sem gravar a senha digitada | Suporte do revendedor | 🟡 |

## 7. Critérios de Aceitação

```gherkin
Cenário: TV sem ativação
  Dado o app instalado sem ativação
  Quando a tela inicial abre
  Então aparece a tela de ativação com o código do aparelho e o campo da senha

Cenário: Ativar digitando a senha
  Dado o código do aparelho 1234-5678-9012 na tela
  Quando o revendedor gera a senha desse código no notebook
  E a pessoa digita a senha na TV e escolhe "Ativar"
  Então a grade temática aparece
  E o diagnóstico mostra "Ativada em <data>"

Cenário: Senha errada
  Dado a tela de ativação
  Quando a pessoa digita uma senha que não corresponde ao código
  Então aparece "Senha incorreta. Confira os números com o seu revendedor." e a TV continua sem ativação

Cenário: Espera após 5 erros
  Dado 5 senhas erradas seguidas
  Quando a pessoa tenta de novo
  Então a tela pede para aguardar 30 s antes de aceitar nova senha

Cenário: Senha de outra TV
  Dado uma senha gerada para outro código de aparelho
  Quando ela é digitada nesta TV
  Então é recusada como senha incorreta

Cenário: Ativação perpétua e estável
  Dado a TV ativada
  Quando a TV é reiniciada e o app é reinstalado com a mesma assinatura
  Então a TV continua ativada

Cenário: Home sem ativação
  Dado a TV sem ativação e o usuário no YouTube
  Quando ele aperta Home
  Então aparece a tela de ativação, não a grade temática nem a tela do Google

Cenário: Ativação pelo ADB
  Dado o código do aparelho lido pelo ADB
  Quando a instalação envia a senha correta pela importação e recarrega
  Então a TV fica ativada sem digitar nada

Cenário: Segredo já existe
  Dado o segredo de ativação já gerado no notebook
  Quando o revendedor tenta gerar outro
  Então a ferramenta recusa e orienta a usar o existente
```

## 8. Prioridade MoSCoW

| Item | MoSCoW | Justificativa |
|------|--------|---------------|
| RF-01 a RF-09 | Must | Sem eles a ativação não funciona de ponta a ponta |
| RF-10, RF-11, RF-12 | Should | Instalação automática (feature 006), controle do revendedor e suporte |

## 9. Esclarecimentos

### Sessão 2026-10-30

- **Q:** Qual a validade da licença?
  **R:** Sempre perpétua. Aplicado em RN-03 e RF-07.
- **Q:** Quando uma licença com validade vencer, o que acontece?
  **R:** O usuário escolheu carência de 7 dias, mas com a licença perpétua não há vencimento; a regra não se aplica e não foi implementada. Fica registrada caso a validade volte a existir.
- **Q:** A tela de ativação mostra o contato do revendedor?
  **R:** O usuário quer ativar na hora com uma senha que a pessoa digita na TV. Confirmado depois: senha de 12 números gerada no notebook a partir do código do aparelho. Aplicado em RN-02, RF-01, RF-04 a RF-08 e na troca do arquivo de licença assinado pela senha.
- **Q:** Onde a senha é gerada?
  **R:** No notebook: um comando agora e, na feature 005, um campo na página da ferramenta. Aplicado em RF-03, RF-04 e RF-11.

## 10. Lacunas

- Nenhuma lacuna aberta.

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-requirements` | reversa |
| 2026-10-30 | Sessão de esclarecimentos: ativação perpétua por senha de 12 números digitada na TV, gerada no notebook; sem arquivo de licença assinado | reversa-clarify |

## Emendas

### E001, 2026-10-30

O que muda: o campo da senha no painel de ativação coloca os traços sozinho enquanto a pessoa digita só números no teclado numérico, mostrando a senha no formato `0000-0000-0000`, igual ao jeito que ela é ditada; continua aceitando no máximo 12 números.
Motivo: o teclado numérico da TV não tem traço e o usuário pediu que o campo ficasse no mesmo formato da senha.
Arquivos previstos: `launcher/android/app/src/main/java/br/tematv/launcher/activation/ActivationInputFormat.kt` (novo), `home/HomeActivity.kt`, `res/layout/activity_home.xml` e o teste do formato.
