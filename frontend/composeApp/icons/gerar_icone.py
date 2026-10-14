"""
Rasterizes the Solaris app symbol (design/solaris-handoff_v2/solaris-handoff_2/
assets/icone-app-108.svg geometry, reproduced by hand since no SVG
rasterizer was available when this was first generated) into the icon
formats Compose Desktop's nativeDistributions needs: .ico (Windows),
.icns (macOS), .png (Linux). Run from anywhere; writes next to this file.

Requires Pillow: pip install pillow
"""
from PIL import Image, ImageDraw
import os

DEEP_OCEAN = (0x17, 0x26, 0x21, 255)
GOLD = (0xB8, 0xA4, 0x7B, 255)
GOLD_60 = (0xB8, 0xA4, 0x7B, 153)  # 0.6 alpha flattened onto opaque bg below
PAPER = (0xE8, 0xE5, 0xD8, 255)

OUT_DIR = os.path.dirname(os.path.abspath(__file__))


def flatten(rgba, bg=DEEP_OCEAN):
    a = rgba[3] / 255.0
    return tuple(int(rgba[i] * a + bg[i] * (1 - a)) for i in range(3)) + (255,)


def draw_symbol(size: int) -> Image.Image:
    # Supersample 4x then downscale for clean anti-aliased edges.
    ss = 4
    s = size * ss
    img = Image.new("RGBA", (s, s), DEEP_OCEAN)
    draw = ImageDraw.Draw(img)

    # 108x108 base canvas; symbol group is translate(18,18) scale(1.125) of
    # the 64x64 geometry (circle cx32,cy24,r12; horizon y40; bars).
    k = s / 108.0
    ox, oy, sc = 18 * k, 18 * k, 1.125 * k

    def pt(x, y):
        return (ox + x * sc, oy + y * sc)

    # Planet.
    cx, cy, r = pt(32, 24)[0], pt(32, 24)[1], 12 * sc
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=GOLD)

    # Horizon line, stroke-width 2.5, square cap -> drawn as a filled rect.
    x1, y = pt(8, 40)
    x2, _ = pt(56, 40)
    hw = 2.5 * sc / 2
    draw.rectangle([x1, y - hw, x2, y + hw], fill=PAPER)

    # Reflection bars, gold at 0.6 alpha over Deep Ocean.
    bar_color = flatten(GOLD_60)
    bx1, by1 = pt(21, 45)
    bx2, by2 = pt(43, 48)
    draw.rectangle([bx1, by1, bx2, by2], fill=bar_color)
    bx1, by1 = pt(27, 51)
    bx2, by2 = pt(37, 54)
    draw.rectangle([bx1, by1, bx2, by2], fill=bar_color)

    return img.resize((size, size), Image.LANCZOS)


os.makedirs(OUT_DIR, exist_ok=True)

# ICO: Windows wants a handful of sizes bundled together.
ico_sizes = [16, 24, 32, 48, 64, 128, 256]
base = draw_symbol(256)
imgs = [draw_symbol(sz) for sz in ico_sizes]
base.save(
    os.path.join(OUT_DIR, "icon.ico"),
    format="ICO",
    sizes=[(sz, sz) for sz in ico_sizes],
    append_images=imgs[1:],
)
print("wrote icon.ico")

# PNG: Linux.
draw_symbol(512).save(os.path.join(OUT_DIR, "icon.png"), format="PNG")
print("wrote icon.png")

# ICNS: macOS. Pillow needs specific sizes present; feed it the full set via a temp master.
try:
    master = draw_symbol(1024)
    master.save(os.path.join(OUT_DIR, "icon.icns"), format="ICNS")
    print("wrote icon.icns")
except Exception as e:
    print("icns failed:", e)
