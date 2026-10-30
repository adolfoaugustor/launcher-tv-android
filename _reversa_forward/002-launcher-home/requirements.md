# Requirements: Tela inicial do launcher (launcher-home)

> Identificador: `002-launcher-home`
> Data: `2026-10-30`
> Pasta da extração reversa: `_reversa_sdd/` (projeto novo: fontes são o PRD, as specs SDD e o adendo vigente da feature 001)
> Branch: `feature/002-launcher-home` (feature 2 de 6, criada a partir de `feature/001-theme-engine`)
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA / DÚVIDA

## 1. Resumo executivo

Esta feature entrega a tela inicial temática: a tela que a TV mostra com o logotipo, as cores, a mensagem e o versículo do dia do tema ativo, e com os apps instalados desenhados dentro da moldura do tema (no tema infantil cristão, uma Bíblia). O usuário navega com o direcional, abre qualquer app com OK, e encontra itens fixos para as Configurações da TV e para as opções do tema com diagnóstico. É a primeira parte visível do produto; nesta feature a tela ainda é aberta pela lista de apps do Google TV, e passa a assumir o botão Home na feature 3.

## 2. Contexto a partir do legado

Não há sistema legado. As fontes são os artefatos do `/reversa-new` (🟡 PLANEJADO) e o adendo vigente da feature 001, cujas afirmações foram verificadas na TV de referência (🟢).

| Fonte | Trecho relevante | Confidência |
|-------|------------------|-------------|
| `_reversa_sdd/prd.md#4-escopo-in` | App launcher que lista os apps, abre o escolhido e mostra identidade da igreja e versículo do dia | 🟡 |
| `_reversa_sdd/prd.md#9-critérios-de-aceite-alto-nível` | Cada app dentro da moldura de Bíblia e versículo do dia sem internet | 🟡 |
| `_reversa_sdd/sdd/launcher-home.md#6-requisitos-funcionais` | RF-01 a RF-14 da tela inicial | 🟡 |
| `_reversa_sdd/sdd/launcher-home.md#7-requisitos-não-funcionais` | Abertura ≤ 2 s, troca de foco ≤ 100 ms, PSS ≤ 100 MB, versículo ≥ 28 px | 🟡 |
| `_reversa_sdd/sdd/launcher-home.md#11-edge-cases-e-tratamento-de-erros` | EC-01 a EC-07 | 🟡 |
| `_reversa_sdd/addenda/001-theme-engine.md#impacto-por-artefato-da-extração` | A tela consome `ThemeRepository.activeTheme` (nulo até a primeira leitura), `ThemeImageLoader` e `ThemeRepository.lastError` | 🟢 |
| `_reversa_sdd/addenda/001-theme-engine.md#impacto-por-artefato-da-extração` | Moldura em retrato 260×340 px de referência, 5 apps por linha; mensagem até 120 caracteres em até 2 linhas | 🟢 |
| `_reversa_forward/001-theme-engine/interfaces/importacao-adb.md` | Temas entram na TV por `content write` pelo ADB | 🟢 |
| Consulta à TV de referência em 2026-10-30 | A TCL tem 21 apps com lançador de TV; 11 são da TCL, entre eles a TV ao vivo e as entradas HDMI (`com.tcl.tv`), a central de mídia USB (`com.tcl.ui_mediaCenter`), a galeria (`com.tcl.gallery`), o espelhamento de tela (`com.tcl.MultiScreenInteraction_TV`) e 7 utilitários técnicos | 🟢 |

## 3. Personas e cenários de uso

| Persona | Objetivo | Cenário-chave |
|---------|----------|---------------|
| Público da igreja ou da família (crianças, professores, membros) | Abrir um app pela tela da igreja | Liga a TV, vê a tela com a identidade da igreja e o versículo do dia, escolhe o YouTube dentro de uma Bíblia e aperta OK |
| Revendedor e instalador (`_reversa_sdd/personas.md#persona-1-revendedor-e-instalador`) | Conferir que a TV ficou certa | Abre "Tema" e confere no diagnóstico o tema ativo, a versão e o último erro |

## 4. Regras de negócio novas ou alteradas

1. **RN-01:** Todo o visual da tela vem do tema ativo exposto pelo motor de temas da feature 001; a tela não tem cores, imagens nem textos de cliente fixos no código. 🟢
   - Origem: `_reversa_sdd/addenda/001-theme-engine.md`
   - Tipo: nova
2. **RN-02:** Os apps são exibidos sem filtro por perfil: o produto muda o visual, não restringe o uso. 🟡
   - Origem: `_reversa_sdd/prd.md#5-não-objetivos-out` (modo criança fora do escopo)
   - Tipo: nova
