# Spec: Ativação por licença (license-activation)

**Versão:** 1.0
**Status:** Rascunho
**Autor:** reversa-spec-sdd
**Data:** 2026-10-29
**Reviewers:** Augusto

> Selo 🟡 PLANEJADO em todos os itens. Fonte: [`prd.md`](../prd.md), seção 4 (ativação por licença); resposta da Fase 1 (chave offline por TV).

---

## 1. Resumo

🟡 Cada TV só exibe o tema depois de receber uma licença emitida pelo revendedor para aquela TV. A licença é um arquivo assinado digitalmente no notebook e validado na TV sem internet. Sem licença válida, o launcher mostra uma tela de ativação com o código do aparelho e não assume a tela inicial.

---

## 2. Contexto e Motivação

**Problema:**
🟡 Sem ativação, o APK e um pacote de tema podem ser copiados para qualquer TV, e o revendedor perde o controle de quantas TVs usa cada cliente.

**Evidências:**
🟡 O usuário manteve "licença e cobrança" dentro do MVP e escolheu validação offline por TV (resposta da Fase 1).

**Por que agora:**
🟡 A licença é uma etapa da instalação (`device-provisioning`) e condição para a troca de tela inicial (`home-takeover`).

---

## 3. Goals (Objetivos)

- [ ] 🟡 G-01: Uma licença só funciona na TV para a qual foi emitida.
- [ ] 🟡 G-02: A validação na TV não depende de internet nem de servidor.
- [ ] 🟡 G-03: Só quem tem a chave privada do revendedor emite licenças válidas.
- [ ] 🟡 G-04: A licença pode ter data de validade opcional.

**Métricas de sucesso:**
| Métrica | Baseline atual | Target | Prazo |
|---------|---------------|--------|-------|
| 🟡 Licenças copiadas para outra TV que são aceitas | não existe | 0% | MVP |
| 🟡 Tempo de validação da licença ao abrir o launcher | não existe | ≤ 200 ms | MVP |
| 🟡 Tempo para emitir e gravar a licença na instalação | não existe | ≤ 10 s | MVP |

---

## 4. Non-Goals (Fora do Escopo)

- 🟡 NG-01: Servidor de licenças, bloqueio remoto ou renovação automática pela internet.
- 🟡 NG-02: Cobrança, emissão de boletos, cartões ou integração com meios de pagamento.
- 🟡 NG-03: Proteção contra engenharia reversa ou modificação do APK (anti-tamper).
- 🟡 NG-04: Licenças por quantidade de TVs (lote); cada licença vale para uma TV.

---

## 5. Usuários e Personas

**Usuário primário:** 🟡 Revendedor e instalador ([`personas.md`](../personas.md)), que emite as licenças no notebook.
**Usuário secundário:** 🟡 Público da TV, que só vê a tela de ativação se a licença faltar ou vencer.

**Jornada atual (sem a feature):**
1. 🟡 Não há controle de quais TVs usam o produto.

**Jornada futura (com a feature):**
1. 🟡 Na instalação, a ferramenta lê o código do aparelho e emite a licença daquela TV.
2. 🟡 A TV valida a licença e libera o tema.
3. 🟡 Se a licença vencer, a TV mostra a tela de ativação com o código para o revendedor renovar.

---

## 6. Requisitos Funcionais

### 6.1 Requisitos Principais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-01 | 🟡 O sistema (app na TV) deve calcular o código do aparelho como os 16 primeiros caracteres hexadecimais de `SHA-256(ANDROID_ID + applicationId)`, no formato `XXXX-XXXX-XXXX-XXXX`. | Must | O código é o mesmo após reiniciar e após reinstalar o app com a mesma assinatura. |
| RF-02 | 🟡 O sistema (app na TV) deve gravar o código do aparelho em `device_code.txt` na pasta externa do app e exibi-lo na tela de ativação e no diagnóstico. | Must | `adb shell cat .../files/device_code.txt` retorna o mesmo código exibido na tela. |
| RF-03 | 🟡 O sistema (ferramenta do notebook) deve gerar um par de chaves Ed25519 na primeira execução, guardando a chave privada em `~/.config/tematv/license-private.pem` com permissão `600` e exportando a chave pública para embutir no APK. | Must | O arquivo existe com permissão `600`; gerar de novo é recusado se o arquivo já existir. |
| RF-04 | 🟡 O sistema (ferramenta do notebook) deve emitir `license.key` contendo `device_code`, `customer`, `issued_at`, `expires_at` (opcional) e a assinatura Ed25519 do conteúdo canônico. | Must | A licença emitida é aceita pela TV dona do código. |
| RF-05 | 🟡 O sistema (app na TV) deve validar a assinatura com a chave pública embutida e conferir que `device_code` é igual ao código do aparelho. | Must | Uma licença de outra TV é recusada com `LICENSE_DEVICE_MISMATCH`. |
| RF-06 | 🟡 O sistema (app na TV) deve recusar a licença quando `expires_at` existir e a data da TV for posterior a ela. | Must | Licença vencida é recusada com `LICENSE_EXPIRED`. |
| RF-07 | 🟡 O sistema (app na TV) deve mostrar, sem licença válida, a tela de ativação com o código do aparelho, o motivo da recusa e a instrução "Peça a ativação ao seu revendedor informando este código". | Must | Sem `license.key`, a tela de ativação aparece no lugar da grade. |
| RF-08 | 🟡 O sistema (app na TV) deve informar ao `home-takeover` se a licença é válida; sem licença válida, a troca de tela inicial não acontece. | Must | Sem licença, apertar Home mostra a tela de ativação, nunca a grade temática (mesmo comportamento de RF-09 do `home-takeover`). |
| RF-09 | 🟡 O usuário deve poder emitir uma licença pela ferramenta do notebook digitando o código do aparelho à mão, para entrega por pendrive. | Should | A licença gerada assim é aceita pela TV. |
| RF-10 | 🟡 O sistema (ferramenta do notebook) deve registrar cada licença emitida em `~/tematv/licencas.csv` (código do aparelho, cliente, data de emissão, validade). | Should | Cada emissão acrescenta uma linha ao arquivo. |

