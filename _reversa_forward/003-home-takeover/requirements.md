# Requirements: Troca automática da tela inicial (home-takeover)

> Identificador: `003-home-takeover`
> Data: `2026-10-30`
> Pasta da extração reversa: `_reversa_sdd/` (projeto novo: fontes são o PRD, as specs SDD e os adendos vigentes 001 e 002)
> Branch: `feature/003-home-takeover` (feature 3 de 6, criada a partir de `feature/002-launcher-home`)
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA / DÚVIDA

## 1. Resumo executivo

Esta feature faz a tela inicial temática assumir o lugar do launcher do Google sozinha: depois que a TV liga (do zero ou saindo do modo de espera) e sempre que alguém aperta Home. Sem desativar o launcher do Google, um serviço de acessibilidade percebe quando ele aparece e abre a tela temática por cima. A tela "Tema" ganha a opção de desligar a troca e voltar ao launcher original, e de religá-la depois, sem ADB. Resolve o maior problema relatado no PRD: soluções que "caem" e exigem reativar o tema a cada vez que a TV liga.

## 2. Contexto a partir do legado

Não há sistema legado. Fontes: artefatos do `/reversa-new` (🟡) e adendos vigentes, com fatos verificados na TV de referência (🟢).

| Fonte | Trecho relevante | Confidência |
|-------|------------------|-------------|
| `_reversa_sdd/prd.md#1-problema` | Soluções existentes exigem reativar o tema sempre que a TV liga | 🟡 |
| `_reversa_sdd/prd.md#3-métricas-de-sucesso` | Tema estável por 30 dias de liga e desliga, 0 chamados | 🟡 |
| `_reversa_sdd/sdd/home-takeover.md#6-requisitos-funcionais` | RF-01 a RF-10 (serviço de acessibilidade, boot, preferência, desligar e religar, aviso, janela de 5 s, licença, diagnóstico) | 🟡 |
| `_reversa_sdd/sdd/home-takeover.md#11-edge-cases-e-tratamento-de-erros` | EC-01 a EC-06, incluindo a trava contra loop que pausa 30 s e retoma sozinha | 🟡 |
| `_reversa_sdd/addenda/002-launcher-home.md#impacto-por-artefato-da-extração` | A tela a abrir é `br.tematv.launcher/.home.HomeActivity` (`singleTask`), que já consome o Voltar; o diagnóstico já mostra "disponível em versão futura" para esta feature | 🟢 |
| `_reversa_sdd/addenda/002-launcher-home.md#impacto-por-artefato-da-extração` | OQ-02 (categoria HOME) ficou para esta feature | 🟢 |
| Consulta à TV de referência em 2026-10-30 | A tela inicial padrão é `com.google.android.apps.tv.launcherx/.home.HomeActivity`, com prioridade 2 na resolução de HOME (apps comuns só têm prioridade 0); nenhum serviço de acessibilidade está ligado | 🟢 |

## 3. Personas e cenários de uso

| Persona | Objetivo | Cenário-chave |
|---------|----------|---------------|
| Público da igreja ou da família | Ver a tela da igreja sempre que liga a TV ou aperta Home | Liga a TV pelo controle e, em segundos, a tela temática aparece sem ninguém mexer |
| Revendedor e instalador (`_reversa_sdd/personas.md#persona-1-revendedor-e-instalador`) | Entregar uma TV que não "cai" | Liga o serviço na instalação, reinicia a TV e confere que o tema voltou sozinho |
| Responsável da igreja | Voltar ao launcher do Google quando precisar | Abre "Tema", escolhe "Voltar ao launcher original do Google" e, depois, religa pela lista de apps |

## 4. Regras de negócio novas ou alteradas

1. **RN-01:** O launcher do Google nunca é desativado, desinstalado nem substituído; a troca só abre a tela temática por cima dele. 🟡
   - Origem: `_reversa_sdd/sdd/home-takeover.md#4-non-goals-fora-do-escopo` (NG-01)
   - Tipo: nova
2. **RN-02:** A troca acontece só com `takeover_enabled` ligado; desligar é sempre possível pela TV, sem ADB. 🟡
   - Origem: `_reversa_sdd/prd.md#4-escopo-in`
   - Tipo: nova
