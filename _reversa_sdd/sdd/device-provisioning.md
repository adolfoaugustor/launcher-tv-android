# Spec: Instalação na TV (device-provisioning)

**Versão:** 1.0
**Status:** Rascunho
**Autor:** reversa-spec-sdd
**Data:** 2026-10-29
**Reviewers:** Augusto

> Selo 🟡 PLANEJADO em todos os itens. Fonte: [`prd.md`](../prd.md), seções 3, 4, 7 e 8; respostas da Fase 1 (ADB pelo notebook como caminho principal).

---

## 1. Resumo

🟡 Aba "Instalar na TV" da ferramenta do notebook. Com o IP da TV, ela conecta por ADB, instala o APK do launcher, copia o pacote de tema, emite e grava a licença, liga o serviço de acessibilidade e confere que a tela temática aparece, tudo num clique e com uma lista de verificação na tela. O pendrive fica como caminho alternativo.

---

## 2. Contexto e Motivação

**Problema:**
🟡 Instalar app, tema, licença e serviço de acessibilidade à mão, comando por comando, é lento e sujeito a erro para um revendedor iniciante, e a meta é menos de 10 minutos por TV.

**Evidências:**
🟡 Métrica do PRD: instalar e personalizar uma TV em menos de 10 minutos. Jornada da persona, passos 2 a 6. Os comandos ADB usados hoje na TCL de referência funcionaram pela rede (porta 5555).

**Por que agora:**
🟡 É o caminho para entregar o produto nas TVs e para medir as métricas de instalação e estabilidade.

---

## 3. Goals (Objetivos)

- [ ] 🟡 G-01: Do IP digitado até a tela temática na TV em ≤ 10 min, incluindo a autorização na TV.
- [ ] 🟡 G-02: Nenhum comando digitado à mão pelo revendedor.
- [ ] 🟡 G-03: Configurações existentes da TV preservadas (outros serviços de acessibilidade, apps instalados).
- [ ] 🟡 G-04: Registro de cada instalação no notebook.

**Métricas de sucesso:**
| Métrica | Baseline atual | Target | Prazo |
|---------|---------------|--------|-------|
| 🟡 Tempo do clique em "Instalar" até a verificação final | manual, sem medição | ≤ 5 min | MVP |
| 🟡 Tempo total por TV (incluindo autorização e conferência) | não existe | < 10 min | 3 meses |
| 🟡 Instalações que exigem comando manual | não existe | 0% | MVP |

---

## 4. Non-Goals (Fora do Escopo)

- 🟡 NG-01: Instalar várias TVs ao mesmo tempo; o MVP instala uma TV por vez.
- 🟡 NG-02: Ativar o modo desenvolvedor ou a depuração na TV pelo notebook; isso é feito à mão na TV, com instruções na tela.
- 🟡 NG-03: Instalar pela loja Google Play.
- 🟡 NG-04: Atualização remota de app ou tema sem estar na mesma rede da TV.
- 🟡 NG-05: Remover apps do cliente ou alterar outras configurações da TV.

---

## 5. Usuários e Personas

**Usuário primário:** 🟡 Revendedor e instalador ([`personas.md`](../personas.md)), na igreja, com o notebook na mesma rede da TV.
**Usuário secundário:** 🟡 Nenhum.

**Jornada atual (sem a feature):**
1. 🟡 Instalar o `adb`, conectar, instalar o APK, copiar arquivos e ajustar configurações por comandos digitados.

**Jornada futura (com a feature):**
1. 🟡 Abrir a aba "Instalar na TV", escolher o cliente e digitar o IP da TV.
2. 🟡 Aceitar o aviso de depuração na TV quando a página pedir.
3. 🟡 Acompanhar a lista de verificação até "Tela temática confirmada".

---

## 6. Requisitos Funcionais

