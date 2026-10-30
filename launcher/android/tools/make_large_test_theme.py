#!/usr/bin/env python3
"""Gera um pacote de tema perto do limite de 15 MB para medir o tempo de aplicação na TV.

O fundo é ruído aleatório, que não comprime, então o .tematv fica com cerca de 14 MB.
Saída: app/build/large-test-theme/grande-teste.tematv (fora do git).

Uso: python3 tools/make_large_test_theme.py   (a partir de launcher/android/)
"""
import json
import os
import struct
import zipfile
import zlib
from pathlib import Path

ANDROID_ROOT = Path(__file__).resolve().parent.parent
SOURCE_THEME_DIR = ANDROID_ROOT / "sample-themes/biblia-exemplo"
OUTPUT_FILE = ANDROID_ROOT / "app/build/large-test-theme/grande-teste.tematv"
NOISE_WIDTH = 1920
NOISE_HEIGHT = 1800
MAX_PACKAGE_BYTES = 15 * 1024 * 1024


def noise_png(width, height):
    def chunk(kind, data):
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    rows = b"".join(b"\x00" + os.urandom(width * 4) for _ in range(height))
    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", zlib.compress(rows, 1)) + chunk(b"IEND", b"")


def main():
    manifest = json.loads((SOURCE_THEME_DIR / "theme.json").read_text(encoding="utf-8"))
    manifest["theme_id"] = "grande-teste"
    manifest["display_name"] = "Pacote grande de teste"
    manifest["background_image"] = "fundo-grande.png"

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(OUTPUT_FILE, "w", compression=zipfile.ZIP_STORED) as package:
        package.writestr("theme.json", json.dumps(manifest, ensure_ascii=False, indent=2))
        for name in ("logo.png", "frame-biblia.png"):
            package.write(SOURCE_THEME_DIR / name, name)
        package.writestr("fundo-grande.png", noise_png(NOISE_WIDTH, NOISE_HEIGHT))

    size = OUTPUT_FILE.stat().st_size
    if size > MAX_PACKAGE_BYTES:
        raise SystemExit(f"pacote com {size} bytes passou do limite de {MAX_PACKAGE_BYTES}")
    print(f"gerado {OUTPUT_FILE.relative_to(ANDROID_ROOT)} ({size / 1024 / 1024:.1f} MB)")


if __name__ == "__main__":
    main()
