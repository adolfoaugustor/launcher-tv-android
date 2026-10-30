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

echo "Ferramenta instalada. Para abrir: tematv"

# Launcher de produção para a aba "Instalar na TV" (feature 006): só com a chave definitiva.
launcher_dir="$HOME/tematv/launcher"
if [ -f "$HOME/.config/tematv/assinatura.properties" ]; then
    ./gradlew -q :app:assembleRelease
    release_dir=app/build/outputs/apk/release
    sdk_dir=$(sed -n 's/^sdk.dir=//p' local.properties)
    apksigner=$(ls -d "$sdk_dir"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1)
    signer=""
    if [ -n "$apksigner" ]; then
        signer=$("$apksigner" verify --print-certs "$release_dir/app-release.apk" | sed -n 's/^Signer #1 certificate SHA-256 digest: //p')
    fi
    mkdir -p "$launcher_dir"
    cp "$release_dir/app-release.apk" "$launcher_dir/tematv-launcher.apk"
    python3 - "$release_dir/output-metadata.json" "$signer" > "$launcher_dir/launcher.json" <<'PYTHON'
import json, sys
element = json.load(open(sys.argv[1]))["elements"][0]
info = {"version_code": element["versionCode"], "version_name": element["versionName"]}
if sys.argv[2]:
    info["signer_sha256"] = sys.argv[2]
print(json.dumps(info))
PYTHON
    echo "Launcher $(sed -n 's/.*"version_name": "\([^"]*\)".*/\1/p' "$launcher_dir/launcher.json") pronto em $launcher_dir para a aba \"Instalar na TV\"."
    [ -n "$signer" ] || echo "Aviso: apksigner não encontrado; a aba vai reinstalar o launcher sempre, mesmo na mesma versão."
else
    echo "Chave de assinatura não encontrada: a aba \"Instalar na TV\" fica sem o launcher. Crie com tools/gerar-chave-assinatura.sh e rode este script de novo."
fi
case ":$PATH:" in
    *":$HOME/.local/bin:"*) ;;
    *) echo "Abra um terminal novo (ou rode: export PATH=\"\$HOME/.local/bin:\$PATH\") para o comando tematv aparecer." ;;
esac