### 6.2 Fluxo Principal (Happy Path)

1. 🟡 O `device-provisioning` instala o app e o abre uma vez.
2. 🟡 O app calcula o código do aparelho e grava `device_code.txt`.
3. 🟡 A ferramenta do notebook lê o código, emite `license.key` assinada para o cliente e a grava na pasta do app.
4. 🟡 O app valida a assinatura, o código e a validade.
5. 🟡 Resultado: o launcher libera a tela temática e a troca de tela inicial.

### 6.3 Fluxos Alternativos

**Fluxo Alternativo A — Ativação por pendrive:**
1. 🟡 O revendedor recebe o código exibido na tela de ativação (foto ou ligação).
2. 🟡 Emite a licença pelo código digitado (RF-09) e a entrega no kit de pendrive.

**Fluxo Alternativo B — Licença vencida:**
1. 🟡 A data da TV passa de `expires_at`.
2. 🟡 O launcher mostra a tela de ativação com o motivo "Licença vencida em DD/MM/AAAA".

---

## 7. Requisitos Não-Funcionais

| ID | Requisito | Valor alvo | Observação |
|----|-----------|-----------|------------|
| RNF-01 | 🟡 Performance | Validação ≤ 200 ms na TCL | Feita ao abrir o launcher |
| RNF-02 | 🟡 Segurança | Chave privada só no notebook, com permissão `600`; nunca no APK, no pacote de tema ou em logs | Ed25519 |
| RNF-03 | 🟡 Funcionamento offline | 100% da validação sem internet | Escolha do usuário |
| RNF-04 | 🟡 Backup | A ferramenta lembra de copiar a chave privada para um local seguro na primeira execução | Sem a chave, não há como emitir licenças novas |

---

## 8. Design e Interface

**Componentes afetados:** 🟡 tela de ativação do launcher, item "Licença" no diagnóstico, etapa de licença do `device-provisioning`, tela "Licenças" da ferramenta do notebook.

**Comportamento esperado:**
🟡 A tela de ativação usa as cores do tema padrão, mostra o código do aparelho em fonte grande e o motivo da recusa. A tela "Licenças" da ferramenta tem o campo do código, o cliente e a validade opcional.

**Estados da UI:**
- 🟡 Estado vazio: sem `license.key`, motivo "Este aparelho ainda não foi ativado".
- 🟡 Estado de carregamento: não se aplica; a validação é imediata.
- 🟡 Estado de erro: motivo específico (assinatura inválida, outro aparelho, vencida, arquivo corrompido).
- 🟡 Estado de sucesso: o diagnóstico mostra "Licença válida para <cliente>" e a validade.

---

## 9. Modelo de Dados

**Entidades novas ou modificadas:**

```
License (license.key, JSON UTF-8) {
  format_version: int        // 1
  device_code: string        // "XXXX-XXXX-XXXX-XXXX"
  customer: string           // nome do cliente, ex.: "Igreja Batista X"
  issued_at: date            // AAAA-MM-DD
  expires_at: date?          // ausente = sem validade
  signature: string          // Base64 da assinatura Ed25519 sobre os campos acima, em JSON canônico (chaves ordenadas, sem espaços)
}
```

**Migrações necessárias:** 🟡 Não.

---

## 10. Integrações e Dependências

