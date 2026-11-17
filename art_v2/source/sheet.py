import chars as C
keys=list(C.CHARS)+['cat']
W=1500;H=560
items=[]
for i,k in enumerate(keys):
    x=90+i*165;y=500
    g=C.build(k) if k!='cat' else C.cat_flat()
    sc=.92 if k!='cat' else 1.1
    name=C.CHARS[k]['name'] if k!='cat' else 'Тимоша'
    items.append(f'<g transform="translate({x} {y}) scale({sc})">{g}</g><text x="{x}" y="{y+42}" text-anchor="middle" font-family="sans-serif" font-size="20" font-weight="700" fill="#5a3d4a">{name}</text>')
svg=f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}"><defs>{C.PATS}</defs><rect width="{W}" height="{H}" fill="#fff6ec"/>{"".join(items)}</svg>'
open('sheet.svg','w').write(svg)