3. **RN-03:** Se o sistema desligar o serviço de acessibilidade, a TV avisa como religar; o produto não impede o usuário de desligá-lo. 🟡
   - Origem: `_reversa_sdd/sdd/home-takeover.md#4-non-goals-fora-do-escopo` (NG-03)
   - Tipo: nova
4. **RN-04:** A trava contra loop nunca desliga a troca: ela pausa por 30 s e retoma sozinha. 🟡
   - Origem: `_reversa_sdd/sdd/home-takeover.md#11-edge-cases-e-tratamento-de-erros` (EC-02)
   - Tipo: nova
5. **RN-05:** Até a feature 4 (licença), a licença é tratada como válida; o ponto de verificação já fica no código para a feature 4 ligar. 🟡
   - Origem: sequência de branches combinada com o usuário
   - Tipo: nova

## 5. Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de aceite | Confidência |
|----|-----------|------------|--------------------|-------------|
| RF-01 | O sistema deve oferecer um serviço de acessibilidade, com nome e descrição em português, que observa só a troca de janela em primeiro plano dos launchers do sistema. | Must | O serviço aparece em Configurações > Acessibilidade como "Launcher Temático" e não lê o conteúdo das telas. | 🟡 |
| RF-02 | O sistema deve abrir a tela temática quando o launcher do sistema (`com.google.android.apps.tv.launcherx` na TCL) vier para o primeiro plano e a troca estiver ligada. | Must | Com a troca ligada, apertar Home em qualquer app mostra a tela temática em até 1,5 s. | 🟢 |
| RF-03 | O sistema deve abrir a tela temática ao final do boot quando a troca estiver ligada. | Must | Após reiniciar a TV, a tela temática aparece em até 10 s depois que o launcher do Google aparece. | 🟡 |
| RF-04 | O sistema deve abrir a tela temática quando a TV sai do modo de espera e o launcher do sistema aparece. | Must | Desligar e ligar pelo controle mostra a tela temática em até 1,5 s depois que o launcher do Google aparece. | 🟡 |
| RF-05 | O sistema deve guardar a preferência `takeover_enabled`, ligada por padrão, que sobrevive a reinicializações. | Must | Após reiniciar a TV, a preferência mantém o último valor escolhido. | 🟡 |
| RF-06 | O usuário deve poder desligar a troca na tela "Tema" com a opção "Voltar ao launcher original do Google"; o sistema grava `takeover_enabled = false` e abre o launcher do Google. | Must | Após a opção, apertar Home mostra o launcher do Google e a tela temática não volta sozinha. | 🟡 |
| RF-07 | O usuário deve poder religar a troca na tela "Tema" com a opção "Usar o tema como tela inicial", abrindo o launcher temático pela lista de apps do Google. | Must | Após a opção, apertar Home mostra a tela temática. | 🟡 |
| RF-08 | O sistema deve ignorar o launcher do sistema por 5 s depois que o usuário escolher voltar ao launcher original. | Must | Escolhendo voltar, a tela do Google fica visível sem piscar a tela temática. | 🟡 |
| RF-09 | O sistema deve limitar as aberturas automáticas a 3 em 10 s; acima disso, pausa a troca por 30 s, registra o evento e retoma sozinho, sem mudar `takeover_enabled`. | Must | Em um teste que força alternância, a troca pausa, registra e volta a funcionar após 30 s. | 🟡 |
| RF-10 | O sistema deve detectar, ao abrir a tela temática, se o serviço de acessibilidade está desligado com a troca ligada, e então mostrar uma faixa discreta no rodapé da tela inicial ("O tema não está abrindo sozinho. Veja em Tema.") e, na tela "Tema", o passo a passo para religar e um botão que abre as Configurações de acessibilidade. | Must | Com o serviço desligado pelo ADB, a faixa aparece no rodapé; em "Tema", o passo a passo e o botão aparecem e o botão abre as Configurações de acessibilidade. | 🟢 |
| RF-11 | O sistema deve mostrar na tela "Tema" o estado da troca (ligada ou desligada), o estado do serviço de acessibilidade, a data e hora da última troca bem-sucedida e da última vez em que o serviço foi encontrado desligado, e o último evento de pausa contra loop. | Should | O diagnóstico mostra os valores atuais no lugar de "disponível em versão futura" para troca e acessibilidade. | 🟡 |
| RF-12 | O sistema deve consultar o ponto de verificação de licença antes de cada troca; nesta feature ele sempre responde "válida" (RN-05). | Must | O código da troca chama a verificação de licença; um teste com licença inválida simulada não abre a tela temática. | 🟡 |
| RF-14 | O sistema deve cobrir a tela do Google, no mesmo instante em que decide abrir a tela temática, com uma capa do tema ativo (fundo e logotipo) que some quando a tela temática real aparece, ou em no máximo 8 s. | Must | Na TCL, ao apertar Home num app, a tela do Google não fica visível: a capa aparece em até 1 s e é trocada pela tela temática. | 🟢 |
| RF-13 | O usuário deve segurar OK por 3 s na opção "Voltar ao launcher original do Google" para desligar a troca; enquanto segura, a tela mostra o progresso, e soltar antes cancela. | Must | Um toque curto em OK não desliga a troca e mostra "Segure OK por 3 segundos"; segurar 3 s desliga. | 🟢 |