### 6.1 Requisitos Principais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-01 | 🟡 O sistema deve verificar, ao abrir a aba, se o `adb` está instalado no notebook e, se não estiver, mostrar o comando de instalação `sudo apt install -y adb`. | Must | Sem `adb`, a aba mostra o comando e desabilita "Instalar". |
| RF-02 | 🟡 O usuário deve poder escolher o projeto do cliente e digitar o IP da TV; o sistema valida o formato IPv4 (4 números de 0 a 255). | Must | `1922.168.1.2` é recusado com "IP inválido". |
| RF-15 | 🟡 O sistema deve, antes de qualquer etapa, exibir o aviso "Ligue a TV e deixe-a na tela inicial" e testar se a TV responde na rede (ping e porta 5555, até 5 s), mostrando "TV ligada e encontrada em <ip>" ou "TV desligada ou fora da rede"; o botão "Instalar" só é liberado com a TV encontrada. | Must | Com a TV desligada, a página mostra "TV desligada ou fora da rede" e "Instalar" fica desabilitado; ao ligar a TV e clicar em "Verificar de novo", a mensagem muda para "TV ligada e encontrada". |
| RF-03 | 🟡 O sistema deve testar a porta 5555 da TV e, se fechada, mostrar o passo a passo para ligar a depuração por rede nas Opções do desenvolvedor. | Must | Com a porta fechada, aparece o passo a passo e a instalação não começa. |
| RF-04 | 🟡 O sistema deve executar `adb connect <ip>:5555`, pedir ao usuário que aceite o aviso na TV e aguardar o estado `device` por até 120 s. | Must | Após aceitar na TV, a etapa fica verde; sem aceitar em 120 s, mostra erro de autorização. |
| RF-05 | 🟡 O sistema deve sempre usar `-s <ip>:5555` em todos os comandos ADB. | Must | Com outro aparelho conectado, nenhum comando vai para o aparelho errado. |
| RF-06 | 🟡 O sistema deve instalar ou atualizar o APK do launcher com `adb install -r`. | Must | O pacote `br.tematv.launcher` aparece em `pm list packages`. |
| RF-07 | 🟡 O sistema deve abrir o launcher uma vez em seguida à instalação do APK, para o app criar as próprias pastas e o `device_code.txt`, e então ler o código do aparelho. | Must | O código lido é igual ao exibido na tela de ativação da TV. |
| RF-08 | 🟡 O sistema deve, depois de RF-07, copiar o `.tematv` do cliente para a pasta de temas do app, gravar o nome do pacote em `active_theme.txt`, pedir ao `license-activation` a licença dessa TV e gravar `license.key` na pasta do app. | Must | Ao final, o tema do cliente aparece e o diagnóstico do launcher mostra "Licença válida". |
| RF-09 | 🟡 O sistema deve ligar o serviço de acessibilidade do launcher acrescentando-o a `enabled_accessibility_services` sem remover os serviços já presentes, e ativar `accessibility_enabled = 1`. | Must | Um serviço que já estava ligado continua na lista depois da instalação. |
| RF-10 | 🟡 O sistema deve conferir o resultado: enviar a tecla Home e verificar por `dumpsys` que a atividade do launcher está em primeiro plano em até 5 s. | Must | A última etapa fica verde só com o launcher em primeiro plano. |
| RF-11 | 🟡 O sistema deve mostrar uma lista de verificação com uma linha por etapa (conectar, autorizar, instalar, primeira abertura, tema, licença, acessibilidade, conferência) e o tempo decorrido. | Must | Cada etapa mostra pendente, em andamento, ok ou erro. |
| RF-12 | 🟡 O sistema deve registrar cada instalação em `~/tematv/instalacoes/<AAAA-MM-DD>-<ip>.log` com cliente, IP, fabricante, modelo, versão do Android, código do aparelho, resultado de cada etapa e tempo total. | Must | O arquivo existe após cada tentativa, com sucesso ou com erro. |
| RF-13 | 🟡 O usuário deve poder desfazer a instalação numa TV: o sistema remove o serviço de `enabled_accessibility_services` e desinstala o launcher. | Should | Após desfazer, apertar Home mostra o launcher do Google. |
| RF-14 | 🟡 O usuário deve poder gerar um kit de pendrive com o APK, o `.tematv`, a `license.key` (se o código do aparelho for informado) e um `LEIA-ME.txt` com o passo a passo de instalação pela TV. | Should | A pasta do kit contém os quatro arquivos. |

### 6.2 Fluxo Principal (Happy Path)

1. 🟡 O usuário abre "Instalar na TV", escolhe "Igreja Batista X" e digita `192.168.1.2`.
2. 🟡 O sistema avisa para ligar a TV, confirma que ela está ligada e respondendo na rede (RF-15), confere o `adb` e a porta 5555 e conecta.
3. 🟡 O usuário aceita o aviso de depuração na TV.
4. 🟡 O sistema instala o APK, abre o app uma vez para criar as pastas e ler o código do aparelho, copia o tema, emite e grava a licença e liga o serviço de acessibilidade.
5. 🟡 O sistema envia Home e confere que a tela temática está em primeiro plano.
6. 🟡 Resultado: todas as etapas verdes, tempo total exibido e log gravado.

