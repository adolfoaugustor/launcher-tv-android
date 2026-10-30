# Requirements: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`
> Pasta da extração reversa: `_reversa_sdd/` (projeto novo: fontes são o PRD, as specs SDD e os adendos vigentes 001, 002, 003 e 004)
> Branch: `feature/005-theme-builder` (feature 5 de 6, criada a partir de `feature/004-license-activation`)
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA / DÚVIDA

## 1. Resumo executivo

Esta feature entrega a ferramenta do notebook do revendedor: um comando abre uma página local no navegador onde ele cadastra cada cliente, de igreja ou de loja (nome, logotipo, cores, fundo, mensagem, moldura dos ícones e versículos ou mensagens da loja), confere uma prévia da tela da TV e gera o pacote `.tematv`, sem editar JSON nem compilar o app. Nos temas de igreja, os versículos partem da lista pronta de 20 versículos da NVI que já está no launcher, no lugar da Bíblia API. A mesma página gera a senha de ativação da TV a partir do código do aparelho, substituindo o comando da feature 004 no dia a dia.

## 2. Contexto a partir do legado

Não há sistema legado. Fontes: artefatos do `/reversa-new` (🟡), adendos vigentes e decisões do usuário (🟢).

| Fonte | Trecho relevante | Confidência |
|-------|------------------|-------------|
| `_reversa_sdd/sdd/theme-builder.md#61-requisitos-principais` | Página local, formulário com prévia, geração e validação do `.tematv`, projetos reabríveis | 🟡 |
| `_reversa_sdd/sdd/theme-builder.md#4-non-goals-fora-do-escopo` | Sem editor de arrastar, sem Windows nativo, sem hospedagem nem contas | 🟡 |
| `_reversa_sdd/addenda/001-theme-engine.md#atualização-2026-10-30` | Bíblia API indisponível: a ferramenta parte da lista pronta de 20 versículos NVI, com a atribuição da Biblica | 🟢 |
| `_reversa_sdd/addenda/001-theme-engine.md#impacto-por-artefato-da-extração` | O formato a gerar está em `_reversa_forward/001-theme-engine/interfaces/pacote-tematv.md`; nome do pacote em `^[a-z0-9-]+\.tematv$` | 🟢 |
| `_reversa_forward/001-theme-engine/data-delta.md` §1.1 | Regras de cada campo do `theme.json` v1 (limites de texto, cores `#RRGGBB`, 1 a 366 versículos, atribuição até 120 caracteres) | 🟢 |
| `_reversa_sdd/addenda/004-license-activation.md#impacto-por-artefato-da-extração` | A página deve gerar a senha de ativação com o mesmo núcleo e conferir contra os vetores fixos | 🟢 |
| `_reversa_forward/004-license-activation/interfaces/senha-ativacao.md` | Contrato da senha: segredo em `~/.config/tematv/activation-secret`, registro em `~/tematv/ativacoes.csv` | 🟢 |
| `_reversa_sdd/personas.md#persona-1-revendedor-e-instalador` | Revendedor iniciante em programação Android, vai à igreja com o notebook, linha de produto para vários segmentos | 🟢 |

## 3. Personas e cenários de uso

| Persona | Objetivo | Cenário-chave |
|---------|----------|---------------|
| Revendedor e instalador (`_reversa_sdd/personas.md#persona-1-revendedor-e-instalador`) | Preparar o tema de um cliente novo em até 15 minutos | Antes da visita, cria "Igreja Batista X", envia o logotipo, escolhe cores e moldura, confere a prévia e gera `igreja-batista-x.tematv` |
| Revendedor e instalador | Ajustar o tema de um cliente existente | Reabre o projeto, troca só a mensagem e gera o pacote de novo |
| Revendedor e instalador | Ativar uma TV na hora | O cliente dita o código do aparelho; ele digita na página e lê a senha de volta |

## 4. Regras de negócio novas ou alteradas

1. **RN-01:** Todo pacote gerado passa pelas mesmas regras de validação do launcher antes de ser salvo; pacote que o launcher recusaria nunca é salvo. 🟢
   - Origem: `_reversa_sdd/sdd/theme-builder.md#61-requisitos-principais` (RF-10)
   - Tipo: nova
