# Investigation: Troca automática da tela inicial (home-takeover)

> Identificador: `003-home-takeover`
> Data: `2026-10-30`

## 1. Pesquisa de fundo

- 🟢 **Resolução de HOME na TCL (medido em 2026-10-30):** `cmd package query-activities -c HOME` lista `launcherx/.home.HomeActivity` com prioridade 2, `setupwraith/.RecoveryActivity` com 1 e `tv.settings/.system.FallbackHome` com -1000. Apps instalados pelo usuário não conseguem prioridade acima de 0, então declarar a categoria HOME não altera a tela inicial nem mostra seletor.
- 🟢 **Serviço de acessibilidade na TCL:** nenhum ligado hoje (`enabled_accessibility_services` = `null`, `accessibility_enabled` = 0). O shell do ADB grava essas configurações seguras.
- 🟢 **Abertura de atividade em segundo plano (Android 10+):** proibida em geral, com exceções; uma delas é o app ter um serviço vinculado pelo sistema do tipo `AccessibilityService`. Por isso a troca e o boot partem do serviço.
- 🟢 **`BOOT_COMPLETED`:** o receptor roda, mas não pode abrir atividade no Android 10+ sem uma exceção. Não é usado.
- 🟡 **Saída do modo de espera no Google TV:** em geral volta para a última tela; quando era a tela inicial, o launcher do Google reaparece e gera o evento de janela. A ser confirmado no teste de ciclos.

## 2. Alternativas avaliadas

| Tema | Alternativa | Por que foi descartada |
|------|-------------|------------------------|
| Assumir a tela inicial | Desativar o `launcherx` pelo ADB | Viola RN-01 e quebra a volta ao original sem ADB |
| Assumir a tela inicial | Declarar categoria HOME | Prioridade 2 do launcher do Google vence; sem efeito |
| Detectar a tela do Google | `getWindows()` | Exige acesso ao conteúdo das telas; fere o RNF de privacidade |
| Detectar a tela do Google | `UsageStatsManager` | Exige permissão especial concedida à mão e consulta periódica |
| Boot | Receptor de `BOOT_COMPLETED` | Não pode abrir atividade no Android 10+ |
| Desligar com proteção | `onLongClick` | Dispara em cerca de 0,5 s; o pedido é 3 s |
| Desligar com proteção | PIN | Preterido pelo usuário (risco de esquecer) |

## 3. Padrões aplicáveis

- **Política pura + adaptador fino:** `TakeoverPolicy` decide; o serviço só traduz eventos e executa a decisão.
- **Relógio monotônico para janelas de tempo:** `elapsedRealtime` não muda com ajuste de hora.
- **Disjuntor (circuit breaker) com retomada:** 3 aberturas em 10 s abrem o disjuntor por 30 s, que fecha sozinho.

## 4. Fontes externas

- 🟢 Restrições de abertura de atividade em segundo plano: https://developer.android.com/guide/components/activities/background-starts
- 🟢 Criar um serviço de acessibilidade: https://developer.android.com/guide/topics/ui/accessibility/service
- 🟢 Configuração `accessibility-service`: https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo
