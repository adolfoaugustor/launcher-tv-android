# Spec: Motor de temas (theme-engine)

**Versão:** 1.0
**Status:** Rascunho
**Autor:** reversa-spec-sdd
**Data:** 2026-10-29
**Reviewers:** Augusto

> Selo 🟡 PLANEJADO em todos os itens. Fonte: [`prd.md`](../prd.md), seções 4, 5 e 9.

---

## 1. Resumo

🟡 O motor de temas define o formato do **pacote de tema** (arquivo `.tematv`) e é a parte do launcher que lê, valida e aplica esse pacote. Ele permite trocar todo o visual da tela inicial (fundo, cores, logotipo, moldura dos ícones, fonte, mensagem e versículos) sem recompilar o app, o que viabiliza vender o mesmo launcher para igrejas e, depois, para outros segmentos como hamburguerias.

---

## 2. Contexto e Motivação

**Problema:**
🟡 Cada cliente quer a TV com a própria identidade. Se o visual estiver fixo no código, cada cliente exigiria um APK diferente, o que é inviável para um revendedor iniciante em programação Android.

**Evidências:**
🟡 O PRD exige personalização por cliente (white-label) e exige que o tema hamburgueria funcione no futuro sem recompilar o app (PRD, seção 9, último critério).

**Por que agora:**
🟡 Todos os outros componentes dependem do formato do pacote: o `launcher-home` desenha a partir dele, o `theme-builder` o gera e o `device-provisioning` o copia para a TV.

---

## 3. Goals (Objetivos)

- [ ] 🟡 G-01: Um único APK exibe visuais diferentes apenas trocando o arquivo `.tematv`.
- [ ] 🟡 G-02: Um pacote inválido nunca deixa a TV com tela em branco: o sistema cai no tema padrão embutido.
- [ ] 🟡 G-03: O formato suporta, sem mudança de código, uma moldura de ícone com outro desenho (Bíblia hoje, hambúrguer ou sacola depois).

**Métricas de sucesso:**
| Métrica | Baseline atual | Target | Prazo |
|---------|---------------|--------|-------|
| 🟡 Tempo para aplicar um pacote já copiado para a TV | não existe | ≤ 3 s | MVP |
| 🟡 Pacotes inválidos que resultam em tela em branco | não existe | 0% | MVP |
| 🟡 Temas distintos exibidos pelo mesmo APK nos testes | 0 | ≥ 2 | MVP |

---

## 4. Non-Goals (Fora do Escopo)

- 🟡 NG-01: Editor visual de temas dentro da TV. O pacote é gerado no notebook pelo `theme-builder`.
- 🟡 NG-02: Baixar temas da internet ou de um painel na nuvem.
- 🟡 NG-03: Animações, vídeos ou sons no tema. O MVP aceita apenas imagens estáticas e cores.
- 🟡 NG-04: Mais de um tema ativo ao mesmo tempo ou troca automática de tema por horário.
- 🟡 NG-05: Esconder ou bloquear apps (modo criança), retirado do escopo no PRD.

---

## 5. Usuários e Personas

**Usuário primário:** 🟡 Revendedor e instalador ([`personas.md`](../personas.md)), que gera o pacote no notebook e o aplica na TV.
**Usuário secundário:** 🟡 Público da igreja ou da família, que apenas vê o tema na tela inicial.

**Jornada atual (sem a feature):**
1. 🟡 O revendedor depende de launchers de terceiros com personalização limitada.
2. 🟡 Para cada identidade visual nova, seria preciso alterar e recompilar um app.

**Jornada futura (com a feature):**
1. 🟡 O revendedor gera `igreja-x.tematv` no notebook.
2. 🟡 O pacote é copiado para a TV e aplicado.
3. 🟡 A tela inicial passa a exibir a identidade da igreja.

---

## 6. Requisitos Funcionais

