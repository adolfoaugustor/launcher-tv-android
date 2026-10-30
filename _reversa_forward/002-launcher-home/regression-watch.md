# Regression watch: Tela inicial do launcher (launcher-home)

> Identificador: `002-launcher-home`
> Data: `2026-10-30`
> Feature greenfield: não há regras 🟢 para vigiar ainda. Os itens abaixo ficam em "Observações", sem peso de regressão, até uma extração `/reversa` sobre o código novo confirmá-los como 🟢.

## Watch principal

| ID | Origem (arquivo, seção) | Regra esperada após mudança | Tipo de verificação | Sinal de violação |
|----|--------------------------|-----------------------------|---------------------|-------------------|

Nenhum item nesta rodada.

## Observações

| ID | Origem (arquivo, seção) | Regra esperada | Tipo de verificação | Sinal de violação |
|----|--------------------------|----------------|---------------------|-------------------|
| W001 | `requirements.md` RF-01, RN-06 | A grade lista os apps com lançador de TV, sem o próprio launcher e sem os 7 utilitários da TCL; a TV ao vivo aparece | presença | Utilitário na grade, TV ao vivo ausente ou grade vazia no Android 11+ (falta de `<queries>`) |
| W002 | `requirements.md` RF-02, RF-05 | Cada app aparece dentro da moldura do tema, com banner (ou ícone) no `icon_slot` sem distorção e nome no `label_slot`; foco com borda na cor `accent` e zoom de 10% | presença | Banner distorcido, fora do painel ou foco invisível |
| W003 | `requirements.md` RF-03 | Ordem alfabética ignorando acentos e maiúsculas | presença | "Ótica" depois de "YouTube" ou ordem sensível a maiúsculas |
| W004 | `requirements.md` RF-07, RN-03 | Versículo do dia = `floorMod(epochDay, quantidade)`, com referência, sigla e atribuição | presença | Versículo mudando dentro do mesmo dia ou sem atribuição |
| W005 | `requirements.md` RF-14, RN-04 | Voltar na grade não sai da tela inicial | presença | Voltar leva ao launcher do Google |
| W006 | `requirements.md` RF-10, RF-11 | Botões "Configurações" e "Tema" no cabeçalho, alcançados com o direcional para cima | presença | Botões fora do cabeçalho ou inalcançáveis |
| W007 | `requirements.md` RF-13 | Ao reabrir, o foco vai para o último app aberto (persistido em `launcher_prefs`) | presença | Foco sempre no primeiro app após reiniciar |
| W008 | `requirements.md` §6, desempenho e memória | Abertura a frio ≤ 2 s e PSS ≤ 100 MB na TCL; pacote de 15 MB aplicado em ≤ 3 s (medido: ~1,5 s, 80,7 MB, 570 ms) | presença | Medidas acima das metas |
| W009 | `actions.md`, rodada 1 (defeito 1) | Na primeira carga, o foco vai para a grade, não para um botão do cabeçalho | presença | Foco inicial em "Configurações" |

## Histórico de re-extrações

Nenhuma re-extração registrada.

## Arquivadas

Nenhum item arquivado.
