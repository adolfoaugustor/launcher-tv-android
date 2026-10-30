# Requirements: Motor de temas (theme-engine)

> Identificador: `001-theme-engine`
> Data: `2026-10-29`
> Pasta da extração reversa: `_reversa_sdd/` (projeto novo: as fontes são o PRD e as specs SDD do `/reversa-new`)
> Branch: `feature/001-theme-engine` (feature 1 de 6)
> Confidência: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA / DÚVIDA

## 1. Resumo executivo

Esta feature entrega o motor de temas do launcher: o formato do pacote de tema (arquivo `.tematv`) e a parte do app Android TV que encontra, valida e aplica esse pacote, com um tema padrão embutido para quando não houver pacote válido. É a base de todas as outras features: a tela inicial (feature 2) desenha a partir dele e a ferramenta do notebook (feature 5) gera pacotes nesse formato. Resolve a necessidade de cada cliente ter a própria identidade visual sem recompilar o app.

## 2. Contexto a partir do legado

Não há sistema legado: o projeto é novo. O contexto vem dos artefatos gerados pelo `/reversa-new`, todos com selo 🟡 PLANEJADO.

| Fonte | Trecho relevante | Confidência |
|-------|------------------|-------------|
| `_reversa_sdd/prd.md#4-escopo-in` | Motor de temas: o visual da tela inicial vem de um pacote de tema, sem recompilar o app | 🟡 |
| `_reversa_sdd/prd.md#9-critérios-de-aceite-alto-nível` | Um segundo pacote (por exemplo, hamburgueria) muda o visual sem recompilar o app | 🟡 |
| `_reversa_sdd/prd.md#6-restrições` | TV de referência TCL, Android 11, 2 GB de RAM; chave da Bíblia API não pode ir para o app | 🟡 |
| `_reversa_sdd/sdd/theme-engine.md#6-requisitos-funcionais` | RF-01 a RF-09 do motor: formato ZIP, validação, pasta de temas, tema padrão, moldura com áreas | 🟡 |
| `_reversa_sdd/sdd/theme-engine.md#9-modelo-de-dados` | Esquema do `theme.json` versão 1 | 🟡 |
| `_reversa_sdd/sdd/theme-engine.md#11-edge-cases-e-tratamento-de-erros` | EC-01 a EC-09, com códigos de erro | 🟡 |
| `_reversa_sdd/sdd/launcher-home.md#6-requisitos-funcionais` | Consumidor do tema na feature 2 (RF-02, RF-06, RF-07, RF-12) | 🟡 |
| `_reversa_sdd/sdd/device-provisioning.md#6-requisitos-funcionais` | Quem grava o pacote e o `active_theme.txt` na TV (feature 6) | 🟡 |

## 3. Personas e cenários de uso

| Persona | Objetivo | Cenário-chave |
|---------|----------|---------------|
| Revendedor e instalador (`_reversa_sdd/personas.md#persona-1-revendedor-e-instalador`) | Entregar a TV com a identidade do cliente sem programar | Copia `igreja-x.tematv` para a TV, indica-o como tema ativo e a tela muda para a identidade da igreja |
| Público da igreja ou da família | Ver a TV com a cara da igreja | Liga a TV e vê logotipo, cores e mensagem; nunca vê uma tela vazia, mesmo com pacote defeituoso |

## 4. Regras de negócio novas ou alteradas

1. **RN-01:** O visual da tela inicial é definido exclusivamente pelo tema ativo; trocar de cliente ou de segmento é trocar o pacote, nunca o app. 🟡
   - Origem: `_reversa_sdd/prd.md#4-escopo-in`
   - Tipo: nova
2. **RN-02:** Um pacote inválido nunca é aplicado e nunca deixa a TV sem tema: vale o último tema válido aplicado ou, na falta dele, o tema padrão embutido. 🟡
   - Origem: `_reversa_sdd/sdd/theme-engine.md#3-goals-objetivos` (G-02)
   - Tipo: nova