2. **RN-02:** O texto bíblico vem da lista pronta da NVI embutida na ferramenta; não há busca na Bíblia API. Versículos acrescentados à mão são digitados pelo revendedor. 🟢
   - Origem: `_reversa_sdd/addenda/001-theme-engine.md#atualização-2026-10-30`
   - Tipo: alterada (a spec previa buscar os textos na Bíblia API com a chave do revendedor)
3. **RN-03:** A página só atende o próprio notebook (`127.0.0.1`). 🟡
   - Origem: `_reversa_sdd/sdd/theme-builder.md#12-segurança-e-privacidade`
   - Tipo: nova
4. **RN-04:** O segredo de ativação é lido só do arquivo do revendedor e nunca aparece na página, em logs, em mensagens de erro nem em pacotes de tema. 🟢
   - Origem: `_reversa_forward/004-license-activation/requirements.md` (RN-02)
   - Tipo: nova
5. **RN-05:** O pacote se chama `<theme_id>.tematv`, com `theme_id` em kebab-case de 3 a 40 caracteres, para passar pela importação do ADB. 🟢
   - Origem: `_reversa_sdd/addenda/001-theme-engine.md#impacto-por-artefato-da-extração`
   - Tipo: nova
6. **RN-06:** Cada cliente tem um tipo, escolhido ao criar: **igreja** (moldura de Bíblia, versículos só da NVI com a atribuição da Biblica) ou **loja** (moldura de hambúrguer, mensagens da loja no lugar dos versículos, com a versão `LOJA`). 🟢
   - Origem: esclarecimento de 2026-10-30; `_reversa_sdd/personas.md#persona-1-revendedor-e-instalador` (linha de produto para vários segmentos)
   - Tipo: nova
7. **RN-07:** Nos temas de igreja todos os versículos são da NVI, inclusive os digitados à mão; não se misturam versões nem atribuições de editoras diferentes. 🟢
   - Origem: esclarecimento de 2026-10-30
   - Tipo: nova

