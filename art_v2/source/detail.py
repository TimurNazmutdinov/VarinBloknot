import math, random
E = '#5a3d2a'

def sh(c, f):
    c = c.lstrip('#'); r = [int(c[i:i+2], 16) for i in (0, 2, 4)]
    return '#' + ''.join('%02x' % max(0, min(255, round(v*(1+f) if f < 0 else v+(255-v)*f))) for v in r)

_gid = [0]
GDEFS = []
def rg(col, cx=.35, cy=.3):
    _gid[0] += 1; i = f'dg{_gid[0]}'
    GDEFS.append(f'<radialGradient id="{i}" cx="{cx}" cy="{cy}" r=".8"><stop offset="0" stop-color="{sh(col,.3)}"/><stop offset=".6" stop-color="{col}"/><stop offset="1" stop-color="{sh(col,-.25)}"/></radialGradient>')
    return f'url(#{i})'
def lg(col, a=.18, b=-.18, vert=True):
    _gid[0] += 1; i = f'dg{_gid[0]}'
    GDEFS.append(f'<linearGradient id="{i}" x1="0" y1="0" x2="{0 if vert else 1}" y2="{1 if vert else 0}"><stop offset="0" stop-color="{sh(col,a)}"/><stop offset="1" stop-color="{sh(col,b)}"/></linearGradient>')
    return f'url(#{i})'

def place(svg, x, y, s):
    return f'<g transform="translate({x:.1f} {y:.1f}) scale({s:.4f})">{svg}</g>'

# ---------- geranium (pot base at 0,0; ~ 70 px tall) ----------
def leaf(cx, cy, r, rot, rnd):
    pts = []
    for k in range(48):
        a = k / 48 * 2 * math.pi
        rr = r * (1 + .1 * math.sin(7 * a)) * (0.92 if abs(math.sin((a - 1.57) / 2)) < .12 else 1)
        pts.append((cx + rr * math.cos(a + rot), cy + rr * .82 * math.sin(a + rot)))
    d = 'M' + ' L'.join(f'{x:.1f} {y:.1f}' for x, y in pts) + 'Z'
    g = rnd.choice(['#6f9a52', '#7aa65a', '#668f4a'])
    o = f'<path d="{d}" fill="{rg(g,.4,.35)}" stroke="{sh(g,-.4)}" stroke-width=".9"/>'
    # dark horseshoe zone
    pts2 = [(cx + r*.58*math.cos(a+rot), cy + r*.58*.82*math.sin(a+rot)) for a in [k/30*2*math.pi for k in range(31)]]
    o += '<path d="M' + ' L'.join(f'{x:.1f} {y:.1f}' for x, y in pts2) + f'" fill="none" stroke="#3f6a32" stroke-width="{r*.22:.1f}" opacity=".35"/>'
    for k in range(6):
        a = k / 6 * 2 * math.pi + rot
        o += f'<line x1="{cx:.1f}" y1="{cy:.1f}" x2="{cx + r*.85*math.cos(a):.1f}" y2="{cy + r*.85*.82*math.sin(a):.1f}" stroke="#4f7a3a" stroke-width=".6" opacity=".6"/>'
    return o

def umbel(cx, cy, rnd, col):
    o = ''
    for k in range(13):
        a = rnd.uniform(0, 2*math.pi); d = rnd.uniform(0, 8)
        x, y = cx + d*math.cos(a), cy + d*.8*math.sin(a) - (8 - d) * .5
        c = sh(col, rnd.uniform(-.12, .15))
        for p in range(5):
            pa = p / 5 * 2 * math.pi + rnd.uniform(0, 1)
            o += f'<ellipse cx="{x + 2.3*math.cos(pa):.1f}" cy="{y + 2.3*math.sin(pa):.1f}" rx="2.2" ry="1.6" fill="{c}" stroke="{sh(col,-.45)}" stroke-width=".35" transform="rotate({math.degrees(pa):.0f} {x + 2.3*math.cos(pa):.1f} {y + 2.3*math.sin(pa):.1f})"/>'
        o += f'<circle cx="{x:.1f}" cy="{y:.1f}" r=".9" fill="#f7e0a0"/>'
    for k in range(3):
        o += f'<ellipse cx="{cx + rnd.uniform(-6,6):.1f}" cy="{cy + 6 + rnd.uniform(0,3):.1f}" rx="1.4" ry="2.2" fill="#8fae5a"/>'
    return o

