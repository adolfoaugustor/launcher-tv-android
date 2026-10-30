# Spec: Troca automática da tela inicial (home-takeover)

**Versão:** 1.0
**Status:** Rascunho
**Autor:** reversa-spec-sdd
**Data:** 2026-10-29
**Reviewers:** Augusto

> Selo 🟡 PLANEJADO em todos os itens. Fonte: [`prd.md`](../prd.md), seções 4, 6, 8 e 9.

---

## 1. Resumo

🟡 Este componente faz o launcher temático assumir a tela inicial sozinho, toda vez que a TV liga e toda vez que alguém aperta Home, sem desativar o launcher do Google. Ele usa um serviço de acessibilidade do Android que percebe quando o launcher do Google aparece e abre a tela temática por cima. Também oferece o botão para desligar essa troca e voltar ao launcher original.

---

## 2. Contexto e Motivação

**Problema:**
🟡 No Google TV não há opção para escolher outro launcher como padrão. As soluções existentes "caem" e exigem reativar o tema sempre que a TV liga, o que gera chamados de suporte.

**Evidências:**
🟡 Resposta do usuário no `ideation.md` (alternativas limitadas por exigir ativação a cada vez que a TV liga) e métrica do PRD: tema estável por 30 dias, com 0 chamados.

**Por que agora:**
🟡 É o requisito central do produto e o maior risco técnico do PRD; precisa ser validado na TCL antes de investir nos outros componentes.

---

## 3. Goals (Objetivos)

- [ ] 🟡 G-01: Após ligar a TV, a tela temática aparece sem nenhuma ação manual.
- [ ] 🟡 G-02: Ao apertar Home em qualquer app, a tela temática aparece.
- [ ] 🟡 G-03: O responsável volta ao launcher do Google pela própria TV, sem ADB.
- [ ] 🟡 G-04: Quando o sistema desliga o serviço de acessibilidade, a TV avisa como reativar.

**Métricas de sucesso:**
| Métrica | Baseline atual | Target | Prazo |
|---------|---------------|--------|-------|
| 🟡 Dias seguidos com tema aparecendo sozinho após liga e desliga | não existe | 30 dias | 3 meses |
| 🟡 Tempo entre apertar Home e ver a tela temática | não existe | ≤ 1,5 s | MVP |
| 🟡 Tempo entre o fim do boot e ver a tela temática | não existe | ≤ 10 s | MVP |
| 🟡 Chamados de suporte por tema que deixou de aparecer | não existe | 0 por TV em 30 dias | 3 meses |

---

## 4. Non-Goals (Fora do Escopo)

- 🟡 NG-01: Desativar, desinstalar ou substituir o launcher do Google (`com.google.android.apps.tv.launcherx`).
- 🟡 NG-02: Root, alteração de firmware ou permissões de sistema.
- 🟡 NG-03: Impedir que alguém desligue o serviço de acessibilidade nas Configurações; o produto não é um bloqueio.
- 🟡 NG-04: Interceptar outros botões do controle além do retorno à tela inicial.

---

## 5. Usuários e Personas

**Usuário primário:** 🟡 Público da igreja ou da família, que liga a TV e aperta Home sem saber que existe troca de launcher.
**Usuário secundário:** 🟡 Revendedor e instalador ([`personas.md`](../personas.md)) e o responsável da igreja, que podem desligar a troca.

**Jornada atual (sem a feature):**
1. 🟡 A TV liga no launcher do Google.
2. 🟡 Com launchers de terceiros, alguém precisa abrir o launcher temático à mão a cada vez.

**Jornada futura (com a feature):**
1. 🟡 A TV liga, o launcher do Google aparece por um instante e a tela temática assume.
2. 🟡 Ao apertar Home dentro de um app, a tela temática volta.
3. 🟡 Para desligar, o responsável abre "Tema" e escolhe "Voltar ao launcher original do Google".

---

## 6. Requisitos Funcionais