### 6.1 Requisitos Principais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-01 | 🟡 O sistema deve aceitar pacotes de tema no formato ZIP com extensão `.tematv`, contendo `theme.json` na raiz e os arquivos de imagem referenciados por ele. | Must | Um `.tematv` válido é carregado; um ZIP sem `theme.json` é recusado com o erro `THEME_MANIFEST_MISSING`. |
| RF-02 | 🟡 O sistema deve validar o `theme.json` contra o esquema da seção 9 (campos obrigatórios, tipos, cores no formato `#RRGGBB` e imagens existentes no pacote). | Must | Cada campo obrigatório ausente gera erro com o nome do campo; o pacote não é aplicado. |
| RF-03 | 🟡 O sistema deve recusar pacotes cujo `schema_version` seja maior que a versão suportada pelo app. | Must | Pacote com `schema_version: 2` num app que suporta 1 é recusado com `THEME_SCHEMA_UNSUPPORTED`. |
| RF-04 | 🟡 O sistema deve procurar pacotes na pasta de temas do app (`<armazenamento externo do app>/files/themes/`) e aplicar o pacote indicado em `active_theme.txt` na mesma pasta. | Must | Ao trocar o nome em `active_theme.txt` e reabrir o launcher, o tema exibido muda. |
| RF-05 | 🟡 O sistema deve aplicar o tema padrão embutido no APK quando não houver pacote ativo ou quando o pacote ativo for inválido. | Must | Removendo todos os pacotes, a tela inicial mostra o tema padrão, nunca uma tela vazia. |
| RF-06 | 🟡 O sistema deve expor ao `launcher-home` os valores do tema: cor de fundo, imagem de fundo, logotipo, cores de texto e destaque, moldura do ícone com a área do ícone, fonte, mensagem e lista de versículos. | Must | Teste de unidade lê um pacote de exemplo e confere cada valor exposto. |
| RF-07 | 🟡 O sistema deve suportar a moldura do ícone como imagem PNG com transparência, mais a área retangular (`icon_slot`) onde o ícone do app é desenhado e a área (`label_slot`) onde o nome do app é escrito. | Must | O mesmo app aparece dentro de uma Bíblia num pacote e dentro de um hambúrguer em outro, sem mudança de código. |
| RF-08 | 🟡 O sistema deve guardar o motivo da última recusa de pacote, para o `launcher-home` mostrar na tela de diagnóstico. | Should | Após aplicar um pacote inválido, a tela de diagnóstico mostra o código e o campo do erro. |
| RF-09 | 🟡 O sistema deve permitir fonte própria em arquivo TTF ou OTF dentro do pacote; sem fonte no pacote, usa a fonte padrão do sistema. | Could | Pacote com fonte própria exibe a mensagem nessa fonte. |

### 6.2 Fluxo Principal (Happy Path)

1. 🟡 O `device-provisioning` copia `igreja-x.tematv` para a pasta de temas e grava `igreja-x.tematv` em `active_theme.txt`.
2. 🟡 O launcher é aberto ou recebe o aviso de tema alterado.
3. 🟡 O sistema descompacta o pacote numa pasta interna de cache, valida o `theme.json` e confere as imagens.
4. 🟡 O sistema entrega os valores do tema ao `launcher-home`.
5. 🟡 Resultado: a tela inicial é redesenhada com a identidade da igreja em até 3 s.

### 6.3 Fluxos Alternativos

**Fluxo Alternativo A — Pacote inválido:**
1. 🟡 A validação do passo 3 falha.
2. 🟡 O sistema registra o código do erro, mantém o último tema válido aplicado (ou o tema padrão, se não houver) e não apaga o pacote inválido.

**Fluxo Alternativo B — Troca de tema:**
1. 🟡 Um novo pacote é copiado e `active_theme.txt` é atualizado.
2. 🟡 O sistema aplica o novo tema; o pacote anterior permanece na pasta para permitir voltar.

---

## 7. Requisitos Não-Funcionais