def geranium(seed, col='#d93f4c'):
    rnd = random.Random(seed)
    o = ''
    tops = [(-11, -62), (10, -66), (0, -76)]
    for x, y in tops:
        o += f'<path d="M0 -30 Q{x*.4:.1f} {(y-30)/2:.1f} {x} {y+6}" fill="none" stroke="#5f8a45" stroke-width="1.6"/>'
    lv = [(-16, -34, 11, .2), (15, -36, 11, -.3), (-6, -44, 10, .5), (8, -46, 10, -.6), (0, -36, 12, 0), (-20, -26, 9, .9), (20, -27, 9, -.9)]
    for x, y, r, rot in lv:
        o += leaf(x, y, r, rot, rnd)
    for x, y in tops:
        o += umbel(x, y, rnd, col)
    # pot
    o += f'<path d="M-17 -30 H17 L13 0 H-13Z" fill="{lg("#c9673c",.12,-.22,False)}" stroke="#7a3a1c" stroke-width="1"/>'
    o += f'<rect x="-19" y="-33" width="38" height="7" rx="1.5" fill="{lg("#d4774a")}" stroke="#7a3a1c" stroke-width="1"/>'
    o += '<path d="M-11 -24 L-9 -4" stroke="#f0b08a" stroke-width="2" opacity=".6"/>'
    o += '<ellipse cx="0" cy="1" rx="18" ry="2.6" fill="#b8562c" stroke="#7a3a1c" stroke-width=".8"/>'
    return o

# ---------- yarn ball ----------
def yarn(r, col, seed):
    rnd = random.Random(seed); _gid[0] += 1; cid = f'yc{_gid[0]}'
    GDEFS.append(f'<clipPath id="{cid}"><circle r="{r}"/></clipPath>')
    o = f'<circle r="{r}" fill="{rg(col)}" stroke="{sh(col,-.45)}" stroke-width="1"/>'
    o += f'<g clip-path="url(#{cid})">'
    for k in range(9):
        rot = rnd.uniform(0, 180); rx = rnd.uniform(r*.4, r*1.05)
        c = sh(col, rnd.choice([-.28, -.18, .22]))
        o += f'<ellipse rx="{rx:.1f}" ry="{r*1.1:.1f}" fill="none" stroke="{c}" stroke-width="{r*.06:.1f}" transform="rotate({rot:.0f})"/>'
    o += '</g>'
    o += f'<path d="M{r*.6:.1f} {r*.7:.1f} q{r*.6:.1f} {r*.3:.1f} {r*.3:.1f} {r*.9:.1f} t{r*.5:.1f} {r*.4:.1f}" fill="none" stroke="{sh(col,-.1)}" stroke-width="{r*.07:.1f}"/>'
    return o

# ---------- wicker basket with yarn (base center 0,0; width 90) ----------
def basket():
    o = ''
    # back yarns
    o += place(yarn(14, '#5f7f9a', 2), -14, -46, 1) + place(yarn(13, '#e2b85a', 3), 16, -48, 1)
    o += place(yarn(15, '#c0504a', 1), 2, -40, 1)
    o += '<line x1="-6" y1="-42" x2="-30" y2="-78" stroke="#8a6a4a" stroke-width="2.4"/><circle cx="-30" cy="-79" r="2.6" fill="#c99a45"/>'
    o += '<line x1="2" y1="-42" x2="-18" y2="-82" stroke="#8a6a4a" stroke-width="2.4"/><circle cx="-18" cy="-83" r="2.6" fill="#c99a45"/>'
    body = 'M-45 -38 H45 L37 0 H-37Z'
    o += f'<path d="{body}" fill="{lg("#c4914f",.12,-.25)}" stroke="#6e4a26" stroke-width="1.1"/>'
    _gid[0] += 1; cid = f'bc{_gid[0]}'
    GDEFS.append(f'<clipPath id="{cid}"><path d="{body}"/></clipPath>')
    o += f'<g clip-path="url(#{cid})">'
    for row, y in enumerate(range(-36, 0, 6)):
        for x in range(-48, 48, 10):
            xo = x + (5 if row % 2 else 0)
            o += f'<path d="M{xo} {y+3} q5 -3.5 10 0" fill="none" stroke="#e2bd85" stroke-width="2.2" opacity=".7"/><path d="M{xo} {y+3} q5 -3.5 10 0" fill="none" stroke="#7a5230" stroke-width=".5"/>'
    for x in range(-40, 42, 10):
        o += f'<line x1="{x}" y1="-38" x2="{x*.82:.1f}" y2="0" stroke="#7a5230" stroke-width=".7" opacity=".7"/>'
    o += '</g>'
    o += '<rect x="-48" y="-43" width="96" height="8" rx="4" fill="#b58040" stroke="#6e4a26" stroke-width="1"/>'
    o += ''.join(f'<path d="M{x} -43 l4 8" stroke="#7a5230" stroke-width="1"/>' for x in range(-46, 46, 5))
    return o

