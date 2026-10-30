# Spec: Tela inicial do launcher (launcher-home)

**Versão:** 1.0
**Status:** Rascunho
**Autor:** reversa-spec-sdd
**Data:** 2026-10-29
**Reviewers:** Augusto

> Selo 🟡 PLANEJADO em todos os itens. Fonte: [`prd.md`](../prd.md), seções 4, 6 e 9.

---

## 1. Resumo

🟡 A tela inicial é o que a TV mostra no lugar do launcher do Google. Ela lista os apps instalados, desenha cada um dentro da moldura do tema (no tema infantil cristão, uma Bíblia), mostra o logotipo, a mensagem da igreja e o versículo do dia, e abre o app escolhido pelo controle remoto.

---

## 2. Contexto e Motivação

**Problema:**
🟡 O launcher do Google TV não aceita temas: não há como colocar o logotipo, as cores e a mensagem de uma igreja na tela inicial.

**Evidências:**
🟡 O PRD define como valor central que "a TV passa a ter a identidade da igreja no momento em que é ligada" e como critério de aceite que cada app apareça dentro da moldura em formato de Bíblia com o versículo do dia, mesmo sem internet.

**Por que agora:**
🟡 É a parte visível do produto; sem ela não há o que vender nem o que testar na TV de referência.

---

## 3. Goals (Objetivos)

- [ ] 🟡 G-01: A tela inicial mostra a identidade do cliente (logotipo, cores, mensagem) usando o tema ativo.
- [ ] 🟡 G-02: O usuário navega e abre qualquer app instalado apenas com o direcional e o botão OK do controle.
- [ ] 🟡 G-03: O versículo do dia aparece sem depender de internet.
- [ ] 🟡 G-04: O launcher roda leve na TCL de referência com 2 GB de RAM.

**Métricas de sucesso:**
| Métrica | Baseline atual | Target | Prazo |
|---------|---------------|--------|-------|
| 🟡 Tempo de abertura a frio da tela inicial na TCL | não existe | ≤ 2 s | MVP |
| 🟡 Memória (PSS) do launcher em primeiro plano | launcher do Google: 109 MB + 44 MB de serviços | ≤ 100 MB | MVP |
| 🟡 Apps instalados com lançador de TV exibidos na grade | não existe | 100% | MVP |

---

## 4. Non-Goals (Fora do Escopo)

- 🟡 NG-01: Esconder, bloquear ou reordenar apps por perfil (modo criança), retirado do escopo no PRD.
- 🟡 NG-02: Linhas de recomendação de conteúdo, busca por voz ou integração com o Google Assistente.
- 🟡 NG-03: Widgets, relógio animado, previsão do tempo ou notícias.
- 🟡 NG-04: Leitura de versículos pela internet a partir da TV.
- 🟡 NG-05: Configurar Wi-Fi, Bluetooth ou ajustes do sistema dentro do launcher; o launcher apenas abre as Configurações do Android.

---

## 5. Usuários e Personas

**Usuário primário:** 🟡 Público da igreja e das famílias (líderes, professores, crianças, membros), que liga a TV e escolhe um app.
**Usuário secundário:** 🟡 Revendedor e instalador ([`personas.md`](../personas.md)), que confere o tema e usa a tela de diagnóstico.

**Jornada atual (sem a feature):**
1. 🟡 A TV liga e mostra a tela inicial genérica do Google.
2. 🟡 Nada na tela remete à igreja.

**Jornada futura (com a feature):**
1. 🟡 A TV liga e mostra a tela temática com logotipo, mensagem e versículo do dia.
2. 🟡 O usuário escolhe um app, que aparece dentro de uma Bíblia, e aperta OK.
3. 🟡 O app abre; ao apertar Home, a tela temática volta.

---

## 6. Requisitos Funcionais