## 5. Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de aceite | Confidência |
|----|-----------|------------|--------------------|-------------|
| RF-01 | A ferramenta deve iniciar com um único comando (`tematv`) e abrir a página em `http://localhost:8787`, aceitando conexões só de `127.0.0.1`; com a porta ocupada, usa a próxima livre até 8797 e mostra a URL no terminal. | Must | O comando abre a página; acesso por outro computador da rede é recusado. | 🟡 |
| RF-02 | A tela "Clientes" deve listar os projetos salvos (com o tipo, igreja ou loja) e oferecer "Novo cliente", que pede o tipo antes do formulário (RN-06); sem projetos, mostra só o botão. | Must | Um projeto criado aparece na lista com o tipo após reiniciar a ferramenta. | 🟢 |
| RF-03 | O formulário do projeto deve aceitar: nome do cliente (1 a 60 caracteres), `theme_id` gerado do nome e editável, logotipo PNG de até 5 MB, cores de fundo, texto e destaque, imagem de fundo opcional (PNG, JPEG ou WEBP) e mensagem de até 120 caracteres. | Must | O formulário recusa mensagem acima de 120 caracteres, logotipo que não seja PNG e `theme_id` fora do formato, com a mensagem ao lado do campo. | 🟢 |
| RF-04 | O usuário deve escolher a moldura dos ícones entre as prontas (Bíblia e Hambúrguer, com as áreas do ícone e do nome já definidas; a sugerida segue o tipo do cliente) ou enviar uma moldura PNG própria informando as duas áreas. | Must | As molduras prontas funcionam sem informar áreas; a prévia mostra um app dentro da moldura escolhida. | 🟢 |
| RF-05 | A lista de versículos ou mensagens deve começar, num projeto de igreja, com os 20 versículos NVI do tema padrão e, num projeto de loja, com uma mensagem de exemplo; o usuário deve poder editar, remover, reordenar e acrescentar itens digitados à mão, de 1 a 366 (referência até 40 caracteres, texto até 600). Na igreja, todo item é gravado com a versão `NVI` (RN-07); na loja, com `LOJA`. | Must | Projeto de igreja novo mostra os 20 versículos; o 367º item é recusado; lista vazia impede gerar; um versículo digitado à mão sai no pacote com a versão `NVI`. | 🟢 |
| RF-06 | A atribuição (até 120 caracteres) deve ser, na igreja, fixa com o texto da NVI; na loja, um campo editável que começa como "Mensagens da <nome do cliente>". | Must | O pacote de igreja traz "Bíblia Sagrada, Nova Versão Internacional®, NVI® © 1993, 2000 Biblica, Inc.® Usado com permissão."; o de loja traz o texto digitado. | 🟢 |
| RF-07 | A página deve mostrar uma prévia da tela inicial em proporção 16:9 (referência 1920×1080), com fundo, logotipo, mensagem, um versículo e apps de exemplo dentro da moldura, atualizada a cada alteração. | Must | Mudar a cor de fundo muda a prévia sem recarregar a página. | 🟡 |
| RF-08 | "Gerar pacote" deve montar o `.tematv` no formato v1 e validá-lo com as mesmas regras do launcher antes de salvar; se recusado, mostra o campo e o motivo e não salva. | Must | O pacote gerado é aceito na TV; um pacote inválido nunca aparece em `~/tematv/temas/`. | 🟢 |
| RF-09 | A ferramenta deve redimensionar imagens maiores que 1920×1080 e recusar gerar se o pacote passar de 15 MB, informando o tamanho. | Must | Um fundo de 4000×3000 entra no pacote com no máximo 1920×1080. | 🟡 |
| RF-10 | A ferramenta deve salvar o projeto em `~/tematv/clientes/<theme_id>/` (dados e imagens) e o pacote em `~/tematv/temas/<theme_id>.tematv`. | Must | Os dois caminhos existem após gerar. | 🟡 |
| RF-11 | O usuário deve poder reabrir um projeto salvo, alterar campos e gerar de novo. | Must | Alterar só a mensagem e gerar produz um pacote com a mensagem nova e os mesmos versículos. | 🟡 |
| RF-12 | A tela "Ativar TV" deve receber o código do aparelho (com ou sem traços) e o nome do cliente, mostrar a senha de ativação em `XXXX-XXXX-XXXX` e registrar a geração em `~/tematv/ativacoes.csv`, com o mesmo cálculo do `activation-tool`. | Must | A senha mostrada é igual à do `activation-tool senha` para o mesmo código; código com dígitos a menos é recusado. | 🟢 |
| RF-13 | Sem o arquivo do segredo de ativação, a tela "Ativar TV" deve explicar como gerá-lo ou restaurá-lo do backup, sem impedir o resto da ferramenta. | Should | Sem o arquivo, temas continuam sendo gerados e a tela de ativação mostra a orientação. | 🟡 |
| RF-14 | Ao gerar, a página deve mostrar o caminho do pacote e os comandos do ADB para enviá-lo e ativá-lo na TV, prontos para copiar, até a feature 006 automatizar a instalação. | Should | Os comandos copiados, rodados com a TV conectada, aplicam o tema. | 🟡 |

## 6. Requisitos Não Funcionais

| Tipo | Requisito | Evidência ou justificativa | Confidência |
|------|-----------|----------------------------|-------------|
| Plataforma | Linux e WSL2 com Ubuntu; navegador do Windows acessa o `localhost` do WSL | `_reversa_sdd/sdd/theme-builder.md#7-requisitos-não-funcionais` (RNF-01) | 🟢 |
| Instalação | Instalar com um único script, sem o revendedor compilar nada à mão | `_reversa_sdd/sdd/theme-builder.md#7-requisitos-não-funcionais` (RNF-05) | 🟡 |
| Desempenho | Gerar e validar um pacote de até 15 MB em até 10 s; prévia atualiza em até 300 ms após a alteração | Meta de preparar um cliente em até 15 minutos | 🟡 |
| Segurança | Página só em `127.0.0.1`; segredo de ativação fora da página, logs e pacotes (RN-04) | RN-03, RN-04 | 🟢 |
| Compatibilidade | Pacote aceito pelo launcher das features 001 e 002 (`theme.json` v1) | RN-01 | 🟢 |
| Usabilidade | Textos da página em português, sem termos técnicos; erros ao lado do campo afetado | Persona iniciante | 🟡 |
| Observabilidade | Terminal registra início, porta, pacote gerado e erros, sem o segredo nem a senha gerada | Suporte do revendedor | 🟡 |