### 6.3 Fluxos Alternativos

**Fluxo Alternativo A — TV já instalada (troca de tema):**
1. 🟡 O launcher já está instalado na mesma versão.
2. 🟡 O sistema pula a instalação do APK e da licença válida e só copia o novo tema.

**Fluxo Alternativo B — Pendrive:**
1. 🟡 O usuário gera o kit de pendrive (RF-14).
2. 🟡 Na TV, instala o APK com um instalador de arquivos e segue o `LEIA-ME.txt` para ligar o serviço de acessibilidade à mão.

---

## 7. Requisitos Não-Funcionais

| ID | Requisito | Valor alvo | Observação |
|----|-----------|-----------|------------|
| RNF-01 | 🟡 Performance | Clique em "Instalar" até a conferência ≤ 5 min em Wi-Fi local | Sem contar o tempo do usuário aceitando na TV |
| RNF-02 | 🟡 Segurança | Nenhum comando ADB que apague dados, desative apps ou altere configurações além das listadas em RF-06 a RF-10 | Preserva a TV do cliente |
| RNF-03 | 🟡 Idempotência | Repetir a instalação na mesma TV produz o mesmo estado final, sem duplicar o serviço na lista | Reinstalar não quebra a TV |
| RNF-04 | 🟡 Rastreabilidade | 100% das tentativas com log | Ajuda no suporte |

---

## 8. Design e Interface

**Componentes afetados:** 🟡 aba "Instalar na TV" da ferramenta do notebook (seleção do cliente, campo de IP, botão "Instalar", lista de verificação, botões "Desfazer" e "Gerar kit de pendrive").

**Comportamento esperado:**
🟡 Ao clicar em "Instalar", as etapas executam em sequência; na etapa de autorização, a página mostra em destaque "Aceite o aviso 'Permitir depuração?' na tela da TV e marque 'Sempre permitir'".

**Estados da UI:**
- 🟡 Estado vazio: sem cliente escolhido, "Instalar" fica desabilitado.
- 🟡 Estado de carregamento: etapa atual com indicador de andamento e tempo decorrido.
- 🟡 Estado de erro: etapa em vermelho com a mensagem da seção 11 e botão "Tentar de novo a partir desta etapa".
- 🟡 Estado de sucesso: "TV pronta em mm:ss" com o caminho do log.

---

## 9. Modelo de Dados

**Entidades novas ou modificadas:**

```
InstallRecord (~/tematv/instalacoes/<AAAA-MM-DD>-<ip>.log, uma linha JSON por etapa) {
  timestamp: datetime
  client_theme_id: string
  tv_ip: string
  tv_manufacturer: string       // ro.product.manufacturer
  tv_model: string              // ro.product.model
  android_version: string       // ro.build.version.release
  device_code: string?          // do license-activation
  step: "adb" | "port" | "connect" | "authorize" | "install" | "first_launch" | "theme" | "license" | "accessibility" | "verify"
  result: "ok" | "error" | "skipped"
  detail: string?               // mensagem de erro
  elapsed_seconds: int
}
```

**Migrações necessárias:** 🟡 Não.

---

## 10. Integrações e Dependências

| Dependência | Tipo | Impacto se indisponível |
|-------------|------|------------------------|
| 🟡 `adb` (Android platform-tools) no notebook | Obrigatória | Mostra o comando de instalação (RF-01) |
| 🟡 Depuração por rede ligada na TV (porta 5555) | Obrigatória para ADB | Mostra o passo a passo (RF-03); alternativa: kit de pendrive |
| 🟡 APK do launcher assinado | Obrigatória | Sem APK, a etapa de instalação falha com mensagem clara |
| 🟡 `theme-builder` (pacote `.tematv` do cliente) | Obrigatória | Sem pacote, pede para gerar o pacote primeiro |
| 🟡 `license-activation` (emissão da licença) | Obrigatória | Sem chave privada, a etapa de licença falha (ver EC-07) |

---

## 11. Edge Cases e Tratamento de Erros

