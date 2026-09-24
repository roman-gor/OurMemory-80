import argparse
import io
from pathlib import Path

import qrcode
import requests
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas

DATABASE_URL = "https://chatroom-85fb8-default-rtdb.firebaseio.com/OurMemory"
LINK_BASE = "https://chatroom-85fb8.web.app/veteran"
FONT_DIR = Path(__file__).resolve().parents[2] / "app" / "src" / "main" / "res" / "font"
REGULAR_FONT = "Mulish"
BOLD_FONT = "Mulish-Bold"
CARD_WIDTH = 65 * mm
CARD_HEIGHT = 85 * mm
QR_SIZE = 52 * mm
PADDING = 6 * mm
NAME_FONT_SIZE = 10
PLOT_FONT_SIZE = 8
LINE_GAP = 4.5 * mm


def load(node):
    response = requests.get(f"{DATABASE_URL}/{node}.json", timeout=30)
    response.raise_for_status()
    return list((response.json() or {}).values())


def qr_image(url):
    buffer = io.BytesIO()
    qrcode.make(url, box_size=10, border=2).save(buffer, format="PNG")
    buffer.seek(0)
    return ImageReader(buffer)


def plot_label(burial):
    if not burial or not all(burial.get(key) for key in ("section", "row", "place")):
        return ""
    return f"Участок {burial['section']} · ряд {burial['row']} · место {burial['place']}"


def wrap(pdf, text, font, size, width):
    lines, current = [], ""
    for word in text.split():
        candidate = f"{current} {word}".strip()
        if pdf.stringWidth(candidate, font, size) <= width:
            current = candidate
        else:
            lines.append(current)
            current = word
    return lines + [current] if current else lines


def draw_card(pdf, x, y, veteran, burial):
    pdf.setStrokeColorRGB(0.85, 0.85, 0.85)
    pdf.rect(x, y, CARD_WIDTH, CARD_HEIGHT)
    qr_x = x + (CARD_WIDTH - QR_SIZE) / 2
    qr_y = y + CARD_HEIGHT - PADDING - QR_SIZE
    pdf.drawImage(qr_image(f"{LINK_BASE}/{veteran['id']}"), qr_x, qr_y, QR_SIZE, QR_SIZE)
    text_y = qr_y - LINE_GAP
    for line in wrap(pdf, veteran.get("name", ""), BOLD_FONT, NAME_FONT_SIZE, CARD_WIDTH - 2 * PADDING):
        pdf.setFont(BOLD_FONT, NAME_FONT_SIZE)
        pdf.drawCentredString(x + CARD_WIDTH / 2, text_y, line)
        text_y -= LINE_GAP
    plot = plot_label(burial)
    if plot:
        pdf.setFont(REGULAR_FONT, PLOT_FONT_SIZE)
        pdf.drawCentredString(x + CARD_WIDTH / 2, text_y, plot)


def main():
    parser = argparse.ArgumentParser(description="Generate a printable PDF sheet of veteran QR codes")
    parser.add_argument("--output", default="qr_sheet.pdf")
    args = parser.parse_args()

    pdfmetrics.registerFont(TTFont(REGULAR_FONT, FONT_DIR / "mulish_medium.ttf"))
    pdfmetrics.registerFont(TTFont(BOLD_FONT, FONT_DIR / "mulish_bold.ttf"))

    burials = {burial["id"]: burial for burial in load("Burials")}
    veterans = sorted(load("Veterans"), key=lambda veteran: veteran.get("name", ""))

    pdf = canvas.Canvas(args.output, pagesize=A4)
    page_width, page_height = A4
    columns = int(page_width // CARD_WIDTH)
    rows = int(page_height // CARD_HEIGHT)
    margin_x = (page_width - columns * CARD_WIDTH) / 2
    margin_y = (page_height - rows * CARD_HEIGHT) / 2
    for index, veteran in enumerate(veterans):
        slot = index % (columns * rows)
        if index and slot == 0:
            pdf.showPage()
        x = margin_x + (slot % columns) * CARD_WIDTH
        y = page_height - margin_y - (slot // columns + 1) * CARD_HEIGHT
        draw_card(pdf, x, y, veteran, burials.get(veteran.get("burialId", "")))
    pdf.save()
    print(f"{len(veterans)} cards written to {args.output}")


if __name__ == "__main__":
    main()