### 6.1 Requisitos Principais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-01 | 🟡 O sistema deve oferecer um serviço de acessibilidade que observa a troca de janela em primeiro plano (`TYPE_WINDOW_STATE_CHANGED`). | Must | O serviço aparece em Configurações > Acessibilidade com o nome do launcher. |
| RF-02 | 🟡 O sistema deve abrir a tela temática quando o pacote em primeiro plano for o launcher do Google e a troca estiver ligada. | Must | Com a troca ligada, apertar Home mostra a tela temática em ≤ 1,5 s. |
| RF-03 | 🟡 O sistema deve abrir a tela temática ao final do boot (`BOOT_COMPLETED`) quando a troca estiver ligada. | Must | Após desligar da tomada e religar, a tela temática aparece em ≤ 10 s do fim do boot. |
| RF-04 | 🟡 O sistema deve guardar a preferência `takeover_enabled`, ligada por padrão após a ativação da licença. | Must | A preferência sobrevive a reinicialização. |
| RF-05 | 🟡 O usuário deve poder desligar a troca na tela "Tema", opção "Voltar ao launcher original do Google"; o sistema então grava `takeover_enabled = false` e abre o launcher do Google. | Must | Após a opção, apertar Home mostra o launcher do Google e a tela temática não volta sozinha. |
| RF-06 | 🟡 O usuário deve poder religar a troca abrindo o launcher temático pela lista de apps do Google e escolhendo "Usar o tema como tela inicial". | Must | Após a opção, apertar Home mostra a tela temática. |
| RF-07 | 🟡 O sistema deve detectar, ao abrir a tela temática, se o serviço de acessibilidade está desligado e mostrar um aviso com o caminho exato para religar e um botão que abre as Configurações de acessibilidade. | Must | Com o serviço desligado por ADB, a tela mostra o aviso e o botão. |
| RF-08 | 🟡 O sistema deve ignorar o launcher do Google por 5 s depois que o usuário escolher voltar ao launcher original, para não reabrir a tela temática nesse intervalo. | Must | Escolhendo voltar, a tela do Google fica visível sem piscar. |
| RF-09 | 🟡 O sistema não deve abrir a tela temática se a licença não for válida; nesse caso abre a tela de ativação do `license-activation`. | Must | Sem licença, apertar Home mostra a tela de ativação. |
| RF-10 | 🟡 O sistema deve registrar no diagnóstico a data e hora da última troca bem-sucedida e da última vez em que o serviço foi encontrado desligado. | Should | O diagnóstico mostra as duas datas. |

### 6.2 Fluxo Principal (Happy Path)

1. 🟡 A TV termina o boot.
2. 🟡 O sistema recebe `BOOT_COMPLETED` e verifica `takeover_enabled = true` e licença válida.
3. 🟡 O sistema abre a tela temática.
4. 🟡 O usuário abre um app e depois aperta Home.
5. 🟡 O launcher do Google entra em primeiro plano; o serviço de acessibilidade detecta e abre a tela temática.
6. 🟡 Resultado: a tela temática fica visível em ≤ 1,5 s.

### 6.3 Fluxos Alternativos

**Fluxo Alternativo A — Voltar ao launcher original:**
1. 🟡 O usuário abre "Tema" e escolhe "Voltar ao launcher original do Google".
2. 🟡 O sistema grava `takeover_enabled = false`, abre o launcher do Google e para de interceptar.

**Fluxo Alternativo B — Serviço desligado pelo sistema:**
1. 🟡 Após uma atualização, o Android desliga o serviço de acessibilidade.
2. 🟡 A TV liga no launcher do Google; se alguém abrir o launcher temático, o aviso de RF-07 aparece.

---

## 7. Requisitos Não-Funcionais

| ID | Requisito | Valor alvo | Observação |
|----|-----------|-----------|------------|
| RNF-01 | 🟡 Performance | Home até tela temática ≤ 1,5 s | O launcher do Google pode aparecer por até 1,5 s antes |
| RNF-02 | 🟡 Consumo | Serviço de acessibilidade com PSS ≤ 20 MB | Roda o tempo todo |
| RNF-03 | 🟡 Confiabilidade | 30 dias de liga e desliga sem intervenção | Teste na TCL de referência |
| RNF-04 | 🟡 Privacidade | O serviço só lê o nome do pacote em primeiro plano | Não lê textos, conteúdo de tela nem teclas |

---

## 8. Design e Interface

**Componentes afetados:** 🟡 serviço de acessibilidade, receptor de boot, tela "Tema" (opções), aviso de serviço desligado.

**Comportamento esperado:**
🟡 A tela "Tema" mostra o estado atual ("Tema como tela inicial: ligado/desligado"), o botão para alternar e a descrição do que cada estado faz. O aviso de serviço desligado aparece como faixa no topo da tela temática.

**Estados da UI:**
- 🟡 Estado vazio: não se aplica.
- 🟡 Estado de carregamento: não se aplica; a troca é imediata.
- 🟡 Estado de erro: faixa "O tema não vai abrir sozinho: ative o serviço em Configurações > Acessibilidade > Launcher Temático" com botão "Abrir Configurações".
- 🟡 Estado de sucesso: tela temática visível; a tela "Tema" mostra "ligado".

---

## 9. Modelo de Dados

**Entidades novas ou modificadas:**

```
TakeoverPrefs {
  takeover_enabled: bool           // padrão true após licença válida
  suppress_until_epoch_ms: long    // janela de 5 s de RF-08
  last_takeover_at: datetime?      // RF-10
  last_service_off_at: datetime?   // RF-10
}
```

**Migrações necessárias:** 🟡 Não.

---

## 10. Integrações e Dependências

