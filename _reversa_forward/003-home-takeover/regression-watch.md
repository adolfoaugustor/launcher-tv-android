# Regression watch: Troca automática da tela inicial (home-takeover)

> Identificador: `003-home-takeover`
> Data: `2026-10-30`
> Feature greenfield: não há regras 🟢 para vigiar ainda. Os itens abaixo ficam em "Observações", sem peso de regressão, até uma extração `/reversa` sobre o código novo confirmá-los como 🟢.

## Watch principal

| ID | Origem (arquivo, seção) | Regra esperada após mudança | Tipo de verificação | Sinal de violação |
|----|--------------------------|-----------------------------|---------------------|-------------------|

Nenhum item nesta rodada.

## Observações

| ID | Origem (arquivo, seção) | Regra esperada | Tipo de verificação | Sinal de violação |
|----|--------------------------|----------------|---------------------|-------------------|
| W001 | `requirements.md` RF-01, RNF de privacidade | O serviço recebe só a troca de janela dos launchers do sistema, sem conteúdo de tela e sem teclas | presença | `canRetrieveWindowContent` ou filtro de teclas ligados |
| W002 | `requirements.md` RF-02, RF-14 | Home num app leva à tela temática em até 1,5 s, com a capa cobrindo a tela do Google (medido < 1 s na TCL) | presença | Tela do Google visível após o Home ou atraso de ~4 s |
| W003 | `requirements.md` RF-03, RF-04 | Após reiniciar ou sair do modo de espera, a tela temática volta sozinha | presença | Falhas no teste de ciclos |
| W004 | `requirements.md` RN-01 | O launcher do Google nunca é desativado nem desinstalado | ausência | `pm disable`/`uninstall` do `launcherx` em qualquer parte do produto |
| W005 | `requirements.md` RF-06, RF-08, RF-13 | Desligar exige segurar OK 3 s; o "soltar" não religa; depois, Home fica no Google | presença | Toque curto desliga, ou a troca religa sozinha |
| W006 | `requirements.md` RF-09, RN-04 | Trava contra loop pausa 30 s e retoma sem mudar `takeover_enabled` | presença | Troca desligada por pausa ou loop sem pausa |
| W007 | `requirements.md` RF-10 | Serviço desligado com a troca ligada mostra a faixa discreta e o passo a passo em "Tema" | presença | Sem aviso com o serviço desligado |
| W008 | `roadmap.md` D-17 | Na TCL, o app precisa de `APP_AUTO_START` liberado para o serviço ser vinculado | presença | `forbid bind service` no log da TV |

## Histórico de re-extrações

Nenhuma re-extração registrada.

## Arquivadas

Nenhum item arquivado.