| ID | Requisito | Valor alvo | Observação |
|----|-----------|-----------|------------|
| RNF-01 | 🟡 Performance | Aplicar um pacote de até 15 MB em ≤ 3 s na TCL de referência | Inclui descompactar e validar |
| RNF-02 | 🟡 Memória | Imagens do tema decodificadas na resolução da tela (1920×1080 no máximo) | TV com 2 GB de RAM e cerca de 500 MB livres |
| RNF-03 | 🟡 Tamanho | Pacote `.tematv` com no máximo 15 MB | Acima disso, recusa com `THEME_TOO_LARGE` |
| RNF-04 | 🟡 Segurança | Descompactação recusa entradas com `..` ou caminho absoluto | Evita escrita fora da pasta de cache (zip slip) |

---

## 8. Design e Interface

**Componentes afetados:** 🟡 leitor de pacote, validador de esquema, cache do tema descompactado, provedor de tema consumido pelo `launcher-home`.

**Comportamento esperado:**
🟡 O motor não tem tela própria. Ele entrega ao `launcher-home` um objeto de tema já validado e informa quando o tema muda.

**Estados da UI:**
- 🟡 Estado vazio: sem pacote ativo, o tema padrão embutido é usado.
- 🟡 Estado de carregamento: enquanto descompacta, o `launcher-home` mantém o tema anterior na tela.
- 🟡 Estado de erro: o tema anterior permanece e o erro fica disponível na tela de diagnóstico.
- 🟡 Estado de sucesso: o novo tema é exibido.

---

## 9. Modelo de Dados

**Entidades novas ou modificadas:**

```
ThemeManifest (theme.json) {
  schema_version: int              // obrigatório; MVP suporta 1
  theme_id: string                 // obrigatório; kebab-case, ex.: "igreja-x-biblia"
  display_name: string             // obrigatório; nome do cliente, ex.: "Igreja Batista X"
  colors: {
    background: "#RRGGBB"          // obrigatório
    text: "#RRGGBB"                // obrigatório
    accent: "#RRGGBB"              // obrigatório; foco e destaque
  }
  background_image: string?        // opcional; caminho no pacote (PNG, JPG ou WEBP)
  logo_image: string               // obrigatório; PNG com transparência
  message: string                  // obrigatório; até 120 caracteres
  font_file: string?               // opcional; TTF ou OTF
  icon_frame: {
    image: string                  // obrigatório; PNG com transparência, ex.: frame-biblia.png
    icon_slot: {x, y, w, h}        // obrigatório; frações de 0 a 1 da imagem da moldura
    label_slot: {x, y, w, h}       // obrigatório; área do nome do app
  }
  verses: [                        // obrigatório; de 1 a 366 itens
    { reference: string,           // ex.: "Mt 19:14"
      text: string,                // texto do versículo
      version: string }            // sigla, ex.: "ACF"
  ]
  verses_attribution: string       // obrigatório; ex.: "Almeida Corrigida Fiel, SBTB"
}
```

**Migrações necessárias:** 🟡 Não. É a versão 1 do formato.

---

## 10. Integrações e Dependências

| Dependência | Tipo | Impacto se indisponível |
|-------------|------|------------------------|
| 🟡 `theme-builder` (gera o pacote) | Obrigatória | Sem pacote, o sistema usa o tema padrão embutido |
| 🟡 `device-provisioning` (copia o pacote e grava `active_theme.txt`) | Obrigatória | Sem cópia, o sistema usa o último tema válido ou o padrão |
| 🟡 `launcher-home` (consome o tema) | Obrigatória | O motor não tem saída visual própria |
| 🟡 Armazenamento externo do app no Android | Obrigatória | Falha de leitura gera `THEME_IO_ERROR` e fallback para o tema padrão |

---

## 11. Edge Cases e Tratamento de Erros