| Cenário | Trigger | Comportamento esperado |
|---------|---------|----------------------|
| 🟡 EC-01: TV fora da rede | Sem resposta ao teste de conexão em 5 s | "TV não encontrada em <ip>. Confira o IP em Configurações > Rede da TV e se o notebook está na mesma rede." |
| 🟡 EC-02: Autorização não aceita | Estado `unauthorized` por 120 s | "A TV não autorizou este computador. Aceite o aviso na tela da TV e tente de novo." |
| 🟡 EC-03: Falha na instalação do APK | `INSTALL_FAILED_*` retornado pelo ADB | Mostra o código de falha; com `INSTALL_FAILED_INSUFFICIENT_STORAGE`, sugere liberar espaço. |
| 🟡 EC-04: Cópia para a pasta do app negada | Erro de permissão ao gravar no armazenamento do app | Registra o erro e tenta o caminho alternativo de importação pelo próprio app (OQ-01). |
| 🟡 EC-05: Conexão cai no meio | ADB desconecta durante uma etapa | Reconecta uma vez; se falhar, para e oferece "Tentar de novo a partir desta etapa". |
| 🟡 EC-06: Conferência falha | Launcher não fica em primeiro plano em 5 s | Etapa em vermelho com o pacote em primeiro plano encontrado, registrado no log. |
| 🟡 EC-07: Chave privada de licença ausente | Arquivo da chave não encontrado no notebook | Para na etapa de licença com "Chave de licença não encontrada; veja license-activation". |
| 🟡 EC-08: Lista de acessibilidade já contém o serviço | Reinstalação | Não duplica a entrada (RNF-03). |

---

## 12. Segurança e Privacidade

- 🟡 **Autenticação:** a TV autoriza o notebook pela chave RSA do ADB, aceita na tela.
- 🟡 **Autorização:** o sistema só executa os comandos listados nesta spec.
- 🟡 **Dados sensíveis:** o log não guarda a chave da Bíblia API nem a chave privada de licença; guarda IP, modelo e código do aparelho.
- 🟡 **Auditoria:** log por instalação (RF-12).

---

## 13. Plano de Rollout

- 🟡 **Estratégia:** primeiro na TCL de referência, depois em clientes piloto.
- 🟡 **Como reverter (rollback):** botão "Desfazer instalação" (RF-13) ou opção "Voltar ao launcher original do Google" na TV.
- 🟡 **Monitoramento pós-deploy:** comparar o tempo total nos logs com a meta de 10 min.

---

## 14. Open Questions

| # | Pergunta | Impacto | Dono | Prazo |
|---|---------|---------|------|-------|
| OQ-01 | 🟡 Em Android 12 ou superior o ADB ainda grava na pasta externa do app? Se não, o app precisa importar o tema de `/data/local/tmp` ou da pasta Download. | Médio | Augusto | Antes de instalar em TV com Android 12+ |
| OQ-02 | 🟡 A premissa do PRD "instalar sem modo desenvolvedor" conflita com o caminho ADB, que exige depuração ligada. Aceitar o modo desenvolvedor para o MVP? | Alto | Augusto | Antes do primeiro cliente |
| OQ-03 | 🟡 A depuração por rede deve ser desligada ao final da instalação, por segurança? | Médio | Augusto | Antes do primeiro cliente |

---

## 15. Decisões Tomadas (Decision Log)

| Decisão | Alternativas consideradas | Racional |
|---------|--------------------------|---------|
| 🟡 ADB pela rede como caminho principal | Pendrive como principal | Escolha do usuário; permite automatizar tudo e cumprir os 10 min |
| 🟡 Aba dentro da mesma ferramenta do `theme-builder` | Ferramenta separada | Um só programa para o revendedor aprender |
| 🟡 Ligar a acessibilidade por `settings put` | Pedir ao usuário para ligar nas Configurações | Sem passo manual na TV |

---

## Apêndice

### Referências
- 🟡 [`prd.md`](../prd.md), [`personas.md`](../personas.md)
- 🟡 Specs relacionadas: [`theme-builder.md`](./theme-builder.md), [`home-takeover.md`](./home-takeover.md), [`license-activation.md`](./license-activation.md), [`theme-engine.md`](./theme-engine.md)

### Histórico de Revisões
| Versão | Data | Autor | Mudanças |
|--------|------|-------|---------|
| 1.0 | 2026-10-29 | reversa-spec-sdd | Criação inicial |

---

## Relatório de avaliação

🟡 **Score total:** 100/100 (⭐ Excelente — Pronta para implementação)  
🟡 **Iterações:** 3 (100 → 95 → 100, após correções de revisão)  
🟡 **Avaliado em:** 2026-10-29T19:51:15-03:00 por `spec_scorer.py`

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
