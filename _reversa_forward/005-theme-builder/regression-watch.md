# Regression watch: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`
> Feature greenfield: não há regras 🟢 para vigiar ainda. Os itens abaixo ficam em "Observações", sem peso de regressão, até uma extração `/reversa` sobre o código novo confirmá-los como 🟢.

## Watch principal

| ID | Origem (arquivo, seção) | Regra esperada após mudança | Tipo de verificação | Sinal de violação |
|----|--------------------------|-----------------------------|---------------------|-------------------|

Nenhum item nesta rodada.

## Observações

| ID | Origem (arquivo, seção) | Regra esperada | Tipo de verificação | Sinal de violação |
|----|--------------------------|----------------|---------------------|-------------------|
| W001 | `requirements.md` RN-01 | Todo pacote salvo em `~/tematv/temas/` passou pelo `ThemePackageLoader` | presença | Pacote recusado na TV gerado pela ferramenta |
| W002 | `requirements.md` RN-02, RN-07 | Igreja: lista inicial igual ao tema padrão do app, versão `NVI` e atribuição da NVI | presença | Terceira cópia da lista ou versão diferente de NVI |
| W003 | `requirements.md` RN-03, `roadmap.md` D-03 | Servidor só em `127.0.0.1` e recusa `Host`, `Origin` e `Content-Type` estranhos | presença | Página acessível pela rede ou escrita aceita de outro site |
| W004 | `requirements.md` RN-04 | Segredo de ativação fora da página, dos logs e dos pacotes | ausência | Segredo em resposta da API, log do terminal ou `.tematv` |
| W005 | `requirements.md` RF-12 | A senha da página é igual à do `activation-tool` e aos vetores fixos | presença | Senha diferente para o mesmo código |
| W006 | `roadmap.md` D-11 | Ferramentas do notebook compilam sem o segredo; o app não | presença | `gerar-segredo` impossível de compilar num notebook novo |
| W007 | `actions.md` notas (T021) | Comando `tematv` com socket IPv4 (`preferIPv4Stack`) | presença | Página não abre no navegador do Windows pelo WSL2 |

## Histórico de re-extrações

Nenhuma re-extração registrada.

## Arquivadas

Nenhum item arquivado.
