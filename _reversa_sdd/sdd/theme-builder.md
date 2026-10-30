# Spec: Ferramenta de preparo de tema (theme-builder)

**Versão:** 1.0
**Status:** Rascunho
**Autor:** reversa-spec-sdd
**Data:** 2026-10-29
**Reviewers:** Augusto

> Selo 🟡 PLANEJADO em todos os itens. Fonte: [`prd.md`](../prd.md), seções 4, 6, 7 e 9; respostas da Fase 1 (Linux/WSL, página no navegador).

---

## 1. Resumo

🟡 Ferramenta que roda no notebook do revendedor (Linux ou WSL) e abre uma página local no navegador. Nela, o revendedor preenche os dados do cliente (logotipo, cores, mensagem, moldura dos ícones, versão da Bíblia e lista de versículos), vê uma prévia e gera o pacote `.tematv`. Os versículos são buscados na Bíblia API com a chave do revendedor, que fica só no notebook.

---

## 2. Contexto e Motivação

**Problema:**
🟡 O revendedor é iniciante em programação Android e precisa preparar o tema de cada igreja antes da visita, sem editar arquivos JSON nem compilar nada.

**Evidências:**
🟡 Jornada da persona, passo 1: preparar o tema do cliente (logotipo, cores, mensagem) no notebook antes da visita. Restrição do PRD: a chave da Bíblia API não pode ficar no app da TV.

**Por que agora:**
🟡 Sem esta ferramenta não há como gerar o pacote consumido pelo `theme-engine`.

---

## 3. Goals (Objetivos)

- [ ] 🟡 G-01: O revendedor gera um pacote `.tematv` válido sem editar arquivos à mão.
- [ ] 🟡 G-02: Os versículos são baixados uma vez, no notebook, e embutidos no pacote.
- [ ] 🟡 G-03: A chave da Bíblia API nunca vai para o pacote nem para a TV.
- [ ] 🟡 G-04: O revendedor reabre o projeto de um cliente e altera o tema sem recomeçar.

**Métricas de sucesso:**
| Métrica | Baseline atual | Target | Prazo |
|---------|---------------|--------|-------|
| 🟡 Tempo para preparar o tema de um cliente novo | não existe | ≤ 15 min | MVP |
| 🟡 Pacotes gerados que falham na validação do `theme-engine` | não existe | 0% | MVP |
| 🟡 Pacotes que contêm a chave da API | não existe | 0% | MVP |

---

## 4. Non-Goals (Fora do Escopo)

- 🟡 NG-01: Editor visual de arrastar e soltar elementos na tela; a ferramenta usa um formulário com prévia.
- 🟡 NG-02: Rodar no Windows nativo ou no macOS no MVP; só Linux e WSL.
- 🟡 NG-03: Hospedar a ferramenta na internet ou ter contas de usuário.
- 🟡 NG-04: Criar molduras de ícone dentro da ferramenta; o revendedor escolhe uma moldura pronta ou envia uma imagem PNG.
- 🟡 NG-05: Tema hamburgueria pronto; o formato o suporta, mas o MVP traz só a moldura de Bíblia.

---

## 5. Usuários e Personas

**Usuário primário:** 🟡 Revendedor e instalador ([`personas.md`](../personas.md)), iniciante em programação Android, que prepara o tema antes da visita.
**Usuário secundário:** 🟡 Nenhum no MVP.

**Jornada atual (sem a feature):**
1. 🟡 Não há como gerar um tema para a TV sem programar.

**Jornada futura (com a feature):**
1. 🟡 O revendedor roda um comando no terminal e a página abre no navegador.
2. 🟡 Preenche o formulário do cliente e confere a prévia.
3. 🟡 Clica em "Gerar pacote" e recebe `igreja-x.tematv`, pronto para o `device-provisioning`.

---

## 6. Requisitos Funcionais

