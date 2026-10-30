#!/usr/bin/env bash
# Instala a ferramenta de preparo de tema (feature 005) e cria o comando `tematv`.
set -euo pipefail

cd "$(dirname "$0")/.."

if ! command -v java >/dev/null; then
    echo "Java não encontrado. Instale o JDK 17: sudo apt install openjdk-17-jdk" >&2
    exit 1
fi
java_major=$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1)
if [ "${java_major:-0}" -lt 17 ]; then
    echo "É preciso o JDK 17 ou mais novo (encontrado: ${java_major:-desconhecido}). Instale com: sudo apt install openjdk-17-jdk" >&2
    exit 1
fi

./gradlew -q :theme-builder:installDist

install_dir="$HOME/.local/share/tematv"
rm -rf "$install_dir"
mkdir -p "$install_dir" "$HOME/.local/bin"
cp -r theme-builder/build/install/tematv/. "$install_dir"
ln -sf "$install_dir/bin/tematv" "$HOME/.local/bin/tematv"

echo "Instalado. Para abrir: tematv"
case ":$PATH:" in
    *":$HOME/.local/bin:"*) ;;
    *) echo "Abra um terminal novo (ou rode: export PATH=\"\$HOME/.local/bin:\$PATH\") para o comando tematv aparecer." ;;
esac