3. **RN-03:** O versículo do dia é o de índice `(dias desde 1970-01-01 no fuso da TV) mod (quantidade de versículos do tema)` e sempre aparece com referência, sigla e atribuição da tradução. 🟡
   - Origem: `_reversa_sdd/sdd/launcher-home.md#6-requisitos-funcionais` (RF-07, RF-08)
   - Tipo: nova
4. **RN-04:** O botão Voltar nunca tira o usuário da tela inicial. 🟡
   - Origem: `_reversa_sdd/sdd/launcher-home.md#6-requisitos-funcionais` (RF-14)
   - Tipo: nova
5. **RN-05:** Nesta feature, as opções de troca da tela inicial (feature 3) e a licença (feature 4) ainda não existem: a tela "Tema" e o diagnóstico mostram esses itens como "disponível em versão futura", sem simular estado. 🟡
   - Origem: sequência de branches combinada com o usuário (6 features em cadeia)
   - Tipo: nova
6. **RN-06:** Utilitários técnicos de fabricante ficam fora da grade por uma lista fixa de pacotes; a TV ao vivo e as entradas HDMI sempre aparecem. Lista inicial (TCL): `com.tcl.guard`, `com.tcl.suspension`, `com.tcl.partnercustomizer`, `com.tcl.notereminder`, `com.tcl.dashboard`, `com.tcl.waterfall.overseas` e `com.tcl.t_solo`. Os apps escondidos continuam acessíveis pelas Configurações da TV. 🟢
   - Origem: sessão de esclarecimentos de 2026-10-30 (seção 9)
   - Tipo: nova

## 5. Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de aceite | Confidência |
|----|-----------|------------|--------------------|-------------|
| RF-01 | O sistema deve listar os apps instalados que declaram atividade de lançador para TV, exceto o próprio launcher e os utilitários técnicos de RN-06. | Must | Na TCL, YouTube, Netflix, XCIPTV, Disney+ e a TV ao vivo (`com.tcl.tv`) aparecem na grade; o próprio launcher e os 7 utilitários de RN-06 não aparecem (14 apps na grade). | 🟢 |
| RF-02 | O sistema deve desenhar cada app dentro da moldura do tema, com o banner de TV do app (ou o ícone, se não houver banner) dentro de `icon_slot`, ocupando a largura do painel, centralizado e sem distorção, e o nome do app dentro de `label_slot`, na cor de texto do tema. | Must | No tema bíblico, cada app aparece dentro da Bíblia com o banner no painel e o nome na faixa; no tema hambúrguer, dentro do hambúrguer; um app sem banner mostra o ícone centralizado. | 🟢 |
| RF-03 | O sistema deve ordenar os apps pelo nome exibido, em ordem alfabética, ignorando acentos e maiúsculas. | Must | "Disney+" vem antes de "Netflix", que vem antes de "YouTube". | 🟡 |
| RF-04 | O usuário deve poder mover o foco com o direcional e abrir o app em foco com OK. | Must | Navegação completa só com o controle; OK abre o app em foco em tela cheia. | 🟡 |
| RF-05 | O sistema deve destacar o item em foco com borda na cor de destaque do tema e ampliação de 10%. | Must | O item em foco tem borda na cor `accent` e fica 10% maior; os demais ficam no tamanho normal. | 🟡 |
| RF-06 | O sistema deve exibir o fundo (imagem ou cor), o logotipo e a mensagem do tema ativo, com a mensagem em no máximo 2 linhas. | Must | Trocar o tema pela importação do ADB e recarregar muda fundo, logotipo e mensagem juntos, sem reabrir o app. | 🟢 |
| RF-07 | O sistema deve exibir o versículo do dia conforme RN-03, com referência e sigla da versão. | Must | Com o relógio da TV avançado um dia, o versículo muda; reabrir a tela no mesmo dia mostra o mesmo versículo. | 🟡 |
| RF-08 | O sistema deve exibir a atribuição da tradução abaixo do versículo. | Must | A atribuição aparece sempre que um versículo aparece. | 🟡 |
| RF-09 | O sistema deve atualizar a grade quando um app é instalado, removido ou atualizado, sem reiniciar a TV. | Must | Após instalar um app pelo ADB com a tela aberta, ele aparece na grade em até 5 s. | 🟡 |
| RF-10 | O sistema deve oferecer, no canto superior direito do cabeçalho, um botão fixo "Configurações" que abre as Configurações da TV, alcançado com o direcional para cima a partir da grade. | Must | Do primeiro app da grade, direcional para cima leva aos botões; OK em "Configurações" abre as Configurações do Android TV. | 🟢 |
| RF-11 | O sistema deve oferecer, ao lado de "Configurações" no cabeçalho, um botão fixo "Tema" que abre a tela de opções do launcher; nesta feature ela contém o diagnóstico e o aviso de RN-05. | Must | OK em "Tema" abre a tela com o diagnóstico; Voltar retorna à grade. | 🟢 |
| RF-12 | O sistema deve exibir no diagnóstico: versão do app, tema ativo (nome e origem: pacote ou padrão), último erro de tema (código, campo, pacote, data e hora) e, para acessibilidade e licença, o texto "disponível em versão futura". | Should | Após um pacote recusado, o diagnóstico mostra o código e o pacote do erro. | 🟢 |
| RF-13 | O sistema deve devolver o foco ao último app aberto quando a tela inicial volta a ser exibida. | Could | Após abrir e fechar o YouTube, o foco está no YouTube. | 🟡 |
| RF-14 | O sistema deve ignorar o botão Voltar na grade; nas telas "Tema" e diagnóstico, Voltar retorna à grade. | Must | Apertar Voltar 10 vezes na grade mantém a tela temática visível. | 🟡 |
| RF-15 | O launcher deve aparecer na lista de apps do Google TV com o nome "Launcher Temático" e um banner, para ser aberto antes da feature 3. | Must | O launcher aparece na lista de apps do Google TV e abre a tela inicial temática. | 🟡 |
| RF-16 | O sistema deve mostrar, enquanto o tema ainda não foi lido, a cor de fundo do tema padrão e molduras vazias, sem tela preta. | Should | Na abertura a frio, a primeira imagem visível nunca é uma tela preta ou branca. | 🟡 |

