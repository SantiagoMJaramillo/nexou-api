"""Genera las ilustraciones planas de los artículos del seed (sin degradados, paleta de EduLoan).

Uso (desde la raíz del repositorio):  python scripts/generar_imagenes_demo.py
Requiere Pillow:                       pip install pillow
Salida:                                src/main/resources/seed-images/*.jpg
"""
import os
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

SALIDA = str(Path(__file__).resolve().parent.parent / "src" / "main" / "resources" / "seed-images")
os.makedirs(SALIDA, exist_ok=True)

ANCHO, ALTO, S = 900, 675, 2  # se dibuja al doble y se reduce: bordes suaves
FONDO = "#eceae6"
SUELO = "#dedbd5"
TINTA = "#18181b"
TEAL = "#0f766e"
TEAL_OSCURO = "#115e59"
TEAL_CLARO = "#a7d8d3"
GRIS_1 = "#d4d4d8"
GRIS_2 = "#a1a1aa"
GRIS_3 = "#52525b"
BLANCO = "#fafaf9"


def _buscar_fuente(candidatas):
    for ruta in candidatas:
        if os.path.exists(ruta):
            return ruta
    raise SystemExit("No se encontró una fuente TrueType (Arial/DejaVu). Instala una o ajusta FUENTE_NEGRITA en el script.")


FUENTE_NEGRITA = _buscar_fuente([
    r"C:\Windows\Fonts\arialbd.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    "/Library/Fonts/Arial Bold.ttf",
    "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
])


def lienzo():
    img = Image.new("RGB", (ANCHO * S, ALTO * S), FONDO)
    return img, ImageDraw.Draw(img)


def r(*valores):
    """Escala coordenadas del lienzo base al doble."""
    return [v * S for v in valores]


def suelo(d, cx=450, y=585, ancho=520):
    d.ellipse(r(cx - ancho / 2, y - 18, cx + ancho / 2, y + 18), fill=SUELO)


def guardar(img, nombre):
    img.resize((ANCHO, ALTO), Image.LANCZOS).save(os.path.join(SALIDA, nombre), "JPEG", quality=88, optimize=True)
    print("ok", nombre)


def texto_multilinea(d, texto, x, y, ancho_max, fuente, color, interlineado=1.18):
    palabras, lineas, actual = texto.split(), [], ""
    for p in palabras:
        prueba = f"{actual} {p}".strip()
        if d.textlength(prueba, font=fuente) <= ancho_max * S:
            actual = prueba
        else:
            lineas.append(actual)
            actual = p
    lineas.append(actual)
    for i, linea in enumerate(lineas):
        d.text((x * S, y * S + i * fuente.size * interlineado), linea, font=fuente, fill=color)
    return len(lineas) * fuente.size * interlineado / S


# ---------------------------------------------------------------- libros
def portada(nombre_archivo, titulo, color_tapa, color_lomo, color_texto, motivo):
    img, d = lienzo()
    suelo(d, 450, 590, 420)
    x0, y0, x1, y1 = 300, 70, 600, 570  # tapa 300x500
    d.rounded_rectangle(r(x0 + 10, y0 + 12, x1 + 10, y1 + 12), radius=14 * S, fill=SUELO)  # sombra plana
    d.rounded_rectangle(r(x0, y0, x1, y1), radius=10 * S, fill=color_tapa)
    d.rounded_rectangle(r(x0, y0, x0 + 26, y1), radius=10 * S, fill=color_lomo)
    d.rectangle(r(x0 + 16, y0, x0 + 26, y1), fill=color_lomo)
    fuente = ImageFont.truetype(FUENTE_NEGRITA, 34 * S)
    texto_multilinea(d, titulo, x0 + 54, y0 + 46, 210, fuente, color_texto)
    d.rectangle(r(x0 + 54, y0 + 40, x0 + 94, y0 + 44), fill=color_texto)
    motivo(d, x0, y0, x1, y1)
    guardar(img, nombre_archivo)


def motivo_codigo(d, x0, y0, x1, y1):
    base_y, x = y1 - 190, x0 + 54
    for i, (sangria, largo) in enumerate([(0, 120), (24, 150), (24, 96), (48, 130), (24, 70), (0, 100)]):
        d.rounded_rectangle(r(x + sangria, base_y + i * 26, x + sangria + largo, base_y + i * 26 + 10), radius=5 * S, fill=TEAL_CLARO if i % 3 else BLANCO)


def motivo_patrones(d, x0, y0, x1, y1):
    cy, x = y1 - 130, x0 + 60
    d.ellipse(r(x, cy - 34, x + 68, cy + 34), outline=TEAL_CLARO, width=5 * S)
    d.rectangle(r(x + 84, cy - 34, x + 152, cy + 34), outline=BLANCO, width=5 * S)
    d.polygon(r(x + 168, cy + 34, x + 202, cy - 34, x + 236, cy + 34), outline=TEAL_CLARO, width=5 * S)
    d.line(r(x + 34, cy + 56, x + 202, cy + 56), fill=GRIS_2, width=3 * S)


def motivo_bd(d, x0, y0, x1, y1):
    cx, base = (x0 + x1) / 2 + 12, y1 - 70
    for nivel in range(3):
        yb = base - nivel * 46
        d.rectangle(r(cx - 62, yb - 40, cx + 62, yb), fill=TINTA)
        d.ellipse(r(cx - 62, yb - 56, cx + 62, yb - 24), fill=TINTA)
        d.ellipse(r(cx - 62, yb - 16, cx + 62, yb + 16), fill=TINTA)
        d.ellipse(r(cx - 62, yb - 56, cx + 62, yb - 24), outline=GRIS_1, width=3 * S)
        d.arc(r(cx - 62, yb - 16, cx + 62, yb + 16), 0, 180, fill=GRIS_1, width=3 * S)


def motivo_redes(d, x0, y0, x1, y1):
    cx, cy = (x0 + x1) / 2 + 12, y1 - 130
    nodos = [(cx, cy - 44), (cx - 80, cy + 22), (cx + 80, cy + 22), (cx, cy + 60)]
    for a, b in [(0, 1), (0, 2), (1, 3), (2, 3), (1, 2), (0, 3)]:
        d.line(r(*nodos[a], *nodos[b]), fill=TEAL_CLARO, width=3 * S)
    for i, (nx, ny) in enumerate(nodos):
        rad = 17 if i == 0 else 12
        d.ellipse(r(nx - rad, ny - rad, nx + rad, ny + rad), fill=BLANCO if i == 0 else TEAL_CLARO)


def motivo_seguridad(d, x0, y0, x1, y1):
    cx, cy = (x0 + x1) / 2 + 12, y1 - 140
    escudo = [(cx - 62, cy - 64), (cx + 62, cy - 64), (cx + 62, cy + 6), (cx, cy + 74), (cx - 62, cy + 6)]
    d.polygon(r(*[c for p in escudo for c in p]), fill=TEAL)
    d.ellipse(r(cx - 17, cy - 30, cx + 17, cy + 4), fill=BLANCO)
    d.polygon(r(cx - 8, cy, cx + 8, cy, cx + 14, cy + 40, cx - 14, cy + 40), fill=BLANCO)
    d.ellipse(r(cx - 7, cy - 22, cx + 7, cy - 8), fill=TEAL)


# ---------------------------------------------------------------- equipos
def portatil():
    img, d = lienzo()
    suelo(d, 450, 520, 640)
    d.rounded_rectangle(r(210, 130, 690, 430), radius=18 * S, fill=TINTA)           # tapa
    d.rounded_rectangle(r(228, 148, 672, 412), radius=8 * S, fill=BLANCO)           # pantalla
    d.rectangle(r(228, 148, 672, 176), fill=GRIS_1)                                   # barra de ventana
    for x in (246, 266, 286):
        d.ellipse(r(x, 156, x + 11, 167), fill=GRIS_2)
    for i, (_, alto) in enumerate([(90, 120), (90, 170), (90, 90), (90, 140)]):      # gráfico de barras
        x = 262 + i * 112
        d.rectangle(r(x, 392 - alto, x + 70, 392), fill=TEAL if i % 2 == 0 else TEAL_CLARO)
    d.rectangle(r(520, 200, 650, 210), fill=GRIS_1)
    d.rectangle(r(520, 224, 620, 234), fill=GRIS_1)
    d.rectangle(r(520, 248, 636, 258), fill=GRIS_1)
    d.polygon(r(150, 440, 750, 440, 800, 492, 100, 492), fill=GRIS_2)                # base
    d.rounded_rectangle(r(100, 486, 800, 502), radius=8 * S, fill=GRIS_3)
    d.rounded_rectangle(r(390, 458, 510, 472), radius=7 * S, fill=GRIS_1)            # trackpad
    guardar(img, "portatil-thinkpad-e14.jpg")