3. **RN-03:** O pacote de tema não contém a chave da Bíblia API nem dados pessoais; os versículos chegam já com o texto pronto. 🟡
   - Origem: `_reversa_sdd/prd.md#6-restrições`
   - Tipo: nova
4. **RN-04:** O app só aceita pacotes cuja versão de formato (`schema_version`) ele conhece; nesta feature, apenas a versão 1. 🟡
   - Origem: `_reversa_sdd/sdd/theme-engine.md#6-requisitos-funcionais` (RF-03)
   - Tipo: nova
5. **RN-05:** Pacotes anteriores permanecem guardados na TV ao trocar de tema, para permitir voltar. 🟡
   - Origem: `_reversa_sdd/sdd/theme-engine.md#6-3-fluxos-alternativos` (Fluxo B)
   - Tipo: nova

## 5. Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de aceite | Confidência |
|----|-----------|------------|--------------------|-------------|
| RF-01 | O app deve aceitar pacotes de tema compactados no formato ZIP, com extensão `.tematv` e o arquivo `theme.json` na raiz. | Must | Um pacote de exemplo válido é carregado; um ZIP sem `theme.json` é recusado com `THEME_MANIFEST_MISSING`. | 🟡 |
| RF-02 | O app deve validar o `theme.json` contra o esquema versão 1 (`_reversa_sdd/sdd/theme-engine.md#9-modelo-de-dados`): campos obrigatórios, tipos, cores `#RRGGBB`, mensagem de até 120 caracteres (exibida em no máximo 2 linhas na feature 2), de 1 a 366 versículos e imagens existentes no pacote. | Must | Para cada regra violada, o erro informa o código e o campo; o pacote não é aplicado. | 🟡 |
| RF-03 | O app deve recusar pacotes com `schema_version` diferente de 1. | Must | Pacote com `schema_version: 2` é recusado com `THEME_SCHEMA_UNSUPPORTED`. | 🟡 |
| RF-04 | O app deve procurar pacotes na pasta `themes/` do armazenamento externo do próprio app e aplicar o pacote cujo nome está em `active_theme.txt`, na mesma pasta. | Must | Trocar o nome em `active_theme.txt` e pedir nova leitura faz o tema exposto mudar. | 🟡 |
| RF-05 | O app deve trazer embutido um tema padrão completo, válido pelo mesmo esquema, e usá-lo quando não houver pacote ativo ou quando nenhum pacote válido tiver sido aplicado antes. O tema padrão é o tema infantil cristão com moldura de Bíblia, sem logotipo de igreja (usa o logotipo do produto) e com a mensagem "Deus te abençoe!". | Must | Sem nenhum arquivo na pasta `themes/`, o tema exposto é o padrão, com moldura de Bíblia e a mensagem "Deus te abençoe!". | 🟢 |
| RF-06 | O app deve manter o último tema válido aplicado quando o novo pacote ativo for recusado. | Must | Aplicar o tema A, depois apontar para um pacote inválido B: o tema exposto continua A e o erro de B fica registrado. | 🟡 |
| RF-07 | O app deve expor aos demais componentes um tema já validado com: cores de fundo, texto e destaque; imagem de fundo opcional; logotipo; moldura do ícone com as áreas do ícone (`icon_slot`) e do nome (`label_slot`); fonte opcional; mensagem; lista de versículos com referência, texto e sigla; e atribuição da tradução. | Must | Teste automatizado lê o pacote de exemplo e confere cada valor exposto. | 🟡 |
| RF-08 | O app deve avisar os componentes interessados quando o tema exposto mudar. | Must | Um componente inscrito recebe o novo tema depois de uma troca bem-sucedida, e não recebe nada depois de uma troca recusada. | 🟡 |
| RF-09 | O app deve registrar o código, o campo e a data e hora da última recusa de pacote, para exibição futura na tela de diagnóstico (feature 2). | Should | Após aplicar um pacote inválido, o registro de erro contém código, campo e data e hora. | 🟡 |
| RF-10 | O app deve aceitar uma fonte própria (arquivo TrueType `.ttf` ou OpenType `.otf`) no pacote; sem fonte, usa a do sistema. | Could | Pacote com fonte expõe a fonte; pacote sem fonte expõe "fonte do sistema". | 🟡 |
| RF-11 | O projeto deve incluir dois pacotes de exemplo válidos que usem o mesmo app: um com a moldura de Bíblia e outro com a moldura de hambúrguer, ambas em retrato com 260×340 px de referência (5 apps por linha na tela 1080p). | Must | Os dois pacotes passam na validação e expõem molduras diferentes, com as áreas `icon_slot` e `label_slot` definidas. | 🟢 |
| RF-12 | O app deve aceitar molduras de qualquer tamanho em pixels e expor também a proporção da imagem; a grade da feature 2 usa como referência 260×340 px por app, 5 por linha e 2 linhas visíveis em 1080p. | Must | Uma moldura de 520×680 px é aceita e expõe a proporção 13:17. | 🟢 |