| Dependência | Tipo | Impacto se indisponível |
|-------------|------|------------------------|
| 🟡 `ANDROID_ID` do Android | Obrigatória | Sem ele, o código não é calculado e a tela de ativação mostra `LICENSE_DEVICE_ID_UNAVAILABLE` |
| 🟡 Chave privada no notebook | Obrigatória para emitir | Sem ela, não há emissão (EC-05) |
| 🟡 `device-provisioning` | Obrigatória | Lê o código e grava a licença na TV |
| 🟡 `home-takeover` e `launcher-home` | Obrigatória | Consomem o estado da licença |
| 🟡 Biblioteca de assinatura Ed25519 no app | Obrigatória | O suporte nativo a Ed25519 não é garantido nas APIs 28 a 30; o app embute uma biblioteca para verificar a assinatura. ⚠️ ABERTO: escolher a biblioteca na fase de plano. |

---

## 11. Edge Cases e Tratamento de Erros

| Cenário | Trigger | Comportamento esperado |
|---------|---------|----------------------|
| 🟡 EC-01: Restauração de fábrica | `ANDROID_ID` muda | O código muda e a licença antiga é recusada com `LICENSE_DEVICE_MISMATCH`; o revendedor emite outra. |
| 🟡 EC-02: Assinatura inválida | Arquivo editado à mão | Recusa com `LICENSE_SIGNATURE_INVALID`. |
| 🟡 EC-03: Arquivo corrompido | JSON inválido ou campo ausente | Recusa com `LICENSE_FORMAT_INVALID`. |
| 🟡 EC-04: Data da TV errada | Relógio sem sincronizar antes de `issued_at` | Aceita a licença sem validade; com validade, compara só com `expires_at`. |
| 🟡 EC-05: Chave privada perdida | Arquivo apagado no notebook | Não gera chave nova automaticamente; mostra "Restaure a chave privada do backup"; licenças já emitidas continuam valendo. |
| 🟡 EC-06: APK assinado com outra chave de assinatura | Build de teste reinstalado | O `ANDROID_ID` muda; a licença é recusada e precisa ser reemitida. |
| 🟡 EC-07: Falha de leitura do armazenamento | Erro de IO ao ler `license.key` ou pasta do app indisponível | Trata como sem licença válida com `LICENSE_IO_ERROR`, mostra a tela de ativação e tenta ler de novo na próxima abertura do launcher. |
| 🟡 EC-08: `ANDROID_ID` indisponível | O sistema retorna valor vazio | Mostra `LICENSE_DEVICE_ID_UNAVAILABLE` na tela de ativação e não grava `device_code.txt`. |

---

## 12. Segurança e Privacidade

- 🟡 **Autenticação:** a autenticidade da licença vem da assinatura Ed25519.
- 🟡 **Autorização:** só o dono da chave privada emite licenças.
- 🟡 **Dados sensíveis:** o código do aparelho é um hash e não revela o `ANDROID_ID`; a licença guarda só o nome do cliente.
- 🟡 **Auditoria:** `licencas.csv` no notebook.

---

## 13. Plano de Rollout

- 🟡 **Estratégia:** licenças sem validade para clientes piloto; validade opcional para os pagantes.
- 🟡 **Como reverter (rollback):** apagar `license.key` volta a TV à tela de ativação; uma build de diagnóstico pode ignorar a licença durante os testes na TCL de referência.
- 🟡 **Monitoramento pós-deploy:** conferir o estado da licença no diagnóstico de cada TV e a lista em `licencas.csv`.

---

## 14. Open Questions

| # | Pergunta | Impacto | Dono | Prazo |
|---|---------|---------|------|-------|
| OQ-01 | 🟡 As licenças terão validade (mensalidade, anuidade) ou serão perpétuas? | Médio | Augusto | Antes do primeiro cliente pagante |
| OQ-02 | 🟡 Uma licença vencida deve bloquear o tema na hora ou dar um prazo de carência (por exemplo, 7 dias com aviso)? | Médio | Augusto | Antes do primeiro cliente pagante |

---

## 15. Decisões Tomadas (Decision Log)

| Decisão | Alternativas consideradas | Racional |
|---------|--------------------------|---------|
| 🟡 Licença offline assinada com Ed25519 | Servidor online; código curto com segredo no APK | Escolha do usuário (sem servidor); assinatura assimétrica impede emitir licenças a partir do APK |
| 🟡 Código do aparelho derivado do `ANDROID_ID` | Número de série; MAC | Disponível sem permissões especiais no Android 8+ |
| 🟡 Sem anti-tamper no MVP | Ofuscação e verificação de integridade | Custo alto para o MVP; risco aceito |

---

## Apêndice

### Referências
- 🟡 [`prd.md`](../prd.md)
- 🟡 Specs relacionadas: [`device-provisioning.md`](./device-provisioning.md), [`home-takeover.md`](./home-takeover.md), [`launcher-home.md`](./launcher-home.md)

### Histórico de Revisões
| Versão | Data | Autor | Mudanças |
|--------|------|-------|---------|
| 1.0 | 2026-10-29 | reversa-spec-sdd | Criação inicial |

---

## Relatório de avaliação

🟡 **Score total:** 100/100 (⭐ Excelente — Pronta para implementação)  
🟡 **Iterações:** 3 (98 → 100 → 100, após correções de revisão)  
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