### 6.1 Requisitos Principais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-01 | 🟡 O sistema deve listar todos os apps instalados que declaram atividade de lançador para TV (categoria `LEANBACK_LAUNCHER`), exceto o próprio launcher. | Must | Com YouTube, Netflix e XCIPTV instalados, os três aparecem na grade. |
| RF-02 | 🟡 O sistema deve desenhar cada app dentro da moldura do tema, com o banner ou ícone do app em `icon_slot` e o nome do app em `label_slot`. | Must | No tema bíblico, cada app aparece dentro da imagem da Bíblia com o nome na capa. |
| RF-03 | 🟡 O sistema deve ordenar os apps por nome, em ordem alfabética, ignorando acentos e maiúsculas. | Must | "Disney+" aparece antes de "Netflix", que aparece antes de "YouTube". |
| RF-04 | 🟡 O usuário deve poder mover o foco entre os apps com o direcional e abrir o app em foco com o botão OK. | Must | Navegação completa sem mouse nem toque; OK abre o app em foco. |
| RF-05 | 🟡 O sistema deve destacar o app em foco com a cor `accent` do tema e ampliação de 10%. | Must | O item em foco tem borda na cor `accent` e fica 10% maior. |
| RF-06 | 🟡 O sistema deve exibir o logotipo, a mensagem e a imagem ou cor de fundo do tema ativo. | Must | Trocando o tema, logotipo, mensagem e fundo mudam juntos. |
| RF-07 | 🟡 O sistema deve exibir o versículo do dia escolhido pelo índice `(dias desde 1970-01-01 no fuso da TV) mod (quantidade de versículos do tema)`, com referência e sigla da versão. | Must | Em dias consecutivos o versículo muda; no mesmo dia, reabrir a tela mostra o mesmo versículo. |
| RF-08 | 🟡 O sistema deve exibir a atribuição da tradução (`verses_attribution`) abaixo do versículo. | Must | O texto de atribuição aparece sempre que um versículo é exibido. |
| RF-09 | 🟡 O sistema deve atualizar a grade quando um app é instalado ou removido, sem reiniciar a TV. | Must | Após `adb install`, o app novo aparece na grade em até 5 s. |
| RF-10 | 🟡 O sistema deve oferecer um item fixo "Configurações" que abre as Configurações do Android. | Must | O item abre a tela de Configurações da TV. |
| RF-11 | 🟡 O sistema deve oferecer um item fixo "Tema" que abre a tela de opções do launcher (ativar ou desativar a troca de tela inicial e ver o diagnóstico). | Must | O item abre as opções descritas no `home-takeover`. |
| RF-12 | 🟡 O sistema deve oferecer uma tela de diagnóstico com: versão do app, tema ativo, último erro de tema, estado do serviço de acessibilidade e estado da licença. | Should | A tela mostra os cinco itens com valores atuais. |
| RF-13 | 🟡 O sistema deve voltar o foco ao último app aberto quando a tela inicial é exibida de novo. | Could | Após abrir e fechar o YouTube, o foco está no YouTube. |
| RF-14 | 🟡 O sistema deve ignorar o botão Voltar na tela inicial (grade de apps), sem fechar a tela nem exibir o launcher do Google; nas telas "Tema" e diagnóstico, Voltar retorna à grade. | Must | Apertar Voltar 10 vezes seguidas na grade mantém a tela temática visível. |

### 6.2 Fluxo Principal (Happy Path)

1. 🟡 O usuário liga a TV ou aperta Home.
2. 🟡 O sistema exibe a tela inicial com o tema ativo, o versículo do dia e a grade de apps.
3. 🟡 O usuário move o foco até um app com o direcional.
4. 🟡 O sistema destaca o app em foco.
5. 🟡 O usuário aperta OK.
6. 🟡 Resultado: o app escolhido abre em tela cheia.

### 6.3 Fluxos Alternativos

**Fluxo Alternativo A — App sem banner:**
1. 🟡 O app não tem banner de TV.
2. 🟡 O sistema usa o ícone do app dentro de `icon_slot`, centralizado e sem distorção.

**Fluxo Alternativo B — Licença inválida:**
1. 🟡 O `license-activation` informa que a licença não é válida.
2. 🟡 O sistema exibe a tela de ativação no lugar da grade temática (ver `license-activation`).

---

## 7. Requisitos Não-Funcionais

| ID | Requisito | Valor alvo | Observação |
|----|-----------|-----------|------------|
| RNF-01 | 🟡 Performance | Abertura a frio ≤ 2 s; troca de foco ≤ 100 ms | Medido na TCL Android 11 de referência |
| RNF-02 | 🟡 Memória | PSS ≤ 100 MB em primeiro plano | Medido com `dumpsys meminfo` |
| RNF-03 | 🟡 Compatibilidade | Android TV 9 (API 28) ou superior | ⚠️ ABERTO: versão mínima depende das TVs dos clientes (OQ-01) |
| RNF-04 | 🟡 Legibilidade | Texto do versículo com no mínimo 28 px em tela 1080p | Distância de sofá |
| RNF-05 | 🟡 Funcionamento offline | 100% das funções da tela inicial sem internet | Versículos vêm do pacote |

---

## 8. Design e Interface

**Componentes afetados:** 🟡 tela inicial (cabeçalho com logotipo e mensagem, faixa do versículo, grade de apps), tela de opções "Tema", tela de diagnóstico.

**Comportamento esperado:**
🟡 Cabeçalho no topo com logotipo à esquerda e mensagem ao lado; faixa do versículo do dia abaixo; grade de apps ocupando o resto da tela, com rolagem vertical quando os apps não couberem. O foco inicial fica no primeiro app da grade.

**Estados da UI:**
- 🟡 Estado vazio: sem apps de TV instalados, a grade mostra só os itens fixos "Configurações" e "Tema".
- 🟡 Estado de carregamento: enquanto a lista de apps carrega, a grade mostra molduras vazias do tema.
- 🟡 Estado de erro: se o tema ativo falhar, a tela usa o tema padrão e o diagnóstico mostra o erro.
- 🟡 Estado de sucesso: tela temática completa com grade e versículo.

---

## 9. Modelo de Dados

**Entidades novas ou modificadas:**

