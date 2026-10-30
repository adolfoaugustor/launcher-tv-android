# Onboarding: testar a ativação (feature 004)

> Pré-requisitos: ferramentas da feature 001 e `local.properties` com o `sdk.dir`.

## 1. Gerar o segredo (uma vez por revendedor)

```bash
cd launcher/android
./gradlew :activation-tool:installDist
activation-tool/build/install/activation-tool/bin/activation-tool gerar-segredo
```

Faça uma cópia de segurança de `~/.config/tematv/activation-secret`: sem ele não é possível ativar novas TVs com o mesmo APK.

## 2. Compilar e testar

```bash
./gradlew :theme-core:test :activation-core:test :app:testDebugUnitTest :app:assembleDebug
```

Sem o segredo, a compilação do app para com a instrução do passo 1.

## 3. Ativar uma TV

1. Instale o APK (`adb install -r app/build/outputs/apk/debug/app-debug.apk`).
2. Na TV, a tela inicial mostra o código do aparelho (`XXXX-XXXX-XXXX`).
3. No notebook:
   ```bash
   activation-tool/build/install/activation-tool/bin/activation-tool senha 1234-5678-9012 --cliente "Igreja X"
   ```
4. Digite a senha na TV e escolha "Ativar". A grade temática aparece.

Ativação pelo ADB, sem digitar: ver `interfaces/ativacao-adb.md`.
