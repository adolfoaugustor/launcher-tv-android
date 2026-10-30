# Regression watch: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`
> Feature greenfield: não há regras 🟢 para vigiar ainda. Os itens abaixo ficam em "Observações", sem peso de regressão, até uma extração `/reversa` sobre o código novo confirmá-los como 🟢.

## Watch principal

| ID | Origem (arquivo, seção) | Regra esperada após mudança | Tipo de verificação | Sinal de violação |
|----|--------------------------|-----------------------------|---------------------|-------------------|

Nenhum item nesta rodada.

## Observações

| ID | Origem (arquivo, seção) | Regra esperada | Tipo de verificação | Sinal de violação |
|----|--------------------------|----------------|---------------------|-------------------|
| W001 | `requirements.md` RN-02 | Serviços de acessibilidade de outros apps continuam ligados; o do launcher aparece uma vez | presença | Serviço de outro app some ou o do launcher duplica |
| W002 | `requirements.md` RN-04 | Todo comando ADB da instalação usa `-s <ip>:5555` | presença | Comando indo para outro aparelho conectado |
| W003 | `requirements.md` RN-05 | Registro de instalação sem senha nem segredos | ausência | Senha ou segredo em `~/tematv/instalacoes/` |
| W004 | `requirements.md` RN-06, `roadmap.md` D-05 | APK de produção assinado com a chave definitiva; sem ela só o release falha | presença | Release assinado com outra chave ou debug sem compilar |
| W005 | `interfaces/estado-do-app.md` | `app_status.json` legível e somente leitura pela importação | presença | Conferências da instalação sem dados |
| W006 | `actions.md` notas (T023) | Senha com quebra de linha aceita pela importação | presença | Etapa de ativação falhando na TV |
| W007 | `requirements.md` RF-05 | Mesma versão e assinatura: instalação pulada | presença | Reinstalação a cada visita |

## Histórico de re-extrações

Nenhuma re-extração registrada.

## Arquivadas

Nenhum item arquivado.