## 6. Requisitos Não Funcionais

| Tipo | Requisito | Evidência ou justificativa | Confidência |
|------|-----------|----------------------------|-------------|
| Desempenho | Home até a tela temática em no máximo 1,5 s na TCL, com a capa do tema cobrindo a tela do Google desde o primeiro instante (sem a capa, o Android segura a troca por cerca de 4 s; com a capa, medido abaixo de 1 s) | `_reversa_sdd/sdd/home-takeover.md#7-requisitos-não-funcionais` (RNF-01) | 🟡 |
| Desempenho | Boot até a tela temática em no máximo 10 s depois do launcher do Google | RNF da spec | 🟡 |
| Consumo | Serviço de acessibilidade com PSS de no máximo 20 MB, rodando o tempo todo | RNF-02; TV de 2 GB | 🟡 |
| Confiabilidade | 30 ciclos seguidos com o tema aparecendo sozinho, como teste acelerado da meta de 30 dias: reinicializações pelo ADB e saídas do modo de espera (comando de energia pelo ADB, ou controle remoto operado pelo usuário quando necessário); os 30 dias reais ficam para o cliente piloto | `_reversa_sdd/prd.md#3-métricas-de-sucesso`; esclarecimento de 2026-10-30 | 🟢 |
| Privacidade | O serviço só lê o nome do pacote em primeiro plano; não lê texto, conteúdo de tela nem teclas | RNF-04 da spec; NG-04 | 🟡 |
| Observabilidade | Cada troca, pausa contra loop e serviço encontrado desligado registrados no log `TemaTV` | Suporte do revendedor | 🟡 |

## 7. Critérios de Aceitação