| Dependência | Tipo | Impacto se indisponível |
|-------------|------|------------------------|
| 🟡 Serviço de acessibilidade do Android | Obrigatória | Sem ele, a tela temática não volta sozinha; mostra o aviso de RF-07 |
| 🟡 Evento `BOOT_COMPLETED` | Obrigatória | Sem ele, a tela temática aparece só no primeiro Home após o boot |
| 🟡 `device-provisioning` (liga o serviço por ADB na instalação) | Obrigatória | Sem ele, o serviço precisa ser ligado à mão nas Configurações |
| 🟡 `license-activation` | Obrigatória | Sem licença válida, abre a tela de ativação |
| 🟡 Pacote do launcher do Google `com.google.android.apps.tv.launcherx` | Obrigatória | Se o fabricante usar outro launcher, ele precisa ser cadastrado na lista de launchers observados |

---

## 11. Edge Cases e Tratamento de Erros

| Cenário | Trigger | Comportamento esperado |
|---------|---------|----------------------|
| 🟡 EC-01: Serviço desligado após atualização | Atualização do Android ou do Google TV | Aviso de RF-07 na próxima abertura e registro em `last_service_off_at`. |
| 🟡 EC-02: Loop de reabertura | Launcher do Google e tela temática se alternando | Janela de 5 s de RF-08 e no máximo 3 aberturas automáticas por 10 s; acima disso, pausa a interceptação por 30 s, registra o evento no diagnóstico e retoma sozinha ao fim da pausa. A pausa nunca altera `takeover_enabled`. |
| 🟡 EC-03: Outro launcher de fábrica | TV de outra marca sem `launcherx` | Lista configurável de pacotes de launcher observados; sem correspondência, nada é interceptado e o diagnóstico mostra "launcher do sistema não reconhecido". |
| 🟡 EC-04: Boot sem `BOOT_COMPLETED` entregue | Fabricante atrasa ou bloqueia o evento | A primeira detecção do launcher do Google pelo serviço abre a tela temática. |
| 🟡 EC-05: Falha ao abrir a tela temática | Erro ao iniciar a atividade | Registra o erro no diagnóstico e deixa o launcher do Google visível, sem tentar de novo por 10 s. |
| 🟡 EC-06: Usuário desliga o serviço nas Configurações | Ação manual | Respeita a escolha; o aviso de RF-07 aparece se o launcher temático for aberto. |

---

## 12. Segurança e Privacidade

- 🟡 **Autenticação:** não se aplica.
- 🟡 **Autorização:** o serviço de acessibilidade exige ativação explícita (por ADB na instalação ou pelo usuário nas Configurações).
- 🟡 **Dados sensíveis:** o serviço declara `packageNames` restrito ao(s) launcher(s) observado(s), não lê conteúdo de janelas (`canRetrieveWindowContent = false`) e não envia nada para fora da TV.
- 🟡 **Auditoria:** datas de RF-10 no diagnóstico.

---

## 13. Plano de Rollout

- 🟡 **Estratégia:** validar primeiro na TCL de referência com o teste de 30 dias (ou um teste acelerado de 50 ciclos de liga e desliga), depois em clientes piloto.
- 🟡 **Como reverter (rollback):** opção "Voltar ao launcher original do Google" na TV, ou por ADB: desligar o serviço de acessibilidade e desinstalar o app.
- 🟡 **Monitoramento pós-deploy:** conferir `last_takeover_at` e `last_service_off_at` no diagnóstico das TVs instaladas.

---

## 14. Open Questions

| # | Pergunta | Impacto | Dono | Prazo |
|---|---------|---------|------|-------|
| OQ-01 | 🟡 O Google TV da TCL desliga serviços de acessibilidade de apps instalados fora da Play Store após atualizações? Validar no teste de ciclos. | Alto | Augusto | Antes do primeiro cliente |
| OQ-02 | 🟡 A política da Google Play aceita esse uso de acessibilidade? Só importa se o app for publicado na loja. | Médio | Augusto | Antes de publicar na loja |

---

## 15. Decisões Tomadas (Decision Log)

| Decisão | Alternativas consideradas | Racional |
|---------|--------------------------|---------|
| 🟡 Serviço de acessibilidade observando o launcher do Google | Desativar o `launcherx` por ADB; declarar categoria HOME | Não desativa o launcher do Google, permite voltar sem ADB (requisito do PRD) |
| 🟡 Ligar o serviço por ADB na instalação | Pedir ao usuário para ligar nas Configurações | Cumpre a meta de instalação em menos de 10 minutos sem passos manuais |
| 🟡 Janela de 5 s após voltar ao original | Nenhuma | Evita que a tela temática reabra durante a saída |

---

## Apêndice

### Referências
- 🟡 [`prd.md`](../prd.md), [`ideation.md`](../ideation.md)
- 🟡 Specs relacionadas: [`launcher-home.md`](./launcher-home.md), [`device-provisioning.md`](./device-provisioning.md), [`license-activation.md`](./license-activation.md)

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
