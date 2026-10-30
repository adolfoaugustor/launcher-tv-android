# Onboarding: Instalação na TV (device-provisioning)

> Identificador: `006-device-provisioning`
> Data: `2026-10-30`

## 1. Uma vez no notebook

```bash
cd launcher/android
./tools/gerar-chave-assinatura.sh
```

Guarde uma cópia de `~/.config/tematv/assinatura.jks`, `~/.config/tematv/assinatura.properties` e `~/.config/tematv/activation-secret` num pendrive ou cofre de senhas.

```bash
./tools/instalar-tematv.sh
```

O script compila a ferramenta e o launcher de produção (`~/tematv/launcher/`).

## 2. Na TV (uma vez por TV)

Configurações › Preferências do dispositivo › Sobre › clique 7 vezes em "Build" para ligar as Opções do desenvolvedor; em Opções do desenvolvedor, ligue "Depuração USB" e "Depuração por rede" (ou "ADB pela rede"). Anote o IP em Configurações › Rede.

## 3. Instalar

1. `tematv`, aba "Instalar na TV".
2. Escolha o cliente (com pacote gerado) e digite o IP. Ligue a TV e clique em "Verificar TV".
3. "Instalar". Quando pedir, aceite "Permitir depuração?" na TV marcando "Sempre permitir".
4. **Conferir:** todas as etapas verdes, a TV na tela temática do cliente, ativada; registro em `~/tematv/instalacoes/`.

## 4. TV de teste (já tinha o launcher de teste)

Na etapa "Instalar" aparece "assinatura diferente". Clique em "Desfazer e instalar de novo": a TV perde a ativação antiga, ganha um código novo e é ativada de novo pela própria aba.

## 5. Repetir e desfazer

- Instalar de novo na mesma TV: a etapa do app aparece como "já instalado" e o tema é trocado.
- "Desfazer instalação": o launcher sai e o Home volta ao Google.
