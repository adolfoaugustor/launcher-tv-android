# Investigation: Motor de temas (theme-engine)

> Identificador: `001-theme-engine`
> Data: `2026-10-29`

## 1. Pesquisa de fundo

- 🟢 **Armazenamento no Android 10+ (scoped storage):** um app lê e grava a própria pasta externa (`Android/data/<pacote>/files/`) sem pedir permissão. A pasta Download pública exige permissão ou o seletor de arquivos do sistema. Por isso os pacotes ficam na pasta externa do próprio app (D-07).
- 🟢 **A pasta externa do app não existe até o app chamar `getExternalFilesDir`.** O app precisa criá-la ao iniciar (D-08), e a feature 6 precisa abrir o app antes de copiar o tema (já corrigido em `_reversa_sdd/sdd/device-provisioning.md`, RF-07 e RF-08).
- 🟡 **Gravação pelo ADB em `Android/data`:** no Android 11, o usuário `shell` do ADB grava em `Android/data/<pacote>/`. No Android 13+ há relatos de restrição. A TCL de referência é Android 11. Registrado como premissa no `roadmap.md`, §4.
- 🟢 **Zip slip:** uma entrada do ZIP chamada `../../x` pode escrever fora da pasta de destino se o caminho não for normalizado. A mitigação padrão é resolver o caminho final e conferir que ele começa com a pasta de destino.
- 🟢 **Zip bomb:** um ZIP pequeno pode descompactar em gigabytes. A mitigação é limitar o total descompactado e o número de entradas (D-05: 40 MB e 200 entradas).
- 🟢 **Memória de imagens:** um PNG de 4000×3000 ocupa cerca de 48 MB decodificado em ARGB_8888. Decodificar com `inSampleSize` até caber em 1920×1080 limita cada imagem a cerca de 8 MB.

## 2. Alternativas avaliadas

| Tema | Alternativa | Por que foi descartada |
|------|-------------|------------------------|
| Linguagem do app | Java | Kotlin é o padrão do Android e tem `StateFlow` para avisar inscritos |
| Linguagem do app | Flutter / React Native | Pesam mais em memória numa TV de 2 GB, e o launcher precisa de APIs nativas (acessibilidade na feature 3) |
| Leitura de JSON | `org.json` | Nos testes JVM, os métodos são stubs que lançam exceção; `kotlinx.serialization` funciona nos dois lados |
| Testes | Robolectric no `:app` | Mais lento; separar o núcleo em `:theme-core` (JVM pura) deixa os testes rápidos e sem Android |
| Aviso de troca de tema | `FileObserver` na pasta externa | Comportamento inconsistente em armazenamento externo (FUSE); recarga explícita é previsível |
| Tema padrão | `.tematv` dentro de `assets/` | Exigiria descompactar a cada boot; pasta pronta em `assets/` é direta |
| Arte | Esperar designer | Bloquearia a feature; arte provisória gerada por script é substituível sem mudar código |

## 3. Padrões aplicáveis

- **Repositório com estado observável:** `ThemeRepository` guarda o tema ativo num `StateFlow`; a tela da feature 2 coleta esse fluxo.
- **Resultado explícito em vez de exceção solta:** a validação devolve `Result` com `ThemeError(code, field)`; nada é engolido (regra "falhar cedo" do usuário).
- **Último estado bom conhecido (last known good):** um pacote recusado não substitui o tema em uso (RN-02).

## 4. Fontes externas

- 🟢 Documentação Android sobre armazenamento específico do app: https://developer.android.com/training/data-storage/app-specific
- 🟢 Documentação Android sobre carregamento eficiente de bitmaps: https://developer.android.com/topic/performance/graphics/load-bitmap
- 🟢 Documentação Kotlin sobre `kotlinx.serialization`: https://kotlinlang.org/docs/serialization.html
- 🟢 Descrição do ataque zip slip (Snyk): https://security.snyk.io/research/zip-slip-vulnerability
