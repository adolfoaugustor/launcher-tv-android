# Onboarding: Ferramenta de preparo de tema (theme-builder)

> Identificador: `005-theme-builder`
> Data: `2026-10-30`

Passo a passo para testar a ferramenta pela primeira vez no notebook (Linux ou WSL2).

## 1. Instalar

```bash
cd launcher/android
./tools/instalar-tematv.sh
```

O script compila a ferramenta e cria o comando `tematv` em `~/.local/bin`. Se o terminal disser que `tematv` não existe, abra um terminal novo.

## 2. Abrir

```bash
tematv
```

O terminal mostra a URL (normalmente `http://localhost:8787`) e o navegador do Windows abre a página. **Conferir:** a página abre no navegador do Windows. Se não abrir, copie a URL do terminal para o navegador.

## 3. Cliente de igreja

1. "Novo cliente" → tipo "Igreja", nome "Igreja Teste".
2. Envie um logotipo PNG, escolha as três cores e escreva a mensagem.
3. Confira que a lista tem os 20 versículos NVI e que a prévia muda a cada alteração.
4. "Gerar pacote". **Conferir:** aparece `~/tematv/temas/igreja-teste.tematv` e os comandos do ADB para enviar à TV.

## 4. Cliente de loja

1. "Novo cliente" → tipo "Loja", nome "Hamburgueria Teste".
2. Moldura "Hambúrguer", duas mensagens da loja, atribuição "Mensagens da Hamburgueria Teste".
3. "Gerar pacote" e conferir o arquivo em `~/tematv/temas/`.

## 5. Ativar TV

1. Aba "Ativar TV", digite o código `2544-3298-7734` e o cliente.
2. **Conferir:** a senha mostrada é `8065-4698-2216` (a mesma entregue na feature 004) e há uma linha nova em `~/tematv/ativacoes.csv`.

## 6. Na TV (quando você quiser)

Copie os comandos que a página mostrou depois de gerar o pacote e rode com a TV ligada e conectada pelo ADB. Isso troca o tema do launcher na TV.

## 7. Erros comuns

| Mensagem | O que fazer |
|----------|-------------|
| "Segredo de ativação não encontrado" | Restaure `~/.config/tematv/activation-secret` do backup |
| "Envie um PNG de até 5 MB" | Converta o logotipo para PNG |
| Porta ocupada | A ferramenta usa a próxima porta; a URL aparece no terminal |
