# PRD: Launcher temático para Android TV

> Selo 🟡 PLANEJADO. Documento gerado a partir de ideation + personas.

**Versão:** 1.0
**Data:** 2026-10-29T17:58:00-03:00
**Autor:** reversa-drafter
**Status:** rascunho

---

## 1. Problema

🟡 Clientes comerciais, começando por igrejas, querem que a TV tenha a identidade deles (logo, cores e mensagem) desde o momento em que é ligada, mudando o tema da tela inicial (launcher), sem restringir nem esconder apps. As soluções existentes de launchers e personalizações de terceiros exigem reativar o tema sempre que a TV liga e não há produto pronto para o segmento comercial. Com isso, o revendedor não consegue entregar ao cliente uma TV personalizada que se mantenha estável sozinha.

### Quem sente
- 🟡 **Revendedor e instalador:** sente ao instalar nas TVs dos clientes e ao receber chamados quando o tema "cai" depois que a TV é desligada e ligada.
- 🟡 **Igrejas:** nas TVs próprias (sala do ministério infantil, recepção, salão), ligadas por líderes ou professores durante cultos e aulas.
- 🟡 **Famílias de membros:** nas TVs de casa, quando a igreja oferece o tema para as famílias.

---

## 2. Personas-alvo

🟡 Referência completa em [`personas.md`](./personas.md). Resumo:

- **Revendedor e instalador**: 🟡 vai à igreja com o notebook, é iniciante em programação Android e quer uma linha de produto para vender a vários segmentos. Dor principal: as soluções atuais exigem reativar o tema a cada vez que a TV liga e não existe nada pronto para o segmento comercial.

---

## 3. Métricas de sucesso

🟡 Métricas definidas no `ideation.md`, avaliadas 3 meses após o início.

| Métrica | Unidade | Alvo | Prazo |
|---|---|---|---|
| 🟡 Tempo para instalar e personalizar uma TV | minutos por TV | 🟡 menos de 10 | 🟡 3 meses |
| 🟡 Estabilidade do tema após ciclos de liga e desliga | dias sem intervenção | 🟡 30 | 🟡 3 meses |
| 🟡 Chamados de suporte por tema que deixou de aparecer | chamados por TV em 30 dias | 🟡 0 | 🟡 3 meses |

---

## 4. Escopo (in)

🟡 Itens derivados de ideation, personas, jornada e respostas de cobertura.

- 🟡 **App launcher para Android TV / Google TV**, que lista os apps instalados e abre o app escolhido, validado primeiro na TV de teste TCL com Android 11 e 2 GB de RAM.
- 🟡 **Motor de temas**: o visual da tela inicial (fundo, cores, logo, moldura dos ícones dos apps, fonte e mensagem) vem de um pacote de tema, sem recompilar o app.
- 🟡 **Tema inicial infantil cristão**: apps exibidos dentro de uma moldura em formato de Bíblia, fundo infantil e mensagem da igreja.
- 🟡 **Personalização por cliente (white-label)**: logo, cores e mensagem de cada igreja dentro do pacote de tema.
- 🟡 **Versículo do dia**: exibido na tela inicial a partir de uma lista de versículos embutida no pacote de tema, sem depender de internet na TV.
- 🟡 **Ferramenta de preparo de tema no notebook**: monta o pacote de tema do cliente (logo, cores, mensagem) e busca os versículos na Bíblia API usando a chave do revendedor, que nunca vai para a TV.
- 🟡 **Assumir a tela inicial automaticamente a cada vez que a TV liga**, sem intervenção, usando um serviço de acessibilidade e sem desativar o launcher do Google.
- 🟡 **Desativar e voltar ao launcher original do Google** pelo próprio launcher, sem precisar de ADB.
- 🟡 **Instalação via pendrive e via ADB**.
- 🟡 **Ativação por licença**: o usuário não excluiu "licença e cobrança" do MVP. O mecanismo está [INDEFINIDO, validar com usuário].

---

## 5. Não-objetivos (out)

- 🟡 **Tema hamburgueria** (apps em formato de hambúrguer ou sacola de pedidos): fica para depois do MVP, mas o motor de temas precisa suportá-lo sem mudar o app.
- 🟡 **Painel na nuvem** para gerenciar e trocar o tema das TVs remotamente.
- 🟡 **Editor visual de temas**: no MVP, o tema é preparado no notebook pela ferramenta de preparo, não montado clicando.
- 🟡 **Modo criança / esconder apps**: o objetivo é mudar o tema, não restringir o uso (esclarecido no `ideation.md`).

---

## 6. Restrições

| Tipo | Descrição |
|---|---|
| 🟡 Técnica | 🟡 Plataforma Android TV / Google TV. TV de referência: TCL, Android 11, 2 GB de RAM, que já opera com cerca de 500 a 650 MB livres; o launcher precisa ser leve. |
| 🟡 Técnica | 🟡 Não desativar nem desinstalar o launcher do Google (`com.google.android.apps.tv.launcherx`); a volta ao original depende disso. |
| 🟡 Técnica | 🟡 A chave da Bíblia API não pode ficar embutida no app da TV: os termos da BIBLIAAPI dizem que a chave é pessoal e não deve ser exposta em código de cliente. |
| 🟡 Técnica | 🟡 O revendedor é iniciante em programação Android: preparar e instalar um tema não pode exigir programar nem compilar. |
| 🟡 Técnica | 🟡 Sistema operacional do notebook onde roda a ferramenta de preparo: [INDEFINIDO, validar com usuário]. |
| 🟡 Prazo | 🟡 [INDEFINIDO, validar com usuário]. As métricas são avaliadas em 3 meses. |
| 🟡 Compliance | 🟡 Textos bíblicos: cada tradução tem detentor de direitos (por exemplo, Biblica para NVI; SBB para ARA, ARC e NAA; SBTB para ACF). A BIBLIAAPI exige atribuição e manda confirmar a licença de cada sigla junto ao detentor para uso comercial. |
| 🟡 Compliance | 🟡 LGPD e dados pessoais: [INDEFINIDO, validar com usuário]. |
| 🟡 Orçamento | 🟡 [INDEFINIDO, validar com usuário]. |