## 6. Requisitos Não Funcionais

| Tipo | Requisito | Evidência ou justificativa | Confidência |
|------|-----------|----------------------------|-------------|
| Desempenho | Abertura a frio da tela inicial em no máximo 2 s na TCL de referência | `_reversa_sdd/sdd/launcher-home.md#7-requisitos-não-funcionais` (RNF-01) | 🟡 |
| Desempenho | Troca de foco em no máximo 100 ms, sem travadas perceptíveis na rolagem | Mesmo RNF-01 | 🟡 |
| Desempenho | Aplicar um pacote de tema de até 15 MB em no máximo 3 s, medido nesta feature porque a 001 não tinha tela | `_reversa_forward/001-theme-engine/actions.md` (rodada 2: não medido) | 🟢 |
| Memória | Uso de memória (PSS) do launcher em primeiro plano em no máximo 100 MB | RNF-02; a TCL tem 2 GB de RAM e cerca de 500 MB livres | 🟡 |
| Legibilidade | Texto do versículo com no mínimo 28 px em tela 1080p; nomes dos apps com no mínimo 20 px | RNF-04 (distância de sofá) | 🟡 |
| Offline | Todas as funções da tela funcionam sem internet | RNF-05 | 🟡 |
| Compatibilidade | Android TV 9 (nível de API 28) ou superior | Adendo 001 (versão mínima adotada) | 🟢 |
| Observabilidade | Falha ao abrir um app registrada no log `TemaTV` com o nome do pacote | Suporte do revendedor | 🟡 |

## 7. Critérios de Aceitação

