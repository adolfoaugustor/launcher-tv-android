# Ideation, Launcher temático para Android TV

> Selo 🟡 PLANEJADO em todos os itens, sujeito a validação.

## Brief original
Launcher próprio para Android TV / Google TV com motor de temas. Primeiro tema: infantil cristão (Bíblia, Cristo, palavra de Deus), apps exibidos em formato de Bíblia, fundo infantil, versículo do dia, modo criança opcional. Temas futuros: hamburgueria (apps em formato de hambúrguer ou sacola de pedidos). Deve ter opção de desativar e voltar ao launcher original do Google (via serviço de acessibilidade, sem desativar o launcherx). Distribuição para várias TVs. TV de teste: TCL Android TV 11, 2 GB RAM, IP 192.168.1.2.

## Problema
🟡 Clientes comerciais, a começar por igrejas, querem que a TV tenha a cara deles, mudando o tema da tela inicial (o launcher). A intenção não é restringir nem esconder apps. O problema aparece em dois cenários:
- 🟡 Nas TVs da própria igreja (por exemplo, sala do ministério infantil, recepção, salão), ligadas por líderes ou professores durante cultos e aulas.
- 🟡 Nas TVs das casas dos membros, com a igreja oferecendo o tema para as famílias.

## Valor entregue
🟡 A TV passa a ter a identidade da igreja, com o logo, as cores e a mensagem, no momento em que é ligada.
🟡 Consequência: o tema precisa ser personalizável por cliente (white-label), não um tema fixo único.

## Alternativas existentes
🟡 Existem launchers e personalizações de terceiros, mas são limitados:
- 🟡 Exigem ativar o tema de novo sempre que a TV é ligada (não se mantêm sozinhos).
- 🟡 Não há nada pronto para o segmento comercial.

## Público-alvo (bruto)
🟡 Usuário principal: o revendedor e instalador (Augusto), que instala e personaliza o launcher nas TVs dos clientes.
🟡 Público final, que vê o tema: igrejas (líderes, professores, crianças) e famílias de membros.

## Métricas de sucesso
- 🟡 Tempo de instalação e personalização por TV: menos de 10 minutos.
- 🟡 Estabilidade: o tema se mantém sozinho por 30 dias de liga e desliga, com 0 chamados de suporte.

## Premissas a validar
1. 🟡 As igrejas pagam por isso.
2. 🟡 Dá para instalar sem a TV estar em modo desenvolvedor.
3. 🟡 Funciona em TVs de outras marcas além da TCL.

## Notas
- 🟡 O "modo criança" (esconder apps) citado no brief deixa de ser foco: o usuário esclareceu que o objetivo é mudar o tema, não restringir o uso.
- 🟡 Requisito central derivado da resposta 3: o launcher precisa assumir a tela inicial automaticamente a cada vez que a TV liga, sem intervenção.
- 🟡 Risco técnico levantado pelo Ideator, não listado pelo usuário entre as premissas: o Google TV pode não permitir que um launcher de terceiros assuma a tela inicial de forma permanente sem desativar o launcher do Google, e pode desligar serviços de acessibilidade após atualizações do sistema. Isso afeta diretamente a métrica de estabilidade de 30 dias.
- 🟡 Temas futuros previstos no brief: hamburgueria (apps em formato de hambúrguer ou sacola de pedidos), indicando que o motor de temas deve servir a outros segmentos comerciais além de igrejas.
- 🟡 Requisito do brief mantido: opção de desativar o tema e voltar ao launcher original do Google.

---
Gerado por reversa-ideator em 2026-10-29T18:07:47-03:00
Fonte: newproject-brief.md