### 6.1 Requisitos Principais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-01 | 🟡 O sistema deve iniciar com um único comando (`tematv`) e abrir a página em `http://localhost:8787`, aceitando conexões apenas de `127.0.0.1`. | Must | O comando abre a página; acesso de outro computador da rede é recusado. |
| RF-02 | 🟡 O usuário deve poder cadastrar a chave da Bíblia API uma vez, numa tela de configurações; o sistema a guarda em `~/.config/tematv/config.json` com permissão `600`. | Must | Após cadastrar, a chave persiste entre execuções e o arquivo tem permissão `600`. |
| RF-03 | 🟡 O usuário deve poder criar um projeto de cliente com: nome do cliente, `theme_id` (gerado do nome em kebab-case e editável), logotipo PNG, cores de fundo, texto e destaque, imagem de fundo opcional e mensagem de até 120 caracteres. | Must | O formulário recusa mensagem acima de 120 caracteres e logotipo que não seja PNG. |
| RF-04 | 🟡 O usuário deve poder escolher a moldura dos ícones entre as molduras prontas (MVP: "Bíblia") ou enviar uma moldura PNG com as áreas `icon_slot` e `label_slot`. | Must | A moldura "Bíblia" já vem com as áreas definidas; a prévia mostra um app dentro dela. |
| RF-05 | 🟡 O sistema deve listar as versões da Bíblia em português disponíveis em `GET /versions` da Bíblia API, com a sigla e o detentor dos direitos. | Must | A lista mostra ACF, ARA, ARC, NVI e as demais retornadas pela API, cada uma com o detentor. |
| RF-06 | 🟡 O sistema deve oferecer uma lista padrão de 30 referências bíblicas voltadas ao público infantil e permitir adicionar, remover e reordenar referências até o máximo de 366. | Must | A lista padrão tem 30 itens; a 367ª referência é recusada. |
| RF-07 | 🟡 O sistema deve buscar o texto de cada referência em `GET /versions/{versao}/books/{livro}/chapters/{capitulo}/verses/{versiculo}`, enviando a chave no header `Authorization: Bearer`. | Must | Com chave válida e internet, todos os textos são preenchidos e exibidos para conferência. |
| RF-08 | 🟡 O sistema deve guardar os textos já baixados no projeto do cliente e não buscar de novo referências já baixadas na mesma versão. | Must | Gerar o pacote duas vezes faz requisições à API só na primeira. |
| RF-09 | 🟡 O sistema deve mostrar uma prévia da tela inicial em 1920×1080 com o logotipo, a mensagem, o versículo do dia e apps de exemplo dentro da moldura. | Must | A prévia muda ao alterar qualquer campo do formulário. |
| RF-10 | 🟡 O sistema deve gerar o pacote `.tematv` no formato da spec `theme-engine` (seção 9) e validá-lo com as mesmas regras antes de salvar. | Must | O pacote gerado é aceito pelo `theme-engine` sem erro. |
| RF-11 | 🟡 O sistema deve salvar o projeto em `~/tematv/clientes/<theme_id>/` (projeto, imagens, versículos) e o pacote em `~/tematv/temas/<theme_id>.tematv`. | Must | Os dois caminhos existem após gerar. |
| RF-12 | 🟡 O usuário deve poder reabrir um projeto salvo, alterar campos e gerar um novo pacote. | Must | Alterar só a mensagem e gerar de novo produz um pacote com a mensagem nova e os mesmos versículos. |
| RF-13 | 🟡 O sistema deve preencher `verses_attribution` com o nome da versão e o detentor dos direitos informados pela API. | Must | O pacote com ACF traz "Almeida Corrigida e Fiel, SBTB". |
| RF-14 | 🟡 O sistema deve avisar, antes de gerar, que o uso comercial do texto depende da licença do detentor da versão escolhida e exigir que o usuário marque a confirmação "Tenho autorização para usar esta versão". | Should | Sem marcar, o botão "Gerar pacote" fica desabilitado. |

### 6.2 Fluxo Principal (Happy Path)

1. 🟡 O usuário roda `tematv` no terminal; o sistema abre a página.
2. 🟡 O usuário cria o projeto "Igreja Batista X", envia o logotipo, escolhe as cores, escreve a mensagem e escolhe a moldura "Bíblia".
3. 🟡 O usuário escolhe a versão ACF e mantém a lista padrão de 30 referências.
4. 🟡 O sistema baixa os 30 versículos e mostra os textos.
5. 🟡 O usuário confere a prévia, marca a confirmação de licença e clica em "Gerar pacote".
6. 🟡 Resultado: `~/tematv/temas/igreja-batista-x.tematv` é criado e validado.

### 6.3 Fluxos Alternativos

**Fluxo Alternativo A — Projeto existente:**
1. 🟡 O usuário abre "Clientes" e escolhe um projeto.
2. 🟡 Altera campos e gera de novo; os versículos já baixados são reaproveitados.