## 6. Requisitos Não Funcionais

| Tipo | Requisito | Evidência ou justificativa | Confidência |
|------|-----------|----------------------------|-------------|
| Desempenho | Aplicar um pacote de até 15 MB em no máximo 3 s na TCL de referência, incluindo descompactar e validar | `_reversa_sdd/sdd/theme-engine.md#7-requisitos-não-funcionais` (RNF-01) | 🟡 |
| Memória | Imagens do tema mantidas na memória em no máximo 1920×1080 | TV com 2 GB de RAM e cerca de 500 MB livres (`_reversa_sdd/prd.md#6-restrições`) | 🟡 |
| Tamanho | Pacotes acima de 15 MB recusados com `THEME_TOO_LARGE` | `_reversa_sdd/sdd/theme-engine.md#7-requisitos-não-funcionais` (RNF-03) | 🟡 |
| Segurança | Entradas do ZIP com `..` ou caminho absoluto recusadas com `THEME_ARCHIVE_UNSAFE`, sem gravar nada fora da pasta de trabalho do app | `_reversa_sdd/sdd/theme-engine.md#7-requisitos-não-funcionais` (RNF-04) | 🟡 |
| Compatibilidade | Funcionar no Android TV 9 (nível de API 28) ou superior | `_reversa_sdd/sdd/launcher-home.md#7-requisitos-não-funcionais` (RNF-03) | 🟡 |
| Testabilidade | Toda regra de validação e todo código de erro cobertos por teste automatizado que roda sem a TV | Feature 1 não tem tela; a verificação é por testes | 🟡 |
| Observabilidade | Cada recusa de pacote registrada no log do sistema com código e campo | Suporte remoto pelo revendedor | 🟡 |

## 7. Critérios de Aceitação