# ---------- clock (ходики), origin = face center ----------
def clock():
    o = ''
    o += '<path d="M0 -62 L36 -38 L36 30 L0 50 L-36 30 L-36 -38Z" fill="%s" stroke="#5e3a1e" stroke-width="1.2"/>' % lg('#a8703f')
    o += '<path d="M-40 -38 L0 -66 L40 -38" fill="none" stroke="#5e3a1e" stroke-width="5" stroke-linecap="round"/>'
    o += '<path d="M-28 30 Q0 44 28 30" fill="none" stroke="#d1a24a" stroke-width="1.6"/>'
    o += '<circle r="25" fill="%s" stroke="#5e3a1e" stroke-width="1.4"/>' % rg('#f6efe0', .4, .35)
    for k in range(12):
        a = k / 12 * 2 * math.pi
        o += f'<line x1="{21*math.sin(a):.1f}" y1="{-21*math.cos(a):.1f}" x2="{18*math.sin(a):.1f}" y2="{-18*math.cos(a):.1f}" stroke="#4a3426" stroke-width="{1.6 if k%3==0 else .8}"/>'
    o += '<path d="M-6 12 q6 -8 12 0 q-6 6 -12 0z" fill="#c9767a"/><path d="M-10 14 q-3 -5 2 -6" stroke="#6f9a52" stroke-width="1.2" fill="none"/>'
    o += '<path d="M0 0 L0 -15 M0 0 L10 5" stroke="#3a2a1e" stroke-width="2.2" stroke-linecap="round"/><circle r="2.2" fill="#3a2a1e"/>'
    # cuckoo door + bird
    o += '<rect x="-7" y="-52" width="14" height="13" rx="2" fill="#6e4228" stroke="#5e3a1e" stroke-width="1"/>'
    # chains + pine-cone weights + pendulum
    for x, h in ((-13, 120), (13, 100)):
        o += f'<line x1="{x}" y1="45" x2="{x}" y2="{45+h}" stroke="#8a7a5a" stroke-width="1.2" stroke-dasharray="2 1.5"/>'
        o += f'<path d="M{x-6} {45+h} q6 -4 12 0 l-1 22 q-5 7 -10 0z" fill="{lg("#6e5a3a")}" stroke="#3a2a1e" stroke-width="1"/>'
        o += ''.join(f'<path d="M{x-5} {48+h+i*5} q5 3 10 0" fill="none" stroke="#3a2a1e" stroke-width=".7"/>' for i in range(4))
    o += '<line x1="0" y1="45" x2="0" y2="88" stroke="#8a7a5a" stroke-width="1.4"/><circle cx="0" cy="94" r="8" fill="%s" stroke="#7a5a1e" stroke-width="1"/>' % rg('#e0b84a')
    return o