```gherkin
Cenário: Home em qualquer app
  Dado a troca ligada e o serviço de acessibilidade ativo
  Quando o usuário está no YouTube e aperta Home
  Então a tela temática aparece em até 1,5 s

Cenário: TV reiniciada
  Dado a troca ligada e o serviço ativo
  Quando a TV é reiniciada
  Então a tela temática aparece sozinha em até 10 s depois do launcher do Google

Cenário: TV sai do modo de espera
  Dado a troca ligada e a TV em modo de espera
  Quando o usuário liga a TV pelo controle
  Então a tela temática aparece em até 1,5 s depois do launcher do Google

Cenário: Voltar ao launcher original
  Dado a troca ligada
  Quando o responsável escolhe "Voltar ao launcher original do Google" na tela "Tema"
  Então o launcher do Google aparece sem piscar a tela temática
  E apertar Home depois continua mostrando o launcher do Google

Cenário: Religar a troca
  Dado a troca desligada
  Quando o responsável abre o Launcher Temático pela lista de apps e escolhe "Usar o tema como tela inicial"
  Então apertar Home volta a mostrar a tela temática

Cenário: Preferência sobrevive à reinicialização
  Dado a troca desligada
  Quando a TV é reiniciada
  Então a tela temática não aparece sozinha

Cenário: Serviço desligado pelo sistema
  Dado a troca ligada e o serviço de acessibilidade desligado
  Quando alguém abre o Launcher Temático
  Então aparece o aviso de como religar o serviço, com botão para as Configurações de acessibilidade

Cenário: Trava contra loop
  Dado a troca ligada
  Quando o launcher do Google aparece mais de 3 vezes em 10 s
  Então a troca pausa por 30 s, registra o evento e volta a funcionar sozinha
  E takeover_enabled continua ligado

Cenário: Licença inválida (ponto de verificação)
  Dado a verificação de licença respondendo "inválida" num teste
  Quando o launcher do Google aparece
  Então a tela temática não é aberta pela troca

Cenário: Toque curto não desliga
  Dado a troca ligada e o foco em "Voltar ao launcher original do Google"
  Quando o usuário aperta OK rapidamente
  Então a troca continua ligada e a tela mostra "Segure OK por 3 segundos"

Cenário: Segurar OK desliga
  Dado a troca ligada e o foco em "Voltar ao launcher original do Google"
  Quando o usuário segura OK por 3 s
  Então a troca é desligada e o launcher do Google aparece

Cenário: Faixa discreta na tela inicial
  Dado a troca ligada e o serviço de acessibilidade desligado
  Quando a tela inicial temática é exibida
  Então aparece no rodapé a faixa "O tema não está abrindo sozinho. Veja em Tema."

Cenário: Capa cobre a espera do Android
  Dado a troca ligada e o usuário no YouTube
  Quando ele aperta Home
  Então a capa do tema aparece em até 1 s por cima da tela do Google
  E some quando a tela temática real aparece

Cenário: Diagnóstico da troca
  Dado a troca ligada e uma troca bem-sucedida
  Quando o usuário abre "Tema"
  Então vê "ligada", o estado do serviço e a data e hora da última troca
```

## 8. Prioridade MoSCoW

| Item | MoSCoW | Justificativa |
|------|--------|---------------|
| RF-01 a RF-10, RF-12 | Must | Sem eles o produto não cumpre a promessa central de não "cair" |
| RF-13 | Must | Sem a proteção, uma criança desliga o tema sem querer |
| RF-14 | Must | Sem a capa, a tela do Google fica visível cerca de 4 s após cada Home na TCL |
| RF-11 | Should | Diagnóstico ajuda o suporte |
| RNF de confiabilidade (30 ciclos) | Must | É o teste acelerado da métrica principal do PRD |

## 9. Esclarecimentos

### Sessão 2026-10-30

- **Q:** A opção "Voltar ao launcher original do Google" deve ter proteção contra crianças?
  **R:** Segurar OK por 3 s, com progresso na tela; soltar antes cancela. Aplicado em RF-13 (agora Must) e nos cenários "Toque curto não desliga" e "Segurar OK desliga".
- **Q:** Quando o serviço de acessibilidade estiver desligado, onde aparece o aviso?
  **R:** Faixa discreta no rodapé da tela inicial e o passo a passo completo, com botão para as Configurações de acessibilidade, na tela "Tema". Aplicado em RF-10 e no cenário "Faixa discreta na tela inicial".
- **Q:** O teste de aceite pode ser o teste acelerado de 30 ciclos?
  **R:** Sim: 30 ciclos agora (reinicializações e saídas do modo de espera), e os 30 dias reais no cliente piloto. Aplicado no RNF de confiabilidade.

## 10. Lacunas

- Nenhuma lacuna aberta. As três dúvidas da versão inicial foram resolvidas na sessão de 2026-10-30 (seção 9).

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-requirements` | reversa |
| 2026-10-30 | Sessão de esclarecimentos: proteção por OK de 3 s, aviso discreto e teste acelerado de 30 ciclos | reversa-clarify |
| 2026-10-30 | Emenda durante o teste na TV: na TCL a tela temática só abre cerca de 4 s após o Home (proteção de troca de app do Android; a TCL não entrega o Home ao serviço). Decisão do usuário: cobrir na hora com uma capa do tema (RF-14); meta do Home ajustada | reversa-coding |
