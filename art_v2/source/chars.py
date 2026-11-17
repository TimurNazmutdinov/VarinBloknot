# Персонажи «Варин блокнот» — плоский мультяшный стиль. Начало координат — между стоп.
import math
SKIN = '#ffe0c7'; CHEEK = '#ff9fb3'; EYE = '#3a2a3a'; SH = 'rgba(60,30,60,.16)'
PATS = '''<pattern id="cDots" width="18" height="18" patternUnits="userSpaceOnUse"><circle cx="5" cy="5" r="2.6" fill="#fff" opacity=".75"/><circle cx="14" cy="14" r="2.6" fill="#fff" opacity=".75"/></pattern>
<pattern id="cStripe" width="20" height="14" patternUnits="userSpaceOnUse"><rect width="20" height="6" fill="#fff" opacity=".55"/></pattern>
<pattern id="cCheck" width="16" height="16" patternUnits="userSpaceOnUse"><rect width="8" height="8" fill="#fff" opacity=".35"/><rect x="8" y="8" width="8" height="8" fill="#fff" opacity=".35"/></pattern>
<pattern id="cFlow" width="22" height="22" patternUnits="userSpaceOnUse"><g fill="#fff" opacity=".7"><circle cx="11" cy="7" r="2.4"/><circle cx="7" cy="11" r="2.4"/><circle cx="15" cy="11" r="2.4"/><circle cx="11" cy="15" r="2.4"/></g><circle cx="11" cy="11" r="1.8" fill="#ffd166"/></pattern>'''

def sh(c, f):
    c = c.lstrip('#'); r = [int(c[i:i+2], 16) for i in (0, 2, 4)]
    return '#' + ''.join('%02x' % max(0, min(255, round(v*(1+f) if f < 0 else v+(255-v)*f))) for v in r)