# ---------- pointe shoes, origin = nail ----------
def pointes():
    o = '<circle r="2.4" fill="#8a7a5a"/>'
    for dx, rot in ((-8, 8), (9, -6)):
        o += f'<g transform="translate({dx} 18) rotate({rot})">'
        o += f'<path d="M-10 0 C-14 22 -12 48 0 56 C12 48 14 22 10 0 C6 -4 -6 -4 -10 0Z" fill="{lg("#f2c3c8",.2,-.15,False)}" stroke="#c98a94" stroke-width="1"/>'
        o += '<path d="M-7 4 C-9 14 -8 22 0 26 C8 22 9 14 7 4 C3 1 -3 1 -7 4Z" fill="#f6dfe0" stroke="#d9a0a8" stroke-width=".8"/>'
        o += '<path d="M0 50 q-3 4 0 6 q3 -2 0 -6" fill="#e5a8b0"/></g>'
    o += '<path d="M-8 18 C-20 40 -14 70 -24 96 M-8 18 C0 46 -4 72 4 100 M9 18 C20 40 14 68 24 98 M9 18 C2 46 8 72 2 104" fill="none" stroke="#e08aa0" stroke-width="2.2" stroke-linecap="round"/>'
    o += '<path d="M0 0 L-8 18 M0 0 L9 18" stroke="#e08aa0" stroke-width="1.6"/>'
    return o

# ---------- cat sitting (origin = bottom center, ~110 px tall) ----------
def cat():
    F = '#e8954a'
    o = ''
    o += f'<path d="M26 -8 C62 -6 72 -34 58 -50 C54 -55 49 -50 52 -45 C60 -32 54 -18 24 -20Z" fill="{lg(F,.1,-.2,False)}" stroke="#9a5420" stroke-width="1.1"/>'
    o += ''.join(f'<path d="M{x} {y} l6 -3" stroke="#b8642a" stroke-width="3" stroke-linecap="round"/>' for x, y in ((40, -12), (52, -20), (56, -34)))
    o += f'<path d="M-32 0 C-42 -40 -26 -66 0 -66 C26 -66 42 -40 32 0Z" fill="{rg(F,.4,.3)}" stroke="#9a5420" stroke-width="1.1"/>'
    o += ''.join(f'<path d="M{s*30} {y} q{s*-8} 4 {s*-12} 10" fill="none" stroke="#b8642a" stroke-width="3.4" stroke-linecap="round"/>' for s in (-1, 1) for y in (-44, -32, -20))
    o += '<path d="M-14 -4 C-18 -30 -10 -50 0 -52 C10 -50 18 -30 14 -4Z" fill="#fbe9cf"/>'
    o += '<path d="M-8 -46 l3 4 l3 -4 l3 4 l3 -4" fill="none" stroke="#fbe9cf" stroke-width="2"/>'
    for x in (-15, 15):
        o += f'<ellipse cx="{x}" cy="-2" rx="11" ry="6" fill="{F}" stroke="#9a5420" stroke-width="1"/>'
        o += ''.join(f'<line x1="{x+d}" y1="-1" x2="{x+d}" y2="3" stroke="#9a5420" stroke-width=".8"/>' for d in (-4, 0, 4))
    for s in (-1, 1):
        o += f'<path d="M{s*28} -86 L{s*25} -118 L{s*6} -98Z" fill="{F}" stroke="#9a5420" stroke-width="1.1"/>'
        o += f'<path d="M{s*25} -90 L{s*23} -110 L{s*11} -98Z" fill="#f2b1a8"/>'
    o += f'<ellipse cx="0" cy="-88" rx="32" ry="27" fill="{rg(F,.4,.35)}" stroke="#9a5420" stroke-width="1.1"/>'
    o += ''.join(f'<path d="M{x} -114 l{x*.08:.1f} 12" stroke="#b8642a" stroke-width="3" stroke-linecap="round"/>' for x in (-8, 0, 8))
    o += '<ellipse cx="0" cy="-76" rx="13" ry="9" fill="#fbe9cf"/>'
    o += '<path d="M-18 -90 Q-12 -95 -6 -90 M6 -90 Q12 -95 18 -90" fill="none" stroke="#3a2416" stroke-width="2.2" stroke-linecap="round"/>'
    o += '<path d="M-4 -80 h8 l-4 5z" fill="#e07a8a"/><path d="M0 -75 q-4 5 -9 3 M0 -75 q4 5 9 3" fill="none" stroke="#5a3a2a" stroke-width="1.2"/>'
    o += ''.join(f'<line x1="{s*12}" y1="{y}" x2="{s*40}" y2="{y+d}" stroke="#7a5a44" stroke-width=".7"/>' for s in (-1, 1) for y, d in ((-79, -4), (-76, 2)))
    o += '<ellipse cx="-20" cy="-82" rx="5" ry="3" fill="#f0a0a0" opacity=".5"/><ellipse cx="20" cy="-82" rx="5" ry="3" fill="#f0a0a0" opacity=".5"/>'
    return o