def proyector():
    img, d = lienzo()
    suelo(d, 330, 500, 400)
    d.polygon(r(480, 330, 880, 150, 880, 520, 480, 380), fill=TEAL_CLARO)           # haz de luz plano
    d.rectangle(r(836, 130, 852, 540), fill=GRIS_3)                                  # pantalla (borde)
    d.rectangle(r(852, 150, 880, 520), fill=BLANCO)
    d.rounded_rectangle(r(110, 270, 500, 470), radius=22 * S, fill=TINTA)            # cuerpo
    d.rounded_rectangle(r(140, 296, 300, 330), radius=8 * S, fill=GRIS_3)            # rejilla
    for i in range(5):
        d.rectangle(r(150 + i * 28, 304, 160 + i * 28, 322), fill=TINTA)
    d.ellipse(r(380, 306, 500, 426), fill=GRIS_3)                                     # lente
    d.ellipse(r(398, 324, 482, 408), fill=TEAL)
    d.ellipse(r(418, 344, 462, 388), fill=TEAL_CLARO)
    d.ellipse(r(126, 400, 140, 414), fill=TEAL)                                       # luz de encendido
    d.rectangle(r(150, 470, 180, 490), fill=GRIS_3)
    d.rectangle(r(430, 470, 460, 490), fill=GRIS_3)
    guardar(img, "proyector-powerlite.jpg")


def tablet():
    img, d = lienzo()
    suelo(d, 450, 540, 640)
    d.rounded_rectangle(r(180, 110, 720, 520), radius=34 * S, fill=TINTA)            # marco
    d.rounded_rectangle(r(204, 134, 696, 496), radius=14 * S, fill=BLANCO)           # pantalla
    d.ellipse(r(190, 306, 200, 316), fill=GRIS_3)                                     # cámara frontal
    d.rectangle(r(204, 134, 696, 168), fill=GRIS_1)
    d.rounded_rectangle(r(228, 190, 428, 330), radius=8 * S, fill=TEAL)               # tarjeta grande
    d.rounded_rectangle(r(444, 190, 672, 250), radius=8 * S, fill=GRIS_1)
    d.rounded_rectangle(r(444, 266, 672, 330), radius=8 * S, fill=TEAL_CLARO)
    for i in range(3):
        d.rounded_rectangle(r(228 + i * 150, 352, 228 + i * 150 + 134, 456), radius=8 * S, fill=GRIS_1 if i != 1 else TEAL_CLARO)
    guardar(img, "tablet-galaxy-tab.jpg")


if __name__ == "__main__":
    portada("codigo-limpio.jpg", "Código limpio", TEAL, TEAL_OSCURO, BLANCO, motivo_codigo)
    portada("patrones-de-diseno.jpg", "Patrones de diseño", TINTA, "#000000", BLANCO, motivo_patrones)
    portada("fundamentos-bases-de-datos.jpg", "Fundamentos de bases de datos", GRIS_1, GRIS_2, TINTA, motivo_bd)
    portada("redes-de-computadoras.jpg", "Redes de computadoras", TEAL_OSCURO, "#0b4a45", BLANCO, motivo_redes)
    portada("sql-y-seguridad.jpg", "SQL y seguridad de datos", GRIS_3, "#3f3f46", BLANCO, motivo_seguridad)
    portatil()
    proyector()
    tablet()