```gherkin
Cenário: Aplicar um pacote válido
  Dado o pacote "igreja-exemplo.tematv" na pasta themes/
  E "igreja-exemplo.tematv" gravado em active_theme.txt
  Quando o motor de temas lê o tema ativo
  Então o tema exposto tem o logotipo, as cores e a mensagem desse pacote
  E os componentes inscritos são avisados da troca

Cenário: Sem pacote ativo
  Dado a pasta themes/ vazia
  Quando o motor de temas lê o tema ativo
  Então o tema exposto é o tema padrão embutido

Cenário: Mesmo app, molduras diferentes
  Dado os dois pacotes de exemplo do projeto
  Quando cada um é aplicado em sequência
  Então a moldura exposta muda entre a Bíblia e o hambúrguer sem mudar o app

Cenário: Fonte própria opcional
  Dado um pacote com font_file apontando para um arquivo .ttf do pacote
  Quando o motor de temas aplica o pacote
  Então o tema exposto usa essa fonte
  E um pacote sem font_file expõe a fonte do sistema

Cenário: Pacote sem manifesto
  Dado um ZIP sem theme.json indicado em active_theme.txt
  Quando o motor de temas lê o tema ativo
  Então o pacote é recusado com THEME_MANIFEST_MISSING
  E o tema exposto continua o último válido

Cenário: Campo inválido
  Dado um pacote com colors.text igual a "azul"
  Quando o motor de temas valida o pacote
  Então o pacote é recusado com THEME_FIELD_INVALID e o campo colors.text

Cenário: Versão de formato desconhecida
  Dado um pacote com schema_version igual a 2
  Quando o motor de temas valida o pacote
  Então o pacote é recusado com THEME_SCHEMA_UNSUPPORTED

Cenário: Pacote malicioso
  Dado um pacote com uma entrada chamada "../../fora.txt"
  Quando o motor de temas descompacta o pacote
  Então o pacote é recusado com THEME_ARCHIVE_UNSAFE
  E nenhum arquivo é gravado fora da pasta de trabalho do app

Cenário: Pacote grande demais
  Dado um pacote de 16 MB
  Quando o motor de temas lê o tema ativo
  Então o pacote é recusado com THEME_TOO_LARGE

Cenário: Tema ativo apagado
  Dado active_theme.txt apontando para um arquivo que não existe
  Quando o motor de temas lê o tema ativo
  Então o tema exposto é o padrão e o erro THEME_ACTIVE_NOT_FOUND é registrado

Cenário: Falha de leitura
  Dado a pasta themes/ indisponível para leitura
  Quando o motor de temas lê o tema ativo
  Então o erro THEME_IO_ERROR é registrado e o tema exposto é o padrão
```

## 8. Prioridade MoSCoW

| Item | MoSCoW | Justificativa |
|------|--------|---------------|
| RF-01 a RF-08 e RF-12 | Must | Sem eles a feature 2 não tem o que desenhar |
| RF-11 | Must | Prova o objetivo central: mesmo app, visuais diferentes |
| RF-09 | Should | Necessário para a tela de diagnóstico da feature 2, não bloqueia o motor |
| RF-10 | Could | A fonte do sistema atende o MVP |
| RNF de segurança (ZIP) | Must | Pacote vem de fora do app |
| RNF de desempenho e memória | Should | Medição real só com a tela da feature 2 na TCL |

## 9. Esclarecimentos

### Sessão 2026-10-29

- **Q:** Como deve ser o tema padrão embutido, que aparece quando não há pacote de tema válido?
  **R:** Tema bíblico sem igreja: o tema infantil cristão com moldura de Bíblia, sem logotipo de igreja e com mensagem genérica ("Deus te abençoe!"). Aplicado em RF-05.
- **Q:** Qual o tamanho da moldura de cada app na grade da TV 1080p?
  **R:** 5 por linha, em retrato, com cerca de 260×340 px e 2 linhas visíveis. Aplicado em RF-11 e RF-12.
- **Q:** Como deve ser a mensagem da igreja na tela?
  **R:** Até 120 caracteres, quebrando em no máximo 2 linhas. Aplicado em RF-02.
- **Q:** Qual forma deve ter a moldura do segundo pacote de exemplo?
  **R:** Hambúrguer, adiantando o futuro tema de hamburgueria. Aplicado em RF-11 e no cenário "Mesmo app, molduras diferentes".

## 10. Lacunas

- Nenhuma lacuna aberta. As três dúvidas da versão inicial foram resolvidas na sessão de 2026-10-29 (seção 9).

## 11. Histórico de alterações

| Data | Alteração | Autor |
|------|-----------|-------|
| 2026-10-29 | Versão inicial gerada por `/reversa-requirements` | reversa |
| 2026-10-29 | Sessão de esclarecimentos: tema padrão, tamanho da moldura, mensagem e segundo exemplo | reversa-clarify |