| Cenário | Trigger | Comportamento esperado |
|---------|---------|----------------------|
| 🟡 EC-01: Manifesto ausente | ZIP sem `theme.json` | Recusa com `THEME_MANIFEST_MISSING`; mantém o tema anterior. |
| 🟡 EC-02: Imagem referenciada ausente | `logo_image` aponta para arquivo inexistente | Recusa com `THEME_ASSET_MISSING` e o nome do arquivo. |
| 🟡 EC-03: Cor em formato inválido | `colors.text = "azul"` | Recusa com `THEME_FIELD_INVALID: colors.text`. |
| 🟡 EC-04: Pacote corrompido | ZIP truncado na cópia | Recusa com `THEME_ARCHIVE_CORRUPT`; mantém o tema anterior. |
| 🟡 EC-05: Pacote grande demais | Arquivo acima de 15 MB | Recusa com `THEME_TOO_LARGE`. |
| 🟡 EC-06: Zip slip | Entrada com `../` no caminho | Recusa com `THEME_ARCHIVE_UNSAFE`, sem gravar nada fora do cache. |
| 🟡 EC-07: Falha de leitura do armazenamento | Erro de IO ou pasta indisponível | Registra `THEME_IO_ERROR` e aplica o tema padrão embutido. |
| 🟡 EC-08: `active_theme.txt` aponta para arquivo inexistente | Pacote apagado manualmente | Aplica o tema padrão e registra `THEME_ACTIVE_NOT_FOUND`. |
| 🟡 EC-09: Lista de versículos vazia | `verses: []` | Recusa com `THEME_FIELD_INVALID: verses`. |

---

## 12. Segurança e Privacidade

- 🟡 **Autenticação:** não se aplica; o motor só lê arquivos locais do próprio app.
- 🟡 **Autorização:** só arquivos na pasta de temas do app são lidos, e só por ele (e pelo ADB do instalador).
- 🟡 **Dados sensíveis:** o pacote não contém dados pessoais nem a chave da Bíblia API.
- 🟡 **Auditoria:** o sistema guarda apenas o último erro de pacote, sem histórico.

---

## 13. Plano de Rollout

- 🟡 **Estratégia:** big bang no MVP; o formato nasce na versão 1 com `schema_version`.
- 🟡 **Como reverter (rollback):** gravar em `active_theme.txt` o pacote anterior, que fica guardado na pasta de temas, ou apagar o arquivo para voltar ao tema padrão.
- 🟡 **Monitoramento pós-deploy:** conferir na tela de diagnóstico o tema ativo e o último erro em cada TV instalada.

---

## 14. Open Questions

| # | Pergunta | Impacto | Dono | Prazo |
|---|---------|---------|------|-------|
| OQ-01 | 🟡 Qual tamanho e proporção padrão da moldura de ícone para a grade de apps na TV 1080p? | Médio | Augusto | Antes do `launcher-home` |
| OQ-02 | 🟡 A mensagem da igreja precisa de mais de 120 caracteres ou de várias linhas? | Baixo | Augusto | Antes do `theme-builder` |

---

## 15. Decisões Tomadas (Decision Log)

| Decisão | Alternativas consideradas | Racional |
|---------|--------------------------|---------|
| 🟡 Pacote ZIP `.tematv` com `theme.json` | Tema compilado dentro do APK; APK de tema separado | Troca o visual sem recompilar e sem o revendedor programar |
| 🟡 Pasta de temas no armazenamento externo do próprio app | Pasta Download pública | O app lê sem pedir permissão de armazenamento no Android 11 e o ADB consegue gravar nela |
| 🟡 Versículos embutidos no pacote | Buscar na API a partir da TV | A chave da Bíblia API não pode ficar no app (termos da BIBLIAAPI) e a TV funciona sem internet |
| 🟡 Moldura como imagem com áreas `icon_slot` e `label_slot` | Formas desenhadas por código | Qualquer formato (Bíblia, hambúrguer, sacola) vira só uma imagem nova |

---

## Apêndice

### Referências
- 🟡 [`prd.md`](../prd.md), [`personas.md`](../personas.md), [`ideation.md`](../ideation.md)
- 🟡 Specs relacionadas: [`launcher-home.md`](./launcher-home.md), [`theme-builder.md`](./theme-builder.md), [`device-provisioning.md`](./device-provisioning.md)

### Histórico de Revisões
| Versão | Data | Autor | Mudanças |
|--------|------|-------|---------|
| 1.0 | 2026-10-29 | reversa-spec-sdd | Criação inicial |

---

## Relatório de avaliação

🟡 **Score total:** 100/100 (⭐ Excelente — Pronta para implementação)  
🟡 **Iterações:** 1  
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
