# Data delta: Troca automática da tela inicial (home-takeover)

> Identificador: `003-home-takeover`
> Data: `2026-10-30`
> Base: `_reversa_sdd/sdd/home-takeover.md#9-modelo-de-dados`

## 1. Entidades novas

### 1.1 `takeover_prefs` (`SharedPreferences`) 🟢

| Chave | Tipo | Conteúdo |
|-------|------|----------|
| `takeover_enabled` | booleano, padrão `true` | Se a troca da tela inicial está ligada (RF-05) |
| `suppress_until_elapsed_ms` | inteiro longo | Fim da janela de 5 s após desligar (RF-08), em `elapsedRealtime` |
| `paused_until_elapsed_ms` | inteiro longo | Fim da pausa de 30 s contra loop (RF-09), em `elapsedRealtime` |
| `last_takeover_at` | texto ISO 8601 ou ausente | Última troca bem-sucedida (RF-11) |
| `last_service_off_at` | texto ISO 8601 ou ausente | Última vez em que o serviço foi encontrado desligado (RF-10, RF-11) |
| `last_loop_pause_at` | texto ISO 8601 ou ausente | Última pausa contra loop (RF-09, RF-11) |
| `last_open_failure_at` | texto ISO 8601 ou ausente | Última falha ao abrir a tela temática (EC-05) |

Os valores em `elapsedRealtime` perdem o sentido depois de reiniciar a TV; a política trata qualquer valor maior que o instante atual mais a duração da janela como expirado.

### 1.2 Histórico de aberturas (em memória, no serviço) 🟢

Lista dos instantes (`elapsedRealtime`) das aberturas automáticas dos últimos 10 s, usada pela trava contra loop.

## 2. Campos removidos

n/a.

## 3. Migrações necessárias

n/a.