**Fluxo Alternativo B — Versículo escrito à mão:**
1. 🟡 Uma referência não existe na API ou a API está fora do ar.
2. 🟡 O usuário pode digitar o texto do versículo à mão; o sistema marca o item como "texto manual".

---

## 7. Requisitos Não-Funcionais

| ID | Requisito | Valor alvo | Observação |
|----|-----------|-----------|------------|
| RNF-01 | 🟡 Plataforma | Ubuntu 22.04+ e WSL2 com Ubuntu | Navegador do Windows acessa `localhost` do WSL |
| RNF-02 | 🟡 Segurança | Chave da API só em `~/.config/tematv/config.json` (permissão `600`), nunca no pacote nem nos logs | Termos da BIBLIAAPI |
| RNF-03 | 🟡 Performance | Baixar 30 versículos em ≤ 60 s com internet estável | Requisições em sequência com até 4 em paralelo |
| RNF-04 | 🟡 Tamanho | Pacote gerado ≤ 15 MB; imagens redimensionadas para no máximo 1920×1080 | Limite do `theme-engine` |
| RNF-05 | 🟡 Instalação | Instalar a ferramenta com um único script, sem compilar nada | Revendedor iniciante em programação Android |

---

## 8. Design e Interface

**Componentes afetados:** 🟡 tela "Clientes" (lista de projetos), tela "Projeto" (formulário e prévia), tela "Configurações" (chave da API), aba "Instalar na TV" (definida em `device-provisioning`).

**Comportamento esperado:**
🟡 O formulário fica à esquerda e a prévia à direita, atualizada a cada alteração. Os versículos aparecem numa lista com o status de cada um (baixado, erro, manual).

**Estados da UI:**
- 🟡 Estado vazio: sem projetos, a tela "Clientes" mostra o botão "Novo cliente".
- 🟡 Estado de carregamento: durante o download dos versículos, barra de progresso "12 de 30".
- 🟡 Estado de erro: mensagem específica por erro (seção 11) ao lado do campo ou do versículo afetado.
- 🟡 Estado de sucesso: "Pacote gerado em ~/tematv/temas/<theme_id>.tematv" com botão "Instalar na TV".

---

## 9. Modelo de Dados

**Entidades novas ou modificadas:**

```
ToolConfig (~/.config/tematv/config.json, permissão 600) {
  biblia_api_key: string
  biblia_api_base_url: string       // padrão "https://bibliaapi.com.br/api/v2"
}

ClientProject (~/tematv/clientes/<theme_id>/projeto.json) {
  theme_id: string
  display_name: string
  colors: {background, text, accent}
  logo_file: string
  background_file: string?
  message: string
  icon_frame: {preset: "biblia"} | {image, icon_slot, label_slot}
  bible_version: string             // ex.: "ACF"
  verses: [{reference, text, version, source: "api" | "manual"}]
  license_confirmed: bool           // RF-14
  updated_at: datetime
}
```

**Migrações necessárias:** 🟡 Não.

---

## 10. Integrações e Dependências

| Dependência | Tipo | Impacto se indisponível |
|-------------|------|------------------------|
| 🟡 BIBLIAAPI (`https://bibliaapi.com.br/api/v2`) | Obrigatória para versículos novos | Permite texto manual (Fluxo B) e reaproveita os já baixados |
| 🟡 Navegador no notebook | Obrigatória | Sem navegador, a ferramenta não tem interface |
| 🟡 `theme-engine` (formato e regras de validação) | Obrigatória | O pacote precisa seguir o esquema da versão 1 |
| 🟡 `device-provisioning` (aba "Instalar na TV") | Opcional | O pacote pode ser copiado para pendrive à mão |

---

## 11. Edge Cases e Tratamento de Erros

| Cenário | Trigger | Comportamento esperado |
|---------|---------|----------------------|
| 🟡 EC-01: Chave ausente | Nenhuma chave cadastrada | Ao tentar baixar, mostra "Cadastre a chave da Bíblia API em Configurações" com link para a tela. |
| 🟡 EC-02: Chave inválida | API responde 401 ou 403 | Mostra "Chave recusada pela Bíblia API" e não marca nenhum versículo como baixado. |
| 🟡 EC-03: Limite mensal atingido | API responde 429 | Para os downloads, mantém os já baixados e mostra "Limite mensal da Bíblia API atingido"; permite texto manual. |
| 🟡 EC-04: Sem internet ou API fora do ar | Timeout de 10 s ou erro 5xx | Tenta de novo 2 vezes com 2 s de intervalo; depois marca o versículo como erro e permite texto manual. |
| 🟡 EC-05: Referência inexistente | API responde 404 | Marca a referência como "não encontrada nesta versão". |
| 🟡 EC-06: Logotipo inválido | Arquivo não PNG ou acima de 5 MB | Recusa com "Envie um PNG de até 5 MB". |
| 🟡 EC-07: `theme_id` já existe | Novo cliente com o mesmo nome | Pede outro `theme_id` ou abrir o projeto existente. |
| 🟡 EC-08: Porta 8787 ocupada | Outra aplicação na porta | Usa a próxima porta livre até 8797 e mostra a URL no terminal. |