## 7. Critérios de Aceitação

```gherkin
Cenário: Cliente novo do início ao pacote
  Dado a ferramenta aberta sem projetos
  Quando o revendedor cria "Igreja Batista X", envia um logotipo PNG, escolhe as cores e a moldura "Bíblia" e clica em "Gerar pacote"
  Então ~/tematv/temas/igreja-batista-x.tematv é criado com os 20 versículos NVI e a atribuição da NVI
  E a TV aplica o pacote sem erro

Cenário: Cliente de loja
  Dado a ferramenta aberta
  Quando o revendedor cria um cliente do tipo loja, escolhe a moldura "Hambúrguer" e escreve duas mensagens da loja
  Então o pacote gerado traz as duas mensagens com a versão LOJA e a atribuição editada
  E a TV aplica o pacote sem erro

Cenário: Versículo digitado à mão na igreja
  Dado um projeto de igreja
  Quando o revendedor acrescenta um versículo digitado à mão
  Então o versículo sai no pacote com a versão NVI e a atribuição continua a da NVI

Cenário: Prévia acompanha o formulário
  Dado um projeto aberto
  Quando o revendedor muda a mensagem
  Então a prévia mostra a mensagem nova sem recarregar a página

Cenário: Mensagem longa demais
  Dado o formulário do projeto
  Quando a mensagem passa de 120 caracteres
  Então aparece o erro ao lado do campo e o pacote não é gerado

Cenário: Pacote que o launcher recusaria
  Dado um projeto com a lista de versículos vazia
  Quando o revendedor clica em "Gerar pacote"
  Então a página mostra o campo e o motivo
  E nenhum arquivo novo aparece em ~/tematv/temas/

Cenário: Reabrir e alterar
  Dado o projeto "igreja-batista-x" salvo
  Quando o revendedor muda só a mensagem e gera de novo
  Então o pacote novo tem a mensagem nova e os mesmos versículos

Cenário: Gerar a senha de ativação
  Dado o segredo de ativação no notebook
  Quando o revendedor digita o código 2544-3298-7734 e o nome do cliente em "Ativar TV"
  Então a página mostra a mesma senha que o activation-tool gera para esse código
  E a geração é registrada em ~/tematv/ativacoes.csv

Cenário: Segredo ausente
  Dado o notebook sem o arquivo do segredo de ativação
  Quando o revendedor abre "Ativar TV"
  Então a página explica como gerar ou restaurar o segredo
  E a geração de temas continua funcionando

Cenário: Acesso pela rede
  Dado a ferramenta aberta no notebook
  Quando outro computador da rede tenta abrir a página
  Então a conexão é recusada
```

## 8. Prioridade MoSCoW

| Item | MoSCoW | Justificativa |
|------|--------|---------------|
| RF-01 a RF-12 | Must | Sem eles o revendedor não prepara nem ativa um cliente sem programar |
| RF-13, RF-14 | Should | Orientação de suporte e ponte até a instalação automática da feature 006 |
| RNF de desempenho | Should | Meta de 15 minutos por cliente |

## 9. Esclarecimentos

### Sessão 2026-10-30

- **Q:** A ferramenta prepara só temas cristãos ou já atende outros segmentos (como a hamburgueria)?
  **R:** Vários segmentos: ao criar o cliente, escolhe o tipo igreja (moldura de Bíblia e versículos NVI) ou loja (moldura de hambúrguer e mensagens da loja). Aplicado em RN-06, RF-02, RF-04, RF-05, RF-06 e no cenário "Cliente de loja".
- **Q:** Nos temas de igreja, o revendedor pode acrescentar versículos de outras versões digitados à mão?
  **R:** Só NVI: os versículos digitados à mão também são NVI e a atribuição da Biblica cobre todos. Aplicado em RN-07, RF-05, RF-06 e no cenário "Versículo digitado à mão na igreja".

## 10. Lacunas

- Nenhuma lacuna aberta.

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-30 | Versão inicial gerada por `/reversa-requirements` | reversa |
| 2026-10-30 | Sessão de esclarecimentos: tipos de cliente igreja e loja; versículos só da NVI | reversa-clarify |