def person(legs=70, torso=90, head=56, top='#8fb8ff', top_pat=None, top_kind='dress', bottom='#4a3a5a',
           shoes='#4a3a5a', hair='#a0522d', style='braid', extra=(), skin=SKIN, sleeves=None, wide=48, skirt=14):
    o = []
    a = o.append
    hipy = -legs; shy = -legs - torso; neck = 18; hy = shy - neck - head * .82
    w = wide
    a(f'<ellipse cx="0" cy="6" rx="{w+10}" ry="10" fill="{SH}"/>')
    # back hair
    if style in ('braid', 'bob', 'long', 'pony', 'bun', 'twotails'):
        if style == 'long':
            a(f'<path d="M{-head-6} {hy} Q{-head-10} {hy+head*1.6} {-head+4} {hy+head*1.9} H{head-4} Q{head+10} {hy+head*1.6} {head+6} {hy}Z" fill="{hair}"/>')
        a(f'<circle cx="0" cy="{hy-head*.25:.1f}" r="{head*1.14:.1f}" fill="{hair}"/>')
        if style == 'bob':
            a(f'<path d="M{-head*1.1:.1f} {hy-10} V{hy+head*.55:.1f} Q{-head*.8:.1f} {hy+head*.75:.1f} {-head*.5:.1f} {hy+head*.6:.1f} H{head*.5:.1f} Q{head*.8:.1f} {hy+head*.75:.1f} {head*1.1:.1f} {hy+head*.55:.1f} V{hy-10}Z" fill="{hair}"/>')
    if style == 'braid':
        for k in range(6):
            a(f'<ellipse cx="{head*.95:.1f}" cy="{hy+head*.3+k*22:.1f}" rx="13" ry="15" fill="{hair}"/><path d="M{head*.95-9:.1f} {hy+head*.3+k*22+2:.1f} q9 7 18 0" fill="none" stroke="{sh(hair,-.25)}" stroke-width="2"/>')
        a(f'<circle cx="{head*.95:.1f}" cy="{hy+head*.3+6*22-6:.1f}" r="7" fill="#ff6b8b"/>')
    if style == 'twotails':
        for s in (-1, 1):
            a(f'<path d="M{s*head*.9:.1f} {hy-head*.2:.1f} q{s*head*.55:.1f} {head*.3:.1f} {s*head*.35:.1f} {head*1.2:.1f} q{-s*head*.1:.1f} {head*.2:.1f} {-s*head*.25:.1f} 0 q{s*head*.05:.1f} {-head*.6:.1f} {-s*head*.2:.1f} {-head*1.1:.1f}Z" fill="{hair}"/><circle cx="{s*head*.95:.1f}" cy="{hy-head*.15:.1f}" r="6" fill="#ffd166"/>')
    if style == 'pony':
        a(f'<path d="M{head*.7:.1f} {hy-head*.8:.1f} q{head*.9:.1f} {head*.2:.1f} {head*.55:.1f} {head*1.4:.1f} q{-head*.15:.1f} {head*.3:.1f} {-head*.3:.1f} 0 q{head*.15:.1f} {-head*.7:.1f} {-head*.4:.1f} {-head*1.2:.1f}Z" fill="{hair}"/>')
    if style == 'bun':
        a(f'<circle cx="0" cy="{hy-head*1.25:.1f}" r="{head*.42:.1f}" fill="{hair}"/>')
    # legs + shoes
    lx = w * .32
    for s in (-1, 1):
        a(f'<rect x="{s*lx-11:.1f}" y="{hipy-6}" width="22" height="{legs+6}" rx="10" fill="{skin if top_kind in ("dress","skirt") else bottom}"/>')
        a(f'<rect x="{s*lx-15+s*3:.1f}" y="-10" width="30" height="15" rx="7.5" fill="{shoes}"/>')
    # bottom / torso
    if top_kind == 'dress':
        d = f'M{-w} {hipy+8} Q{-w-4} {shy+20} 0 {shy} Q{w+4} {shy+20} {w} {hipy+8}Z'
        a(f'<path d="{d}" fill="{top}"/>')
        if top_pat: a(f'<path d="{d}" fill="url(#{top_pat})"/>')
        a(f'<rect x="{-w+2}" y="{hipy-8}" width="{2*w-4}" height="12" rx="6" fill="{sh(top,-.15)}"/>')
    else:
        if top_kind == 'skirt':
            a(f'<path d="M{-w+4} {hipy-34} H{w-4} L{w+8+skirt*.25:.1f} {hipy+skirt} Q0 {hipy+skirt+8} {-w-8-skirt*.25:.1f} {hipy+skirt}Z" fill="{bottom}"/>')
        else:
            for s in (-1, 1):
                a(f'<rect x="{s*lx-13:.1f}" y="{hipy-30}" width="26" height="{legs+22}" rx="11" fill="{bottom}"/>')
            a(f'<rect x="{-w+6}" y="{hipy-34}" width="{2*w-12}" height="30" rx="12" fill="{bottom}"/>')
        d = f'M{-w} {hipy-24} Q{-w-2} {shy+18} 0 {shy} Q{w+2} {shy+18} {w} {hipy-24}Z'
        a(f'<path d="{d}" fill="{top}"/>')
        if top_pat: a(f'<path d="{d}" fill="url(#{top_pat})"/>')
    # arms
    sl = sleeves or top
    for s in (-1, 1):
        a(f'<rect x="{s*(w-2)-9:.1f}" y="{shy+8}" width="18" height="{torso*.82:.1f}" rx="9" fill="{sl}"/>')
        if top_pat and not sleeves: a(f'<rect x="{s*(w-2)-9:.1f}" y="{shy+8}" width="18" height="{torso*.82:.1f}" rx="9" fill="url(#{top_pat})"/>')
        a(f'<circle cx="{s*(w-2):.1f}" cy="{shy+8+torso*.82:.1f}" r="11" fill="{skin}"/>')
    # neck + collar
    a(f'<rect x="-12" y="{shy-neck-6}" width="24" height="{neck+12}" rx="10" fill="{skin}"/>')
    # head
    a(f'<ellipse cx="0" cy="{hy:.1f}" rx="{head:.1f}" ry="{head*.96:.1f}" fill="{skin}"/>')
    ey = hy
    a(f'<ellipse cx="{-head*.36:.1f}" cy="{ey:.1f}" rx="{head*.12:.1f}" ry="{head*.16:.1f}" fill="{EYE}"/><ellipse cx="{head*.36:.1f}" cy="{ey:.1f}" rx="{head*.12:.1f}" ry="{head*.16:.1f}" fill="{EYE}"/>')
    a(f'<circle cx="{-head*.31:.1f}" cy="{ey-head*.07:.1f}" r="{head*.045:.1f}" fill="#fff"/><circle cx="{head*.41:.1f}" cy="{ey-head*.07:.1f}" r="{head*.045:.1f}" fill="#fff"/>')
    a(f'<ellipse cx="{-head*.6:.1f}" cy="{ey+head*.36:.1f}" rx="{head*.18:.1f}" ry="{head*.11:.1f}" fill="{CHEEK}" opacity=".7"/><ellipse cx="{head*.6:.1f}" cy="{ey+head*.36:.1f}" rx="{head*.18:.1f}" ry="{head*.11:.1f}" fill="{CHEEK}" opacity=".7"/>')
    a(f'<circle cx="0" cy="{ey+head*.22:.1f}" r="{head*.07:.1f}" fill="{sh(skin,-.12)}"/>')
    a(f'<path d="M{-head*.2:.1f} {ey+head*.46:.1f} Q0 {ey+head*.66:.1f} {head*.2:.1f} {ey+head*.46:.1f}" fill="none" stroke="#c4455e" stroke-width="{head*.07:.1f}" stroke-linecap="round"/>')
    # front hair
    H = head
    if style in ('braid', 'pony', 'twotails', 'long'):
        a(f'<path d="M{-H*1.04:.1f} {hy-H*.36:.1f} Q{-H*.9:.1f} {hy-H*1.45:.1f} 0 {hy-H*1.45:.1f} Q{H*.9:.1f} {hy-H*1.45:.1f} {H*1.04:.1f} {hy-H*.36:.1f} Q{H*.7:.1f} {hy-H*.75:.1f} {H*.25:.1f} {hy-H*.68:.1f} Q{H*.35:.1f} {hy-H*.46:.1f} {H*.14:.1f} {hy-H*.36:.1f} Q{H*.03:.1f} {hy-H*.72:.1f} {-H*.43:.1f} {hy-H*.68:.1f} Q{-H*.72:.1f} {hy-H*.6:.1f} {-H*1.04:.1f} {hy-H*.36:.1f}Z" fill="{hair}"/>')
    elif style == 'bob':
        a(f'<path d="M{-H*1.1:.1f} {hy-H*.2:.1f} Q{-H*1.0:.1f} {hy-H*1.5:.1f} 0 {hy-H*1.48:.1f} Q{H*1.0:.1f} {hy-H*1.5:.1f} {H*1.1:.1f} {hy-H*.2:.1f} V{hy-H*.5:.1f} H{-H*1.1:.1f}Z" fill="{hair}"/>')
    elif style == 'bun':
        a(f'<path d="M{-H*1.04:.1f} {hy-H*.2:.1f} Q{-H*1.0:.1f} {hy-H*1.4:.1f} 0 {hy-H*1.42:.1f} Q{H*1.0:.1f} {hy-H*1.4:.1f} {H*1.04:.1f} {hy-H*.2:.1f} Q{H*.9:.1f} {hy-H*.8:.1f} {H*.1:.1f} {hy-H*.85:.1f} Q{-H*.6:.1f} {hy-H*.85:.1f} {-H*1.04:.1f} {hy-H*.2:.1f}Z" fill="{hair}"/>')
    elif style == 'man':
        a(f'<path d="M{-H*1.02:.1f} {hy-H*.15:.1f} Q{-H*1.08:.1f} {hy-H*1.35:.1f} 0 {hy-H*1.32:.1f} Q{H*1.08:.1f} {hy-H*1.3:.1f} {H*1.02:.1f} {hy-H*.15:.1f} Q{H*.95:.1f} {hy-H*.75:.1f} {H*.4:.1f} {hy-H*.82:.1f} Q{-H*.5:.1f} {hy-H*.72:.1f} {-H*1.02:.1f} {hy-H*.15:.1f}Z" fill="{hair}"/>')
    elif style == 'boy':
        sp = ''.join(f'L{x*H:.1f} {hy-H*(1.38 + (.14 if i%2 else 0)):.1f} ' for i, x in enumerate([-.9, -.65, -.4, -.15, .1, .35, .6, .85]))
        a(f'<path d="M{-H*1.05:.1f} {hy-H*.2:.1f} Q{-H*1.1:.1f} {hy-H*1.1:.1f} {-H*.9:.1f} {hy-H*1.3:.1f} {sp}Q{H*1.1:.1f} {hy-H*1.1:.1f} {H*1.05:.1f} {hy-H*.2:.1f} Q{H*.8:.1f} {hy-H*.7:.1f} {H*.2:.1f} {hy-H*.72:.1f} Q{-H*.2:.1f} {hy-H*.5:.1f} {-H*.45:.1f} {hy-H*.75:.1f} Q{-H*.8:.1f} {hy-H*.7:.1f} {-H*1.05:.1f} {hy-H*.2:.1f}Z" fill="{hair}"/>')
    elif style == 'grandpa':
        for s in (-1, 1):
            a(f'<path d="M{s*H*1.02:.1f} {hy+H*.1:.1f} Q{s*H*1.15:.1f} {hy-H*.6:.1f} {s*H*.7:.1f} {hy-H*.9:.1f} Q{s*H*.85:.1f} {hy-H*.4:.1f} {s*H*.8:.1f} {hy+H*.1:.1f}Z" fill="{hair}"/>')
    # extras
    for ex in extra:
        kind = ex[0]
        if kind == 'glasses':
            col = ex[1] if len(ex) > 1 else '#4a3a5a'
            for s in (-1, 1): a(f'<circle cx="{s*H*.36:.1f}" cy="{ey:.1f}" r="{H*.25:.1f}" fill="#ffffff" fill-opacity=".25" stroke="{col}" stroke-width="{H*.06:.1f}"/>')
            a(f'<path d="M{-H*.11:.1f} {ey:.1f} H{H*.11:.1f}" stroke="{col}" stroke-width="{H*.05:.1f}"/>')
        if kind == 'mustache':
            a(f'<path d="M0 {ey+H*.34:.1f} q{-H*.2:.1f} {-H*.08:.1f} {-H*.34:.1f} {H*.08:.1f} q{H*.18:.1f} {H*.04:.1f} {H*.34:.1f} {-H*.02:.1f} q{H*.16:.1f} {H*.06:.1f} {H*.34:.1f} {H*.02:.1f} q{-H*.14:.1f} {-H*.16:.1f} {-H*.34:.1f} {-H*.08:.1f}Z" fill="{ex[1]}"/>')
        if kind == 'beard':
            a(f'<path d="M{-H*.85:.1f} {ey+H*.05:.1f} Q{-H*.8:.1f} {ey+H*1.15:.1f} 0 {ey+H*1.2:.1f} Q{H*.8:.1f} {ey+H*1.15:.1f} {H*.85:.1f} {ey+H*.05:.1f} Q{H*.6:.1f} {ey+H*.55:.1f} {H*.25:.1f} {ey+H*.42:.1f} Q0 {ey+H*.52:.1f} {-H*.25:.1f} {ey+H*.42:.1f} Q{-H*.6:.1f} {ey+H*.55:.1f} {-H*.85:.1f} {ey+H*.05:.1f}Z" fill="{ex[1]}"/>')
            a(f'<path d="M{-H*.16:.1f} {ey+H*.6:.1f} Q0 {ey+H*.72:.1f} {H*.16:.1f} {ey+H*.6:.1f}" fill="none" stroke="#c4455e" stroke-width="{H*.06:.1f}" stroke-linecap="round"/>')
        if kind == 'cap':
            a(f'<path d="M{-H*1.0:.1f} {hy-H*.55:.1f} Q{-H*.9:.1f} {hy-H*1.35:.1f} 0 {hy-H*1.35:.1f} Q{H*.95:.1f} {hy-H*1.3:.1f} {H*1.0:.1f} {hy-H*.55:.1f}Z" fill="{ex[1]}"/><path d="M{-H*1.05:.1f} {hy-H*.6:.1f} Q{-H*.2:.1f} {hy-H*.8:.1f} {H*1.2:.1f} {hy-H*.45:.1f} L{H*1.25:.1f} {hy-H*.36:.1f} Q{-H*.2:.1f} {hy-H*.62:.1f} {-H*1.05:.1f} {hy-H*.48:.1f}Z" fill="{sh(ex[1],-.25)}"/>')
        if kind == 'hat':
            a(f'<path d="M{-H*1.08:.1f} {hy-H*.5:.1f} Q{-H*1.0:.1f} {hy-H*1.55:.1f} 0 {hy-H*1.55:.1f} Q{H*1.0:.1f} {hy-H*1.55:.1f} {H*1.08:.1f} {hy-H*.5:.1f}Z" fill="{ex[1]}"/><path d="M{-H*1.08:.1f} {hy-H*.5:.1f} Q{-H*1.0:.1f} {hy-H*1.55:.1f} 0 {hy-H*1.55:.1f} Q{H*1.0:.1f} {hy-H*1.55:.1f} {H*1.08:.1f} {hy-H*.5:.1f}Z" fill="url(#cStripe)"/><rect x="{-H*1.12:.1f}" y="{hy-H*.66:.1f}" width="{H*2.24:.1f}" height="{H*.26:.1f}" rx="{H*.13:.1f}" fill="{sh(ex[1],.35)}"/><circle cx="0" cy="{hy-H*1.62:.1f}" r="{H*.26:.1f}" fill="#ffffff"/>')
        if kind == 'scarf':
            a(f'<rect x="{-w*.62:.1f}" y="{shy-14}" width="{w*1.24:.1f}" height="22" rx="11" fill="{ex[1]}"/><rect x="{w*.18:.1f}" y="{shy}" width="20" height="58" rx="8" fill="{ex[1]}"/><rect x="{w*.18:.1f}" y="{shy}" width="20" height="58" rx="8" fill="url(#cStripe)"/>')
        if kind == 'collar':
            a(f'<path d="M-24 {shy-6} Q-12 {shy+16} 0 {shy+4} Q12 {shy+16} 24 {shy-6} Q0 {shy+2} -24 {shy-6}Z" fill="#ffffff"/>')
        if kind == 'apron':
            a(f'<path d="M{-w*.62:.1f} {shy+30} H{w*.62:.1f} L{w*.8:.1f} {hipy+30} Q0 {hipy+40} {-w*.8:.1f} {hipy+30}Z" fill="{ex[1]}"/><path d="M{-w*.62:.1f} {shy+30} H{w*.62:.1f} L{w*.8:.1f} {hipy+30} Q0 {hipy+40} {-w*.8:.1f} {hipy+30}Z" fill="url(#cFlow)"/><rect x="{-w*.3:.1f}" y="{hipy-40}" width="{w*.6:.1f}" height="26" rx="6" fill="{sh(ex[1],.25)}"/>')
        if kind == 'book':
            a(f'<g transform="translate({w-2} {shy+torso*.75:.1f}) rotate(-12)"><rect x="-18" y="-28" width="36" height="46" rx="4" fill="{ex[1]}"/><rect x="-14" y="-24" width="28" height="6" rx="3" fill="#fff" opacity=".7"/></g>')
        if kind == 'shawl':
            a(f'<path d="M{-w-6} {shy+6} Q0 {shy+60} {w+6} {shy+6} Q0 {shy-10} {-w-6} {shy+6}Z" fill="{ex[1]}"/><path d="M{-w-6} {shy+6} Q0 {shy+60} {w+6} {shy+6} Q0 {shy-10} {-w-6} {shy+6}Z" fill="url(#cFlow)"/>')
        if kind == 'vest':
            a(f'<path d="M{-w+2} {hipy-24} Q{-w} {shy+18} {-w*.3:.1f} {shy+2} L-6 {hipy-24}Z M{w-2} {hipy-24} Q{w} {shy+18} {w*.3:.1f} {shy+2} L6 {hipy-24}Z" fill="{ex[1]}"/>')
        if kind == 'stripe':
            a(f'<rect x="{-w+2}" y="{shy+46}" width="{2*w-4}" height="14" fill="{ex[1]}"/>')
        if kind == 'bag':
            a(f'<path d="M{-w*.8:.1f} {shy+4} L{w*.9:.1f} {hipy-10}" stroke="{ex[1]}" stroke-width="7"/><rect x="{w*.55:.1f}" y="{hipy-30}" width="40" height="34" rx="10" fill="{ex[1]}"/>')
    return ''.join(o)