---

## 12. Segurança e Privacidade

- 🟡 **Autenticação:** não há login; o acesso é restrito a `127.0.0.1`.
- 🟡 **Autorização:** só o usuário do notebook lê `config.json` (permissão `600`).
- 🟡 **Dados sensíveis:** a chave da API é segredo; não aparece em pacote, prévia, log ou mensagem de erro. Os projetos guardam nome e imagens do cliente, sem dados pessoais de membros.
- 🟡 **Auditoria:** `updated_at` em cada projeto.

---

## 13. Plano de Rollout

- 🟡 **Estratégia:** uso interno do revendedor; primeiro com um cliente piloto.
- 🟡 **Como reverter (rollback):** os projetos e pacotes anteriores ficam em `~/tematv/`; reinstalar a versão anterior da ferramenta não altera esses arquivos.
- 🟡 **Monitoramento pós-deploy:** conferir se os pacotes gerados são aceitos na TV (diagnóstico do launcher).

---

## 14. Open Questions

| # | Pergunta | Impacto | Dono | Prazo |
|---|---------|---------|------|-------|
| OQ-01 | 🟡 Qual versão da Bíblia tem uso comercial autorizado para o produto, e em que condições? Contatar o detentor (SBTB, SBB ou Biblica). | Alto | Augusto | Antes do primeiro cliente pagante |
| OQ-02 | 🟡 Qual o limite mensal do plano Free da Bíblia API? Não está publicado. | Baixo | Augusto | Antes do 10º cliente |
| OQ-03 | 🟡 Quais 30 referências compõem a lista padrão infantil? | Médio | Augusto | Antes da implementação |

---

## 15. Decisões Tomadas (Decision Log)

| Decisão | Alternativas consideradas | Racional |
|---------|--------------------------|---------|
| 🟡 Página local no navegador | Linha de comando com arquivo de configuração | Escolha do usuário: mais amigável para iniciante |
| 🟡 Linux e WSL | Windows nativo | Escolha do usuário; é o ambiente usado hoje |
| 🟡 Versículos por referência, não aleatórios | Endpoint `/random` | Garante conteúdo adequado ao público infantil e reaproveitamento |
| 🟡 Chave só no notebook | Proxy na nuvem | Termos da API; o MVP não tem servidor |

---

## Apêndice

### Referências
- 🟡 [`prd.md`](../prd.md), [`personas.md`](../personas.md)
- 🟡 Documentação da BIBLIAAPI: `https://bibliaapi.com.br/integrar#api`, `https://bibliaapi.com.br/licencas`, `https://bibliaapi.com.br/termos`
- 🟡 Specs relacionadas: [`theme-engine.md`](./theme-engine.md), [`device-provisioning.md`](./device-provisioning.md)

### Histórico de Revisões
| Versão | Data | Autor | Mudanças |
|--------|------|-------|---------|
| 1.0 | 2026-10-29 | reversa-spec-sdd | Criação inicial |

---

## Relatório de avaliação

🟡 **Score total:** 100/100 (⭐ Excelente — Pronta para implementação)  
🟡 **Iterações:** 2 (95 → 100)  
🟡 **Avaliado em:** 2026-10-29T19:48:39-03:00 por `spec_scorer.py`

| Dimensão | Score | Peso |
|---|---|---|
| Completude | 100% | 30% |
| Testabilidade | 100% | 25% |
| Clareza | 100% | 20% |
| Escopo | 100% | 15% |
| Edge Cases | 100% | 10% |

**Gaps críticos:** nenhum

**Sugestões:** nenhuma

> O score mede estrutura, testabilidade e clareza do texto. Ele não valida a viabilidade técnica: os riscos reais estão na seção 14 (Open Questions).