# ---------- framed landscape (origin top-left, 150x112) ----------
def landscape():
    o = f'<rect width="150" height="112" fill="{lg("#9c6a3c",.15,-.2,False)}" stroke="#5e3a1e" stroke-width="1.2"/>'
    o += '<rect x="10" y="10" width="130" height="92" fill="#cfe2ea"/>'
    o += '<path d="M10 70 Q50 48 80 62 Q110 44 140 60 V102 H10Z" fill="#9db77f"/><path d="M10 84 Q60 70 140 82 V102 H10Z" fill="#7f9a5f"/>'
    o += '<path d="M10 92 Q70 86 140 94 V102 H10Z" fill="#d9c070"/>'
    o += '<circle cx="112" cy="30" r="9" fill="#f5d77a"/>'
    for x in (34, 46):
        o += f'<line x1="{x}" y1="86" x2="{x+1}" y2="44" stroke="#f3f0e8" stroke-width="3"/><line x1="{x}" y1="80" x2="{x}" y2="76" stroke="#3a2a1e" stroke-width="1.6"/><line x1="{x}" y1="64" x2="{x}" y2="61" stroke="#3a2a1e" stroke-width="1.6"/>'
        o += ''.join(f'<ellipse cx="{x+dx}" cy="{y}" rx="5" ry="3" fill="#a8c46a"/>' for dx, y in ((-6, 50), (5, 46), (0, 56), (7, 58), (-5, 62)))
    o += '<rect x="10" y="10" width="130" height="92" fill="none" stroke="#5e3a1e" stroke-width="1"/>'
    return o

# ---------- patterns for quilt / curtains ----------
def patterns():
    return '''
<pattern id="pDots" width="10" height="10" patternUnits="userSpaceOnUse"><circle cx="5" cy="5" r="1.6" fill="#fff6e6" opacity=".5"/></pattern>
<pattern id="pFlow" width="16" height="16" patternUnits="userSpaceOnUse"><g fill="#fff6e6" opacity=".55"><circle cx="8" cy="5" r="1.6"/><circle cx="5" cy="8" r="1.6"/><circle cx="11" cy="8" r="1.6"/><circle cx="8" cy="11" r="1.6"/></g><circle cx="8" cy="8" r="1.2" fill="#e2b85a"/></pattern>
<pattern id="pStr" width="8" height="8" patternUnits="userSpaceOnUse" patternTransform="rotate(30)"><rect width="3" height="8" fill="#fff6e6" opacity=".45"/></pattern>
<pattern id="pGing" width="12" height="12" patternUnits="userSpaceOnUse"><rect width="6" height="12" fill="#fff6e6" opacity=".35"/><rect width="12" height="6" fill="#fff6e6" opacity=".35"/></pattern>
<pattern id="pCurt" width="40" height="44" patternUnits="userSpaceOnUse"><g opacity=".35"><circle cx="10" cy="10" r="3.5" fill="#f4d6de"/><circle cx="6" cy="13" r="2.4" fill="#f4d6de"/><circle cx="14" cy="13" r="2.4" fill="#f4d6de"/><path d="M10 14 q2 6 6 8" stroke="#b9cf8a" stroke-width="1.2" fill="none"/><circle cx="30" cy="32" r="3" fill="#f6e2b0"/></g></pattern>
<pattern id="pGrain" width="140" height="24" patternUnits="userSpaceOnUse"><path d="M0 6 Q35 3 70 7 T140 5 M0 15 Q40 18 80 14 T140 16" fill="none" stroke="#6e4228" stroke-width=".8" opacity=".35"/><ellipse cx="96" cy="11" rx="6" ry="2.4" fill="none" stroke="#6e4228" stroke-width=".7" opacity=".35"/></pattern>
'''
