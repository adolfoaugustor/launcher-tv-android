#!/usr/bin/env python3
"""Gera a arte provisória dos temas (molduras, logotipo e fundos) em PNG.

Não depende de nenhuma biblioteca: desenha formas simples num buffer RGBA e grava
o PNG com zlib. A arte final pode substituir estes arquivos sem mudar o código.

Uso: python3 tools/make_placeholder_art.py   (a partir de launcher/android/)
"""
import math
import struct
import zlib
from pathlib import Path

ANDROID_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_THEME_DIR = ANDROID_ROOT / "app/src/main/assets/default-theme"
BIBLE_SAMPLE_DIR = ANDROID_ROOT / "sample-themes/biblia-exemplo"
BURGER_SAMPLE_DIR = ANDROID_ROOT / "sample-themes/hamburguer-exemplo"
LAUNCHER_BANNER_FILE = ANDROID_ROOT / "app/src/main/res/drawable-xhdpi/banner.png"

FRAME_WIDTH = 260
FRAME_HEIGHT = 340
ANTIALIAS_SCALE = 3


def hex_color(value, alpha=255):
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha)


class Canvas:
    def __init__(self, width, height, background=(0, 0, 0, 0)):
        self.width = width
        self.height = height
        self.pixels = bytearray(bytes(background) * (width * height))

    def blend_pixel(self, x, y, color):
        if not (0 <= x < self.width and 0 <= y < self.height):
            return
        src_r, src_g, src_b, src_a = color
        if src_a == 0:
            return
        index = (y * self.width + x) * 4
        if src_a == 255:
            self.pixels[index:index + 4] = bytes(color)
            return
        dst_r, dst_g, dst_b, dst_a = self.pixels[index:index + 4]
        alpha = src_a / 255
        out_a = alpha + dst_a / 255 * (1 - alpha)
        if out_a == 0:
            return

        def mix(src, dst):
            return round((src * alpha + dst * (dst_a / 255) * (1 - alpha)) / out_a)

        self.pixels[index:index + 4] = bytes(
            (mix(src_r, dst_r), mix(src_g, dst_g), mix(src_b, dst_b), round(out_a * 255))
        )

    def fill_shape(self, bounds, contains, color):
        x0, y0, x1, y1 = (int(math.floor(v)) for v in bounds)
        for y in range(max(0, y0), min(self.height, y1 + 1)):
            for x in range(max(0, x0), min(self.width, x1 + 1)):
                if contains(x + 0.5, y + 0.5):
                    self.blend_pixel(x, y, color)

    def fill_rect(self, x0, y0, x1, y1, color):
        self.fill_shape((x0, y0, x1, y1), lambda x, y: x0 <= x <= x1 and y0 <= y <= y1, color)

    def fill_rounded_rect(self, x0, y0, x1, y1, radius, color):
        def contains(x, y):
            if not (x0 <= x <= x1 and y0 <= y <= y1):
                return False
            corner_x = min(max(x, x0 + radius), x1 - radius)
            corner_y = min(max(y, y0 + radius), y1 - radius)
            return (x - corner_x) ** 2 + (y - corner_y) ** 2 <= radius ** 2

        self.fill_shape((x0, y0, x1, y1), contains, color)

    def fill_ellipse(self, cx, cy, rx, ry, color, clip=None):
        def contains(x, y):
            if clip and not clip(x, y):
                return False
            return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1

        self.fill_shape((cx - rx, cy - ry, cx + rx, cy + ry), contains, color)

    def fill_ring(self, cx, cy, inner_radius, outer_radius, color, clip=None):
        def contains(x, y):
            if clip and not clip(x, y):
                return False
            return inner_radius ** 2 <= (x - cx) ** 2 + (y - cy) ** 2 <= outer_radius ** 2

        self.fill_shape((cx - outer_radius, cy - outer_radius, cx + outer_radius, cy + outer_radius), contains, color)

    def fill_polygon(self, points, color):
        xs = [p[0] for p in points]
        ys = [p[1] for p in points]

        def contains(x, y):
            inside = False
            j = len(points) - 1
            for i in range(len(points)):
                xi, yi = points[i]
                xj, yj = points[j]
                if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi) + xi:
                    inside = not inside
                j = i
            return inside

        self.fill_shape((min(xs), min(ys), max(xs), max(ys)), contains, color)

    def fill_vertical_gradient(self, top_color, bottom_color):
        for y in range(self.height):
            t = y / max(1, self.height - 1)
            color = bytes(round(a + (b - a) * t) for a, b in zip(top_color, bottom_color))
            row = color * self.width
            start = y * self.width * 4
            self.pixels[start:start + len(row)] = row

    def downsample(self, factor):
        small = Canvas(self.width // factor, self.height // factor)
        samples = factor * factor
        for y in range(small.height):
            for x in range(small.width):
                totals = [0, 0, 0, 0]
                for dy in range(factor):
                    row_start = ((y * factor + dy) * self.width + x * factor) * 4
                    for dx in range(factor):
                        index = row_start + dx * 4
                        alpha = self.pixels[index + 3]
                        totals[0] += self.pixels[index] * alpha
                        totals[1] += self.pixels[index + 1] * alpha
                        totals[2] += self.pixels[index + 2] * alpha
                        totals[3] += alpha
                out_index = (y * small.width + x) * 4
                if totals[3] == 0:
                    continue
                small.pixels[out_index:out_index + 4] = bytes((
                    round(totals[0] / totals[3]),
                    round(totals[1] / totals[3]),
                    round(totals[2] / totals[3]),
                    round(totals[3] / samples),
                ))
        return small

    def save_png(self, path):
        def chunk(kind, data):
            body = kind + data
            return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

        stride = self.width * 4
        raw = b"".join(b"\x00" + bytes(self.pixels[y * stride:(y + 1) * stride]) for y in range(self.height))
        header = struct.pack(">IIBBBBB", self.width, self.height, 8, 6, 0, 0, 0)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(
            b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
        )
        print(f"gerado {path.relative_to(ANDROID_ROOT)} ({self.width}x{self.height})")


def draw_bible_frame():
    s = ANTIALIAS_SCALE
    canvas = Canvas(FRAME_WIDTH * s, FRAME_HEIGHT * s)
    cover = hex_color("#7A4524")
    gold = hex_color("#E8B33C")
    pages = hex_color("#FBF3DC")
    panel = hex_color("#FFFDF6")
    ribbon = hex_color("#C62828")
    # Bloco de páginas aparecendo à direita da capa
    canvas.fill_rounded_rect(30 * s, 14 * s, 252 * s, 330 * s, 14 * s, pages)
    for line in range(4):
        x = (240 + line * 3) * s
        canvas.fill_rect(x, 24 * s, x + s, 320 * s, hex_color("#D9C9A3"))
    # Capa
    canvas.fill_rounded_rect(8 * s, 8 * s, 238 * s, 332 * s, 18 * s, cover)
    canvas.fill_rounded_rect(18 * s, 18 * s, 228 * s, 322 * s, 12 * s, gold)
    canvas.fill_rounded_rect(22 * s, 22 * s, 224 * s, 318 * s, 10 * s, cover)
    # Cruz dourada no topo
    canvas.fill_rect(113 * s, 26 * s, 125 * s, 62 * s, gold)
    canvas.fill_rect(101 * s, 36 * s, 137 * s, 46 * s, gold)
    # Painel do ícone do app (icon_slot: x 0.18 y 0.20 w 0.64 h 0.42)
    canvas.fill_rounded_rect(0.16 * FRAME_WIDTH * s, 0.185 * FRAME_HEIGHT * s,
                             0.84 * FRAME_WIDTH * s, 0.635 * FRAME_HEIGHT * s, 14 * s, gold)
    canvas.fill_rounded_rect(0.18 * FRAME_WIDTH * s, 0.20 * FRAME_HEIGHT * s,
                             0.82 * FRAME_WIDTH * s, 0.62 * FRAME_HEIGHT * s, 12 * s, panel)
    # Faixa do nome do app (label_slot: x 0.12 y 0.70 w 0.76 h 0.14)
    canvas.fill_rounded_rect(0.12 * FRAME_WIDTH * s, 0.70 * FRAME_HEIGHT * s,
                             0.88 * FRAME_WIDTH * s, 0.84 * FRAME_HEIGHT * s, 10 * s, pages)
    # Fita marcadora saindo por baixo
    canvas.fill_polygon([(190 * s, 300 * s), (206 * s, 300 * s), (206 * s, 338 * s),
                         (198 * s, 330 * s), (190 * s, 338 * s)], ribbon)
    return canvas.downsample(s)


def draw_burger_frame():
    s = ANTIALIAS_SCALE
    canvas = Canvas(FRAME_WIDTH * s, FRAME_HEIGHT * s)
    bun = hex_color("#E9A23B")
    bun_dark = hex_color("#C97F1F")
    sesame = hex_color("#FFF4D6")
    lettuce = hex_color("#4CAF50")
    cheese = hex_color("#FFC928")
    patty = hex_color("#6D3B1F")
    panel = hex_color("#FFFDF6")
    width = FRAME_WIDTH * s
    # Pão de cima (meia elipse)
    canvas.fill_ellipse(width / 2, 92 * s, 122 * s, 84 * s, bun, clip=lambda x, y: y <= 92 * s)
    canvas.fill_rect(8 * s, 86 * s, 252 * s, 100 * s, bun)
    for sx, sy in [(70, 40), (110, 28), (150, 30), (190, 44), (90, 66), (140, 58), (180, 70)]:
        canvas.fill_ellipse(sx * s, sy * s, 7 * s, 3.5 * s, sesame)
    # Alface ondulada
    wave = [(8 * s, 98 * s)]
    for step in range(0, 13):
        x = (8 + step * 20) * s
        wave.append((x, (110 if step % 2 == 0 else 122) * s))
    wave += [(252 * s, 98 * s)]
    canvas.fill_polygon(wave, lettuce)
    # Carne, onde fica o ícone do app (icon_slot: x 0.15 y 0.30 w 0.70 h 0.32)
    canvas.fill_rounded_rect(4 * s, 0.27 * FRAME_HEIGHT * s, 256 * s, 0.655 * FRAME_HEIGHT * s, 22 * s, patty)
    canvas.fill_rounded_rect(0.15 * FRAME_WIDTH * s, 0.30 * FRAME_HEIGHT * s,
                             0.85 * FRAME_WIDTH * s, 0.62 * FRAME_HEIGHT * s, 12 * s, panel)
    # Queijo escorrendo
    canvas.fill_polygon([(20 * s, 0.645 * FRAME_HEIGHT * s), (240 * s, 0.645 * FRAME_HEIGHT * s),
                         (200 * s, 0.70 * FRAME_HEIGHT * s), (150 * s, 0.665 * FRAME_HEIGHT * s),
                         (90 * s, 0.705 * FRAME_HEIGHT * s), (50 * s, 0.67 * FRAME_HEIGHT * s)], cheese)
    # Pão de baixo, onde fica o nome do app (label_slot: x 0.14 y 0.70 w 0.72 h 0.13)
    canvas.fill_rounded_rect(8 * s, 0.68 * FRAME_HEIGHT * s, 252 * s, 0.95 * FRAME_HEIGHT * s, 30 * s, bun)
    canvas.fill_rect(8 * s, 0.90 * FRAME_HEIGHT * s, 252 * s, 0.92 * FRAME_HEIGHT * s, bun_dark)
    canvas.fill_rounded_rect(0.14 * FRAME_WIDTH * s, 0.70 * FRAME_HEIGHT * s,
                             0.86 * FRAME_WIDTH * s, 0.83 * FRAME_HEIGHT * s, 10 * s, panel)
    return canvas.downsample(s)


def draw_product_logo():
    s = ANTIALIAS_SCALE
    canvas = Canvas(240 * s, 160 * s)
    left_page = hex_color("#FFFFFF")
    right_page = hex_color("#F4ECD6")
    spine = hex_color("#7A4524")
    heart = hex_color("#E53935")
    # Livro aberto visto de frente
    canvas.fill_polygon([(16 * s, 40 * s), (118 * s, 56 * s), (118 * s, 150 * s), (16 * s, 134 * s)], left_page)
    canvas.fill_polygon([(122 * s, 56 * s), (224 * s, 40 * s), (224 * s, 134 * s), (122 * s, 150 * s)], right_page)
    canvas.fill_rect(117 * s, 54 * s, 123 * s, 152 * s, spine)
    # Coração acima do livro
    canvas.fill_ellipse(108 * s, 24 * s, 13 * s, 13 * s, heart)
    canvas.fill_ellipse(132 * s, 24 * s, 13 * s, 13 * s, heart)
    canvas.fill_polygon([(96 * s, 29 * s), (144 * s, 29 * s), (120 * s, 54 * s)], heart)
    return canvas.downsample(s)


def draw_launcher_banner():
    """Banner 320x180 do launcher na lista de apps do Google TV."""
    s = ANTIALIAS_SCALE
    canvas = Canvas(320 * s, 180 * s)
    canvas.fill_rounded_rect(0, 0, 320 * s - 1, 180 * s - 1, 18 * s, hex_color("#FFF6E0"))
    canvas.fill_rounded_rect(8 * s, 8 * s, 312 * s, 172 * s, 14 * s, hex_color("#8FD3FF"))
    canvas.fill_ellipse(160 * s, 230 * s, 230 * s, 110 * s, hex_color("#81C784"))
    # Livro aberto com coração, o mesmo desenho do logotipo do produto
    canvas.fill_polygon([(84 * s, 66 * s), (158 * s, 78 * s), (158 * s, 146 * s), (84 * s, 134 * s)], hex_color("#FFFFFF"))
    canvas.fill_polygon([(162 * s, 78 * s), (236 * s, 66 * s), (236 * s, 134 * s), (162 * s, 146 * s)], hex_color("#F4ECD6"))
    canvas.fill_rect(157 * s, 76 * s, 163 * s, 148 * s, hex_color("#7A4524"))
    canvas.fill_ellipse(150 * s, 42 * s, 11 * s, 11 * s, hex_color("#E53935"))
    canvas.fill_ellipse(170 * s, 42 * s, 11 * s, 11 * s, hex_color("#E53935"))
    canvas.fill_polygon([(140 * s, 46 * s), (180 * s, 46 * s), (160 * s, 68 * s)], hex_color("#E53935"))
    return canvas.downsample(s)


def draw_kids_background():
    canvas = Canvas(1920, 1080)
    canvas.fill_vertical_gradient(hex_color("#8FD3FF"), hex_color("#FFF6E0"))
    canvas.fill_ellipse(1700, 170, 110, 110, hex_color("#FFD54F"))
    for radius, color in [(520, "#E53935"), (490, "#FB8C00"), (460, "#FDD835"),
                          (430, "#43A047"), (400, "#1E88E5"), (370, "#8E24AA")]:
        canvas.fill_ring(330, 900, radius - 30, radius, hex_color(color, 150), clip=lambda x, y: y <= 900)
    for cx, cy in [(900, 160), (1260, 260)]:
        for dx, dy, r in [(-70, 10, 55), (0, -20, 75), (75, 10, 55), (0, 30, 50)]:
            canvas.fill_ellipse(cx + dx, cy + dy, r * 1.3, r, hex_color("#FFFFFF", 230))
    canvas.fill_ellipse(500, 1180, 900, 260, hex_color("#81C784"))
    canvas.fill_ellipse(1500, 1200, 900, 280, hex_color("#66BB6A"))
    return canvas


def draw_burger_background():
    canvas = Canvas(1920, 1080)
    canvas.fill_vertical_gradient(hex_color("#FFB74D"), hex_color("#FFE8CC"))
    for index in range(12):
        x = 80 + index * 170
        canvas.fill_ellipse(x, 1040, 70, 70, hex_color("#FFFFFF", 70))
    return canvas


def main():
    bible_frame = draw_bible_frame()
    burger_frame = draw_burger_frame()
    product_logo = draw_product_logo()
    kids_background = draw_kids_background()

    bible_frame.save_png(DEFAULT_THEME_DIR / "frame-biblia.png")
    product_logo.save_png(DEFAULT_THEME_DIR / "logo.png")
    kids_background.save_png(DEFAULT_THEME_DIR / "fundo.png")

    bible_frame.save_png(BIBLE_SAMPLE_DIR / "frame-biblia.png")
    product_logo.save_png(BIBLE_SAMPLE_DIR / "logo.png")
    kids_background.save_png(BIBLE_SAMPLE_DIR / "fundo.png")

    burger_frame.save_png(BURGER_SAMPLE_DIR / "frame-hamburguer.png")
    product_logo.save_png(BURGER_SAMPLE_DIR / "logo.png")
    draw_burger_background().save_png(BURGER_SAMPLE_DIR / "fundo.png")

    draw_launcher_banner().save_png(LAUNCHER_BANNER_FILE)


if __name__ == "__main__":
    main()