def cat_flat():
    F = '#ffa94d'; o = []
    a = o.append
    a(f'<ellipse cx="0" cy="4" rx="46" ry="8" fill="{SH}"/>')
    a(f'<path d="M30 -6 C70 -6 74 -44 58 -52 C50 -56 46 -48 52 -44 C60 -36 54 -18 26 -20Z" fill="{F}"/>')
    for x, y in ((44, -12), (56, -24), (58, -40)): a(f'<rect x="{x-6}" y="{y-3}" width="12" height="6" rx="3" fill="#f08c2e" transform="rotate(-30 {x} {y})"/>')
    a(f'<ellipse cx="0" cy="-30" rx="36" ry="34" fill="{F}"/><ellipse cx="0" cy="-24" rx="18" ry="24" fill="#fff1de"/>')
    for s in (-1, 1):
        for k in range(3): a(f'<rect x="{s*28-6}" y="{-50+k*12}" width="12" height="5" rx="2.5" fill="#f08c2e"/>')
    for dx in (-16, 16): a(f'<ellipse cx="{dx}" cy="-2" rx="13" ry="8" fill="{F}"/><ellipse cx="{dx}" cy="0" rx="9" ry="4" fill="#fff1de"/>')
    a(f'<path d="M-30 -78 L-30 -112 L-6 -92Z M30 -78 L30 -112 L6 -92Z" fill="{F}"/><path d="M-26 -86 L-26 -104 L-12 -92Z M26 -86 L26 -104 L12 -92Z" fill="#ffb3c1"/>')
    a(f'<ellipse cx="0" cy="-80" rx="36" ry="30" fill="{F}"/>')
    for dx in (-10, 0, 10): a(f'<rect x="{dx-3}" y="-108" width="6" height="14" rx="3" fill="#f08c2e"/>')
    a('<path d="M-20 -82 Q-13 -88 -6 -82 M6 -82 Q13 -88 20 -82" fill="none" stroke="#3a2a3a" stroke-width="3" stroke-linecap="round"/>')
    a('<ellipse cx="0" cy="-72" rx="5" ry="3.5" fill="#ff6b8b"/><ellipse cx="-22" cy="-72" rx="7" ry="4" fill="#ff9fb3" opacity=".7"/><ellipse cx="22" cy="-72" rx="7" ry="4" fill="#ff9fb3" opacity=".7"/>')
    a('<path d="M0 -69 Q-5 -63 -10 -66 M0 -69 Q5 -63 10 -66" fill="none" stroke="#3a2a3a" stroke-width="2.4" stroke-linecap="round"/>')
    a('<path d="M-14 -72 L-40 -76 M-14 -68 L-38 -64 M14 -72 L40 -76 M14 -68 L38 -64" stroke="#c98a5a" stroke-width="1.4"/>')
    return ''.join(o)