```gherkin
Cenário: Tela inicial com o tema bíblico
  Dado o tema "biblia-exemplo" ativo na TV
  Quando o usuário abre o Launcher Temático
  Então a tela mostra o fundo, o logotipo e a mensagem do tema
  E mostra o versículo do dia com referência, sigla e atribuição
  E mostra cada app dentro da moldura de Bíblia com o nome na faixa

Cenário: Abrir um app
  Dado a tela inicial aberta com o foco no primeiro app
  Quando o usuário move o foco até o YouTube e aperta OK
  Então o YouTube abre em tela cheia

Cenário: Destaque do item em foco
  Dado a tela inicial aberta
  Quando o foco passa para um app
  Então esse app ganha borda na cor de destaque e fica 10% maior

Cenário: Troca de tema com a tela aberta
  Dado a tela inicial aberta com o tema bíblico
  Quando o instalador importa e ativa o "hamburguer-exemplo" e envia a recarga
  Então a tela passa a mostrar as molduras de hambúrguer e as cores do novo tema sem ser reaberta

Cenário: Versículo muda com o dia
  Dado um tema com 7 versículos
  Quando a data da TV avança um dia
  Então o versículo exibido é o seguinte da lista, voltando ao primeiro depois do último

Cenário: App instalado com a tela aberta
  Dado a tela inicial aberta
  Quando um app de TV é instalado
  Então ele aparece na grade em até 5 s, na posição alfabética

Cenário: App removido com foco nele
  Dado o foco em um app
  Quando esse app é desinstalado
  Então ele sai da grade e o foco vai para o item seguinte

Cenário: App que não abre
  Dado um app cuja abertura falha
  Quando o usuário aperta OK nele
  Então aparece por 4 s o aviso "Não foi possível abrir <nome do app>" e a tela inicial continua

Cenário: Voltar não sai da tela
  Dado a grade de apps aberta
  Quando o usuário aperta Voltar 10 vezes
  Então a tela temática continua visível

Cenário: Diagnóstico mostra o último erro
  Dado um pacote recusado com THEME_ARCHIVE_CORRUPT
  Quando o usuário abre "Tema"
  Então o diagnóstico mostra o código, o pacote e a data do erro, e o tema ativo

Cenário: Sem apps de TV
  Dado nenhum app de TV instalado além do launcher
  Quando a tela abre
  Então a grade fica vazia com a mensagem "Nenhum app instalado" e os botões "Configurações" e "Tema" continuam no cabeçalho

Cenário: Utilitários técnicos fora da grade
  Dado a TV de referência com os utilitários da TCL instalados
  Quando a tela abre
  Então "Guard", "Suspension" e os demais utilitários de RN-06 não aparecem
  E a TV ao vivo aparece

Cenário: App sem banner
  Dado um app de TV sem banner
  Quando a grade é desenhada
  Então o ícone do app aparece centralizado no painel da moldura, sem distorção

Cenário: Nome de app longo
  Dado um app com nome maior que a faixa da moldura
  Quando a grade é desenhada
  Então o nome aparece em uma linha, cortado com reticências

Cenário: Abrir pela lista de apps do Google TV
  Dado o launcher instalado
  Quando o usuário abre "Launcher Temático" na lista de apps do Google TV
  Então a tela inicial temática aparece

Cenário: Configurações da TV
  Dado o foco no primeiro app da grade
  Quando o usuário aperta o direcional para cima e escolhe "Configurações"
  Então as Configurações do Android TV abrem

Cenário: Primeira imagem sem tela preta
  Dado o launcher fechado
  Quando ele é aberto a frio
  Então a primeira imagem é a cor de fundo do tema padrão com molduras vazias, até o tema ativo carregar

Cenário: Foco volta ao último app
  Dado o usuário abriu o YouTube pela grade
  Quando volta para a tela inicial
  Então o foco está no YouTube
```

## 8. Prioridade MoSCoW

| Item | MoSCoW | Justificativa |
|------|--------|---------------|
| RF-01 a RF-11, RF-14, RF-15 | Must | Sem eles não há tela inicial utilizável |
| RF-12, RF-16 | Should | Diagnóstico ajuda o suporte; tela sem preto melhora a primeira impressão |
| RF-13 | Could | Conforto, não bloqueia o uso |
| RNF de desempenho e memória | Must | TV de 2 GB; métrica do PRD depende de a tela ser leve |

## 9. Esclarecimentos

### Sessão 2026-10-30

- **Q:** Dentro da moldura, qual imagem do app deve aparecer?
  **R:** O banner de TV (16:9), ocupando a largura do painel; sem banner, o ícone. Aplicado em RF-02 e no cenário "App sem banner".
- **Q:** Os utilitários da TCL devem aparecer na grade?
  **R:** Esconder por uma lista fixa de utilitários técnicos; a TV ao vivo e as entradas HDMI sempre aparecem. Aplicado em RN-06, RF-01 e no cenário "Utilitários técnicos fora da grade". A lista inicial foi montada com os pacotes da TV de referência; central de mídia USB, galeria e espelhamento de tela continuam visíveis por serem úteis ao usuário.
- **Q:** Onde ficam os itens fixos "Configurações" e "Tema"?
  **R:** No canto superior direito do cabeçalho, sempre visíveis, alcançados com o direcional para cima. Aplicado em RF-10, RF-11 e nos cenários "Configurações da TV" e "Sem apps de TV".

## 10. Lacunas

- Nenhuma lacuna aberta. As três dúvidas da versão inicial foram resolvidas na sessão de 2026-10-30 (seção 9).

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-requirements` | reversa |
| 2026-10-30 | Sessão de esclarecimentos: banner, utilitários da TCL e posição dos itens fixos | reversa-clarify |

## Emendas

### E001, 2026-10-30

O que muda: o versículo deixa de ser o "versículo do dia" (o mesmo o dia inteiro) e passa a ser sorteado cada vez que a tela inicial aparece (ligar a TV, apertar Home, voltar de um app), sem repetir o versículo anterior; RN-03 e RF-07 passam a valer assim, mantendo referência, sigla e atribuição.
Motivo: o usuário viu na TV que o versículo não trocava e pediu que fosse aleatório, escolhendo "sorteado ao abrir".
Arquivos previstos: `launcher/android/app/src/main/java/br/tematv/launcher/home/VerseOfTheDay.kt` (substituído por `VersePicker.kt`), `HomeActivity.kt` e o teste correspondente.
