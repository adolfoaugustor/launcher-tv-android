#!/usr/bin/env bash
# Cria a chave definitiva de assinatura do launcher (feature 006). Uma única vez por revendedor.
set -euo pipefail

config_dir="$HOME/.config/tematv"
keystore="$config_dir/assinatura.jks"
properties="$config_dir/assinatura.properties"

if [ -e "$keystore" ] || [ -e "$properties" ]; then
    echo "Já existe uma chave em $config_dir. Use a existente: uma chave nova impede atualizar as TVs já instaladas e troca o código do aparelho." >&2
    exit 1
fi
if ! command -v keytool >/dev/null; then
    echo "keytool não encontrado. Instale o JDK 17: sudo apt install openjdk-17-jdk" >&2
    exit 1
fi

umask 077
mkdir -p "$config_dir"
TEMATV_KEY_PASSWORD=$(head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 32)
export TEMATV_KEY_PASSWORD
keytool -genkeypair -noprompt \
    -keystore "$keystore" -storetype PKCS12 \
    -alias tematv -keyalg RSA -keysize 4096 -validity 10000 \
    -dname "CN=TemaTV Launcher" \
    -storepass:env TEMATV_KEY_PASSWORD -keypass:env TEMATV_KEY_PASSWORD >/dev/null
cat > "$properties" <<PROPERTIES
storeFile=$keystore
storePassword=$TEMATV_KEY_PASSWORD
keyAlias=tematv
keyPassword=$TEMATV_KEY_PASSWORD
PROPERTIES
chmod 600 "$keystore" "$properties"

echo "Chave de assinatura criada em $keystore (permissão 600)."
echo "Faça uma cópia de segurança de $keystore e $properties: sem eles não é possível atualizar as TVs já instaladas."