CHARS = {
 'varya':  dict(name='Варя', legs=70, torso=88, head=56, top='#8fb8ff', top_pat='cDots', top_kind='dress', hair='#a0522d', style='braid', extra=(('collar',),)),
 'mama':   dict(name='Мама', legs=118, torso=118, head=52, top='#4fbfb7', top_kind='skirt', bottom='#7d4a8a', hair='#6b3a1e', style='bun', shoes='#7d4a8a', wide=46, skirt=56, extra=(('collar',),)),
 'papa':   dict(name='Папа', legs=124, torso=126, head=54, top='#6f9bf0', top_pat='cCheck', top_kind='pants', bottom='#5a4a3a', hair='#4a3022', style='man', shoes='#3a2a1e', wide=54, extra=(('mustache', '#4a3022'),)),
 'petya':  dict(name='Петя', legs=110, torso=104, head=52, top='#ff6b6b', top_kind='pants', bottom='#4d6fa8', hair='#6b3a1e', style='boy', shoes='#ffffff', wide=46, extra=(('stripe', '#ffffff'),)),
 'katya':  dict(name='Катя', legs=72, torso=86, head=54, top='#ff8a3d', top_kind='pants', bottom='#3a3a5a', hair='#e8b04a', style='pony', shoes='#ff6b6b', extra=(('hat', '#4d96ff'), ('bag', '#4d96ff'))),
 'dasha':  dict(name='Даша', legs=70, torso=88, head=55, top='#9b7bd6', top_pat='cFlow', top_kind='dress', hair='#3a2416', style='bob', extra=(('glasses', '#5a3a6a'), ('scarf', '#6bcb77'), ('book', '#ff6b6b'))),
 'babushka': dict(name='Бабушка', legs=104, torso=116, head=52, top='#c8566a', top_kind='skirt', bottom='#5f4a7a', hair='#d8d4cc', style='bun', shoes='#5a3a2a', wide=52, skirt=88, extra=(('glasses', '#8a6a4a'), ('apron', '#ffd166'), ('shawl', '#4d96ff'))),
 'dedushka': dict(name='Дедушка', legs=114, torso=120, head=53, top='#f6efe0', top_kind='pants', bottom='#5a5a6a', hair='#d8d4cc', style='grandpa', shoes='#3a2a1e', wide=54, extra=(('vest', '#6b8a5a'), ('beard', '#e9e5dc'), ('cap', '#6a6a7a'))),
}

def build(key):
    p = dict(CHARS[key]); p.pop('name')
    return person(**p)