```
LauncherPrefs (preferências locais do app) {
  last_focused_package: string?    // último app aberto, para RF-13
}

AppEntry (em memória) {
  package_name: string
  label: string                    // nome exibido
  banner_or_icon: imagem
  launch_intent: Intent
}
```

**Migrações necessárias:** 🟡 Não.

---

## 10. Integrações e Dependências

| Dependência | Tipo | Impacto se indisponível |
|-------------|------|------------------------|
| 🟡 `theme-engine` | Obrigatória | Usa o tema padrão embutido |
| 🟡 `home-takeover` | Obrigatória | Sem ele, a tela só aparece ao abrir o launcher pela lista de apps do Google |
| 🟡 `license-activation` | Obrigatória | Sem licença válida, mostra a tela de ativação |
| 🟡 Gerenciador de pacotes do Android | Obrigatória | Falha na consulta mostra só os itens fixos e registra o erro no diagnóstico |

---

## 11. Edge Cases e Tratamento de Erros

| Cenário | Trigger | Comportamento esperado |
|---------|---------|----------------------|
| 🟡 EC-01: App removido com foco nele | Desinstalação enquanto a tela está aberta | Remove o item e move o foco para o item seguinte. |
| 🟡 EC-02: App não abre | Intent de lançamento falha | Mostra aviso "Não foi possível abrir <nome do app>" por 4 s e mantém a tela inicial. |
| 🟡 EC-03: Nome de app longo | Nome maior que `label_slot` | Corta com reticências em uma linha. |
| 🟡 EC-04: Quantidade alta de apps | Mais apps do que cabem na tela | Grade com rolagem vertical mantendo o foco visível. |
| 🟡 EC-05: Data da TV errada | Relógio sem sincronizar (sem internet) | Usa a data do sistema mesmo assim; o versículo continua válido, só não corresponde ao dia real. |
| 🟡 EC-06: Falha ao ler o tema | Erro do `theme-engine` | Exibe o tema padrão e registra o erro no diagnóstico. |
| 🟡 EC-07: Versículo longo | Texto acima de 3 linhas | Reduz a fonte até 28 px; se ainda não couber, corta com reticências e mantém a referência visível. |

---

## 12. Segurança e Privacidade

- 🟡 **Autenticação:** não há login; qualquer pessoa com o controle usa a TV.
- 🟡 **Autorização:** a tela de diagnóstico é somente leitura; desativar a troca de tela inicial fica na tela "Tema".
- 🟡 **Dados sensíveis:** o launcher não coleta nem envia dados; só lê a lista de apps instalados localmente.
- 🟡 **Auditoria:** não se aplica no MVP.

---

## 13. Plano de Rollout

- 🟡 **Estratégia:** instalar primeiro na TCL de referência, depois em clientes piloto.
- 🟡 **Como reverter (rollback):** desativar a troca de tela inicial na tela "Tema" (volta ao launcher do Google) ou desinstalar o app por ADB.
- 🟡 **Monitoramento pós-deploy:** medir abertura e memória com `dumpsys meminfo` na TCL e conferir o diagnóstico em cada TV instalada.

---

## 14. Open Questions

| # | Pergunta | Impacto | Dono | Prazo |
|---|---------|---------|------|-------|
| OQ-01 | 🟡 Qual a versão mínima de Android TV das TVs dos clientes? | Médio | Augusto | Antes do primeiro cliente fora da TCL |
| OQ-02 | 🟡 O launcher deve declarar a categoria `HOME` do Android, ou isso faz o Google TV mostrar um seletor de tela inicial indesejado? Validar na TCL. | Alto | Augusto | Antes da implementação do `home-takeover` |

---

## 15. Decisões Tomadas (Decision Log)

| Decisão | Alternativas consideradas | Racional |
|---------|--------------------------|---------|
| 🟡 Grade única de apps em ordem alfabética | Linhas por categoria; ordem manual | Mais simples para o MVP e previsível para o usuário |
| 🟡 Versículo do dia por índice de data | Sorteio aleatório a cada abertura | O mesmo versículo o dia todo e funciona sem internet |
| 🟡 Itens fixos "Configurações" e "Tema" na grade | Menu escondido por combinação de teclas | O revendedor acha as opções sem treinamento |
| 🟡 `applicationId` provisório `br.tematv.launcher` | Outros nomes | Pode ser renomeado antes da primeira distribuição |

---

## Apêndice

### Referências
- 🟡 [`prd.md`](../prd.md), [`personas.md`](../personas.md)
- 🟡 Specs relacionadas: [`theme-engine.md`](./theme-engine.md), [`home-takeover.md`](./home-takeover.md), [`license-activation.md`](./license-activation.md)

### Histórico de Revisões
| Versão | Data | Autor | Mudanças |
|--------|------|-------|---------|
| 1.0 | 2026-10-29 | reversa-spec-sdd | Criação inicial |

---

## Relatório de avaliação

🟡 **Score total:** 100/100 (⭐ Excelente — Pronta para implementação)  
🟡 **Iterações:** 2 (100 → 100, após correções de revisão)  
🟡 **Avaliado em:** 2026-10-29T19:51:15-03:00 por `spec_scorer.py`

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
