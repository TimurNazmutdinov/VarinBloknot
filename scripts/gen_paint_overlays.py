"""Готовит файлы раскраски для предметов без готового *_tint от художника.

Для каждого предмета берётся слой, по цвету (HSV) вырезается нужная часть (монетки, тесто,
карандаши остаются нетронутыми), переводится в светлый серый и сохраняется как
content/art/<сцена>/layers/<id>_paint.webp. Приложение рисует его поверх слоя с
ColorFilter.tint(цвет, Modulate) — так же, как *_tint художника.

Запуск (Python 3 + Pillow + numpy):  python scripts/gen_paint_overlays.py
"""
import os
import sys
import numpy as np
from PIL import Image, ImageFilter

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'content', 'art')

# id, сцена, слой, оттенки [(hue 0..255, допуск)], sat_min 0..255, белое?, область (доли слоя)
OBJECTS = [
    ('lamp1', 'scene1_room', 'lamp', [(106, 20)], 40, False, {}),
    ('piggy', 'scene2_desk', 'piggy', [(246, 14)], 25, False, {}),
    ('cup', 'scene2_desk', 'cup', [(124, 14)], 40, True, dict(ymin=.42)),
    ('lamp2', 'scene2_desk', 'lamp', [(106, 20)], 40, False, {}),
    ('table', 'scene3_kitchen', 'table', [(0, 10), (250, 10)], 25, True, dict(ymax=.72)),
    ('kettle', 'scene3_kitchen', 'kettle', [(0, 10), (250, 10)], 40, True, dict(xmax=.5)),
    ('pot', 'scene3_kitchen', 'kettle', [(147, 16)], 20, False, dict(xmin=.45)),
    ('bowl', 'scene3_kitchen', 'bowl', [(147, 16)], 40, True, dict(ymin=.55)),
    ('stools', 'scene3_kitchen', 'stools', [(19, 10)], 25, False, {}),
    ('lamp3', 'scene3_kitchen', 'lamp', [(106, 20)], 40, False, {}),
]


def mask_for(img, hues, sat_min, white, box):
    hsv = np.array(img.convert('RGB').convert('HSV')).astype(int)
    a = np.array(img)[..., 3]
    H, S, V = hsv[..., 0], hsv[..., 1], hsv[..., 2]
    m = np.zeros(a.shape, bool)
    for hc, tol in hues:
        d = np.minimum(abs(H - hc), 256 - abs(H - hc))
        m |= (d <= tol) & (S >= sat_min)
    if white:
        m |= (S < 40) & (V > 205)
    h, w = a.shape
    yy, xx = np.mgrid[0:h, 0:w]
    m &= (yy >= box.get('ymin', 0) * h) & (yy <= box.get('ymax', 1) * h)
    m &= (xx >= box.get('xmin', 0) * w) & (xx <= box.get('xmax', 1) * w) & (a > 0)
    return m


def overlay(img, mask):
    a = np.array(img).astype(float)
    lum = 0.299 * a[..., 0] + 0.587 * a[..., 1] + 0.114 * a[..., 2]
    sel = a[..., 3] > 128
    hi = np.percentile(lum[sel], 92) if sel.any() else 255
    g = (np.clip(lum / max(hi, 1), 0, 1) ** 0.8 * 255).astype('uint8')
    soft = Image.fromarray((mask * 255).astype('uint8')).filter(ImageFilter.GaussianBlur(0.7))
    alpha = np.minimum(a[..., 3], np.array(soft).astype(float)).astype('uint8')
    return Image.fromarray(np.dstack([g, g, g, alpha]), 'RGBA')


def main():
    for oid, sc, layer, hues, sat_min, white, box in OBJECTS:
        src = Image.open(os.path.join(ROOT, sc, 'layers', layer + '.webp')).convert('RGBA')
        m = mask_for(src, hues, sat_min, white, box)
        out = overlay(src, m)
        dst = os.path.join(ROOT, sc, 'layers', oid + '_paint.webp')
        out.save(dst, 'WEBP', quality=92, method=6)
        print(oid, sc, layer, 'пикселей:', int(m.sum()), '(%.0f%% слоя)' % (100 * m.sum() / max(1, (np.array(src)[..., 3] > 0).sum())))


if __name__ == '__main__':
    sys.exit(main())