---

## 7. Dependências externas

- 🟡 **BIBLIAAPI** (`https://bibliaapi.com.br/api/v2`): versículos por referência (`/versions/{version}/books/{book}/chapters/{chapter}/verses/{verse}`) e aleatórios (`/versions/{version}/random`). A autenticação é por header (`Authorization: Bearer <chave>` ou `X-API-Key`). Não existe endpoint de "versículo do dia". O plano Free tem limite mensal, cujo valor não está publicado, e os preços dos planos pagos também não estão publicados. Só é usada pela ferramenta de preparo no notebook.
- 🟡 **Detentores de direitos das traduções bíblicas** (Biblica, SBB, SBTB), para autorizar o uso comercial do texto escolhido.
- 🟡 **Serviço de acessibilidade do Android / Google TV**, para assumir a tela inicial sem desativar o launcher do Google.
- 🟡 **ADB** (depuração por rede ou USB) e **pendrive com instalador de APK** na TV, como caminhos de instalação.

---

## 8. Riscos

| Risco | Impacto | Probabilidade | Mitigação proposta |
|---|---|---|---|
| 🟡 O Google TV desliga o serviço de acessibilidade após atualização do sistema ou bloqueia a troca da tela inicial, e o tema "cai" | 🟡 alto | 🟡 média | 🟡 Testar ciclos de liga e desliga e atualizações na TV de referência; detectar quando o serviço estiver desligado e avisar na tela como reativar. |
| 🟡 A instalação exige modo desenvolvedor ou "fontes desconhecidas", contrariando a premissa de instalar sem modo desenvolvedor | 🟡 alto | 🟡 alta | 🟡 Validar cedo os dois caminhos (pendrive e ADB) na TV de referência e documentar o passo a passo mínimo por marca. |
| 🟡 Não funcionar em TVs de outras marcas além da TCL | 🟡 alto | 🟡 média | 🟡 Testar em pelo menos uma segunda marca antes de vender fora da TCL. |
| 🟡 As igrejas não pagarem pelo produto | 🟡 alto | 🟡 [INDEFINIDO] | 🟡 Validar com um ou dois clientes piloto antes de investir nos temas seguintes. |
| 🟡 Uso comercial do texto bíblico sem licença da tradução | 🟡 alto | 🟡 média | 🟡 Usar tradução com uso comercial autorizado, mantendo a atribuição exigida. |
| 🟡 Limite mensal do plano Free da Bíblia API ser atingido no preparo dos temas | 🟡 baixo | 🟡 baixa | 🟡 Os versículos são buscados uma vez por tema no notebook e embutidos no pacote, não a cada boot da TV. |
| 🟡 Launcher pesado em TV com 2 GB de RAM causar lentidão | 🟡 médio | 🟡 média | 🟡 Medir o consumo de memória na TV de referência; usar imagens do tema em resolução adequada à tela. |

---

## 9. Critérios de aceite (alto nível)

- 🟡 **Dado** um pacote de tema preparado no notebook com logo, cores e mensagem da igreja, **Quando** o revendedor instala o launcher e aplica o tema numa TV, **Então** a TV mostra a tela inicial com a identidade da igreja em menos de 10 minutos no total.
- 🟡 **Dado** o launcher ativo numa TV, **Quando** a TV é desligada e ligada, **Então** o tema aparece sozinho, sem nenhuma ação manual, por 30 dias seguidos.
- 🟡 **Dado** o tema infantil cristão aplicado, **Quando** a tela inicial é exibida, **Então** cada app aparece dentro da moldura em formato de Bíblia e o versículo do dia é exibido, mesmo sem internet na TV.
- 🟡 **Dado** o launcher ativo, **Quando** o responsável escolhe desativar o tema, **Então** a TV volta ao launcher original do Google, sem ADB.
- 🟡 **Dado** um segundo pacote de tema (por exemplo, hamburgueria), **Quando** ele for aplicado no futuro, **Então** o visual muda sem recompilar o app.

---

## Pendências de cobertura

- 🟡 Mecanismo de ativação por licença e cobrança (seção 4).
- 🟡 Sistema operacional do notebook da ferramenta de preparo (seção 6).
- 🟡 Prazo do projeto (seção 6).
- 🟡 LGPD e dados pessoais (seção 6).
- 🟡 Orçamento (seção 6).
- 🟡 Probabilidade do risco "igrejas não pagarem" (seção 8).
- 🟡 Tradução bíblica a usar e confirmação da licença comercial com o detentor (seções 6 e 8).

---

Gerado por reversa-drafter em 2026-10-29T17:58:00-03:00
Fontes: ideation.md, personas.md, perguntas de cobertura (versículos via Bíblia API; não-objetivos), documentação pública da BIBLIAAPI
