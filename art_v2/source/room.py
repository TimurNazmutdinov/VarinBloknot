# Варина комната — акварель + перо, слоями
W,H=1200,900
INK='#4a3426'
def P(*pts):return ' '.join(f'{x:.1f},{y:.1f}' for x,y in pts)
def fw(x,y,t,depth=170):  # forward projection from back plane
    return (x+(x-600)*0.42*t, y+depth*t)
L={}  # layers
def add(k,s):L[k]=L.get(k,'')+s+'\n'
def poly(pts,fill,stroke=True,extra='',sw=2.2):
    return f'<polygon points="{P(*pts)}" fill="{fill}" {"stroke=\"%s\" stroke-width=\"%s\""%(INK,sw) if stroke else ""} stroke-linejoin="round" {extra}/>'
def path(d,fill='none',sw=2.2,extra='',stroke=INK):
    return f'<path d="{d}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}" stroke-linecap="round" stroke-linejoin="round" {extra}/>'
defs='''<defs>
<filter id="wc" x="-3%" y="-3%" width="106%" height="106%">
 <feTurbulence type="fractalNoise" baseFrequency="0.03" numOctaves="3" seed="4" result="n"/>
 <feDisplacementMap in="SourceGraphic" in2="n" scale="5" xChannelSelector="R" yChannelSelector="G" result="d"/>
 <feTurbulence type="fractalNoise" baseFrequency="0.09" numOctaves="4" seed="11" result="g"/>
 <feColorMatrix in="g" type="matrix" values="0 0 0 0 .8  0 0 0 0 .78  0 0 0 0 .76  -1.4 -1.4 0 0 1.45" result="gm"/>
 <feComposite in="gm" in2="d" operator="in" result="gmi"/>
 <feBlend in="d" in2="gmi" mode="multiply" result="tex"/>
 <feMorphology in="d" operator="erode" radius="2.2" result="er"/>
 <feComposite in="d" in2="er" operator="out" result="rim"/>
 <feColorMatrix in="rim" type="matrix" values=".72 0 0 0 0  0 .7 0 0 0  0 0 .68 0 0  0 0 0 .55 0" result="rd"/>
 <feGaussianBlur in="rd" stdDeviation="1.1" result="rb"/>
 <feMerge><feMergeNode in="tex"/><feMergeNode in="rb"/></feMerge>
</filter>
<filter id="ink"><feTurbulence type="fractalNoise" baseFrequency="0.05" numOctaves="2" seed="2" result="n"/><feDisplacementMap in="SourceGraphic" in2="n" scale="2.4" xChannelSelector="R" yChannelSelector="G"/></filter>
<filter id="paper" x="0" y="0" width="100%" height="100%"><feTurbulence type="fractalNoise" baseFrequency="0.85" numOctaves="3" seed="5"/><feColorMatrix type="matrix" values="0 0 0 0 .96  0 0 0 0 .93  0 0 0 0 .87  -.35 -.35 -.35 0 .62"/></filter>
<filter id="soft"><feGaussianBlur stdDeviation="18"/></filter>
<pattern id="wp" width="90" height="96" patternUnits="userSpaceOnUse">
 <g transform="translate(22 24)"><path d="M0 10 C-6 4 -4 -4 0 -2 C4 -4 6 4 0 10Z" fill="#c9767a"/><path d="M0 10 C2 16 6 20 10 20" fill="none" stroke="#7f9a5f" stroke-width="2"/><ellipse cx="9" cy="16" rx="5" ry="2.6" fill="#8fa86b" transform="rotate(-30 9 16)"/></g>
 <g transform="translate(67 72)"><path d="M0 10 C-6 4 -4 -4 0 -2 C4 -4 6 4 0 10Z" fill="#d8a46a"/><path d="M0 10 C-2 16 -6 20 -10 20" fill="none" stroke="#7f9a5f" stroke-width="2"/><ellipse cx="-9" cy="16" rx="5" ry="2.6" fill="#8fa86b" transform="rotate(30 -9 16)"/></g>
 <path d="M45 0 V96" stroke="#e6d3ad" stroke-width="3"/>
</pattern>
<linearGradient id="wallG" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#f3e6c9"/><stop offset="1" stop-color="#ead6b0"/></linearGradient>
<linearGradient id="sideL" x1="0" x2="1"><stop offset="0" stop-color="#d7c09a"/><stop offset="1" stop-color="#e6d3ad"/></linearGradient>
<linearGradient id="sideR" x1="1" x2="0"><stop offset="0" stop-color="#d4bc95"/><stop offset="1" stop-color="#e6d3ad"/></linearGradient>
<linearGradient id="wood" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#b9824f"/><stop offset="1" stop-color="#94613a"/></linearGradient>
<linearGradient id="floorG" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#a86f42"/><stop offset="1" stop-color="#c58d58"/></linearGradient>
<linearGradient id="sky" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#b9d6e6"/><stop offset=".7" stop-color="#e9eedf"/><stop offset="1" stop-color="#f6e7c4"/></linearGradient>
<linearGradient id="curt" x1="0" x2="1"><stop offset="0" stop-color="#7d3f55"/><stop offset=".25" stop-color="#a65a72"/><stop offset=".5" stop-color="#84445b"/><stop offset=".75" stop-color="#a65a72"/><stop offset="1" stop-color="#7d3f55"/></linearGradient>
<linearGradient id="shade" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#e08a3c"/><stop offset="1" stop-color="#c5672a"/></linearGradient>
<linearGradient id="fur" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#f0a95a"/><stop offset="1" stop-color="#d27a30"/></linearGradient>
<radialGradient id="glowWin" cx=".5" cy=".3" r=".7"><stop offset="0" stop-color="#fff4cf" stop-opacity=".85"/><stop offset="1" stop-color="#fff4cf" stop-opacity="0"/></radialGradient>
<radialGradient id="glowLamp" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="#ffd98a" stop-opacity=".7"/><stop offset="1" stop-color="#ffd98a" stop-opacity="0"/></radialGradient>
<linearGradient id="green" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#4f8a5a"/><stop offset="1" stop-color="#2f5e3c"/></linearGradient>
</defs>'''

# ---------- background: shell ----------
bg=[]
bg.append(poly([(0,0),(1200,0),(1050,50),(150,50)],'#efe4cc'))
bg.append(poly([(150,50),(1050,50),(1050,600),(150,600)],'url(#wallG)'))
bg.append(poly([(150,50),(1050,50),(1050,470),(150,470)],'url(#wp)',stroke=False,extra='opacity=".9"'))
bg.append(poly([(0,0),(150,50),(150,600),(0,760)],'url(#sideL)'))
bg.append(poly([(0,0),(150,50),(150,470),(0,590)],'url(#wp)',stroke=False,extra='opacity=".75"'))
bg.append(poly([(1200,0),(1050,50),(1050,600),(1200,760)],'url(#sideR)'))
bg.append(poly([(1200,0),(1050,50),(1050,470),(1200,590)],'url(#wp)',stroke=False,extra='opacity=".75"'))
# cornice
bg.append(poly([(150,50),(1050,50),(1050,64),(150,64)],'#f6efe0'))
# wainscot
bg.append(poly([(150,470),(1050,470),(1050,600),(150,600)],'url(#wood)'))
for x in range(170,1040,145):bg.append(f'<rect x="{x}" y="488" width="125" height="96" rx="4" fill="#ad7748" stroke="{INK}" stroke-width="1.6"/>')
bg.append(poly([(0,590),(150,470),(150,600),(0,760)],'#8e5c37'))
bg.append(poly([(1200,590),(1050,470),(1050,600),(1200,760)],'#8e5c37'))
bg.append(poly([(150,462),(1050,462),(1050,474),(150,474)],'#c89660'))
# floor + planks
bg.append(poly([(0,760),(150,600),(1050,600),(1200,760),(1200,900),(0,900)],'url(#floorG)'))
pl=''.join(f'<line x1="{600+i*55}" y1="600" x2="{600+i*55*2.1}" y2="900" stroke="#7a4b2a" stroke-width="1.4" opacity=".55"/>' for i in range(-12,13))
bg.append(f'<g clip-path="url(#floorClip)">{pl}</g>')
defs=defs.replace('</defs>','<clipPath id="floorClip"><polygon points="0,760 150,600 1050,600 1200,760 1200,900 0,900"/></clipPath></defs>')
# window
wx0,wx1,wy0,wy1=470,730,110,400
bg.append(f'<rect x="{wx0}" y="{wy0}" width="{wx1-wx0}" height="{wy1-wy0}" fill="url(#sky)"/>')
view=f'''<g clip-path="url(#winClip)">
<path d="M470 330 Q560 300 640 320 T730 310 V400 H470Z" fill="#9db77f"/>
<path d="M480 335 h40 v-30 l20 -18 l20 18 v30 h30 v-20 h40 v20 h60 v65 h-210z" fill="#d79a74" stroke="{INK}" stroke-width="1.2" opacity=".8"/>
<path d="M650 300 v-38 q0 -12 10 -16 q-2 -14 8 -22 q10 8 8 22 q10 4 10 16 v38z" fill="#e3e8ea" stroke="{INK}" stroke-width="1.2"/>
<path d="M660 222 q8 -10 16 0 q-2 -12 -8 -16 q-6 4 -8 16z" fill="#d8b34a" stroke="{INK}" stroke-width="1"/>
<line x1="668" y1="206" x2="668" y2="196" stroke="{INK}" stroke-width="1.5"/>
<path d="M545 400 C548 320 540 230 552 120" stroke="#f3f0e8" stroke-width="12" fill="none"/>
<path d="M545 400 C548 320 540 230 552 120" stroke="{INK}" stroke-width="1.2" fill="none" stroke-dasharray="6 18"/>
<path d="M549 200 Q520 180 490 186 M550 160 Q580 140 610 146 M548 240 Q580 226 600 236" stroke="#5a4a3a" stroke-width="2" fill="none"/><ellipse cx="492" cy="192" rx="5" ry="8" fill="#a8c46a" transform="rotate(20 492 192)" opacity=".9"/><ellipse cx="504" cy="198" rx="5" ry="8" fill="#a8c46a" transform="rotate(-10 504 198)" opacity=".9"/><ellipse cx="516" cy="190" rx="5" ry="8" fill="#a8c46a" transform="rotate(30 516 190)" opacity=".9"/><ellipse cx="498" cy="206" rx="5" ry="8" fill="#a8c46a" transform="rotate(0 498 206)" opacity=".9"/><ellipse cx="586" cy="150" rx="5" ry="8" fill="#a8c46a" transform="rotate(-20 586 150)" opacity=".9"/><ellipse cx="600" cy="156" rx="5" ry="8" fill="#a8c46a" transform="rotate(10 600 156)" opacity=".9"/><ellipse cx="612" cy="150" rx="5" ry="8" fill="#a8c46a" transform="rotate(-30 612 150)" opacity=".9"/><ellipse cx="594" cy="164" rx="5" ry="8" fill="#a8c46a" transform="rotate(0 594 164)" opacity=".9"/><ellipse cx="578" cy="236" rx="5" ry="8" fill="#a8c46a" transform="rotate(15 578 236)" opacity=".9"/><ellipse cx="590" cy="244" rx="5" ry="8" fill="#a8c46a" transform="rotate(-15 590 244)" opacity=".9"/><ellipse cx="600" cy="240" rx="5" ry="8" fill="#a8c46a" transform="rotate(30 600 240)" opacity=".9"/><ellipse cx="524" cy="182" rx="5" ry="8" fill="#a8c46a" transform="rotate(-25 524 182)" opacity=".9"/><ellipse cx="570" cy="140" rx="5" ry="8" fill="#a8c46a" transform="rotate(10 570 140)" opacity=".9"/><ellipse cx="606" cy="232" rx="5" ry="8" fill="#a8c46a" transform="rotate(0 606 232)" opacity=".9"/>
</g>'''
defs=defs.replace('</defs>',f'<clipPath id="winClip"><rect x="{wx0}" y="{wy0}" width="{wx1-wx0}" height="{wy1-wy0}"/></clipPath></defs>')
bg.append(view)
bg.append(f'<rect x="{wx0-10}" y="{wy0-10}" width="{wx1-wx0+20}" height="{wy1-wy0+20}" fill="none" stroke="#f4ecdb" stroke-width="16"/>')
bg.append(f'<rect x="{wx0-18}" y="{wy0-18}" width="{wx1-wx0+36}" height="{wy1-wy0+36}" fill="none" stroke="{INK}" stroke-width="2.2"/>')
bg.append(f'<path d="M600 {wy0} V{wy1} M{wx0} 250 H{wx1}" stroke="#f4ecdb" stroke-width="10"/><path d="M600 {wy0} V{wy1} M{wx0} 250 H{wx1}" stroke="{INK}" stroke-width="1.2" opacity=".6"/>')
bg.append(poly([(440,404),(760,404),(778,428),(422,428)],'#f6efe0'))
# radiator
rad=''.join(f'<rect x="{x}" y="490" width="16" height="92" rx="7" fill="#efe8da" stroke="{INK}" stroke-width="1.6"/>' for x in range(500,700,20))
bg.append(rad)
L['background']='\n'.join(bg)

# ---------- curtains ----------
add('curtains',path('M430 70 C470 72 482 80 486 92 C474 210 498 330 474 452 L414 452 C426 330 412 210 430 70Z','url(#curt)'))
add('curtains',path('M770 70 C730 72 718 80 714 92 C726 210 702 330 726 452 L786 452 C774 330 788 210 770 70Z','url(#curt)'))
add('curtains',path('M420 300 Q450 312 482 296 M780 300 Q750 312 718 296',sw=3,stroke='#d9b25a'))
add('curtains',f'<line x1="400" y1="66" x2="800" y2="66" stroke="{INK}" stroke-width="6" stroke-linecap="round"/><circle cx="400" cy="66" r="9" fill="#c99a45" stroke="{INK}" stroke-width="1.6"/><circle cx="800" cy="66" r="9" fill="#c99a45" stroke="{INK}" stroke-width="1.6"/>')
# ---------- geraniums ----------
for x in (482,552):
    add('geraniums',path(f'M{x} 372 h46 l-6 36 h-34z','#c9673c'))
    add('geraniums',f'<g>'+''.join(f'<ellipse cx="{x+23+dx}" cy="{360+dy}" rx="15" ry="10" fill="#6f9a52" stroke="{INK}" stroke-width="1.4" transform="rotate({r} {x+23+dx} {360+dy})"/>' for dx,dy,r in((-16,0,-20),(16,2,20),(0,-8,0)))+'</g>')
    add('geraniums',''.join(f'<circle cx="{x+23+dx}" cy="{334+dy}" r="{rr}" fill="#d9434e" stroke="{INK}" stroke-width="1.2"/>' for dx,dy,rr in((-8,0,9),(8,-2,9),(0,-12,9),(0,2,6))))
# ---------- cat on sill ----------
cx,cy=668,352
add('cat',path(f'M{cx+28} {cy+48} C{cx+70} {cy+50} {cx+76} {cy+18} {cx+60} {cy+6} C{cx+56} {cy+2} {cx+52} {cy+8} {cx+56} {cy+12} C{cx+64} {cy+28} {cx+58} {cy+40} {cx+26} {cy+38}Z','url(#fur)'))
add('cat',path(f'M{cx-32} {cy+52} C{cx-40} {cy+10} {cx-24} {cy-14} {cx} {cy-14} C{cx+24} {cy-14} {cx+40} {cy+10} {cx+32} {cy+52}Z','url(#fur)'))
add('cat',f'<ellipse cx="{cx}" cy="{cy+24}" rx="13" ry="20" fill="#f7e2bf"/>')
add('cat',path(f'M{cx-30} {cy-34} L{cx-26} {cy-66} L{cx-6} {cy-44}Z M{cx+30} {cy-34} L{cx+26} {cy-66} L{cx+6} {cy-44}Z','#e59a4c'))
add('cat',f'<ellipse cx="{cx}" cy="{cy-34}" rx="32" ry="27" fill="url(#fur)" stroke="{INK}" stroke-width="2.2"/>')
add('cat',path(f'M{cx-10} {cy-58} L{cx-7} {cy-48} M{cx} {cy-60} L{cx} {cy-50} M{cx+10} {cy-58} L{cx+7} {cy-48}',sw=3,stroke='#a85a22'))
add('cat',path(f'M{cx-18} {cy-36} Q{cx-12} {cy-41} {cx-6} {cy-36} M{cx+6} {cy-36} Q{cx+12} {cy-41} {cx+18} {cy-36}',sw=2.4))
add('cat',path(f'M{cx-4} {cy-26} L{cx+4} {cy-26} L{cx} {cy-21}Z','#e07a8a',sw=1.4)+path(f'M{cx} {cy-21} Q{cx-5} {cy-16} {cx-10} {cy-18} M{cx} {cy-21} Q{cx+5} {cy-16} {cx+10} {cy-18}',sw=1.6))
add('cat',path(f'M{cx-14} {cy-24} L{cx-40} {cy-28} M{cx-14} {cy-20} L{cx-38} {cy-16} M{cx+14} {cy-24} L{cx+40} {cy-28} M{cx+14} {cy-20} L{cx+38} {cy-16}',sw=1,stroke='#6b4a33'))
add('cat',f'<ellipse cx="{cx-14}" cy="{cy+52}" rx="12" ry="6" fill="#f0a95a" stroke="{INK}" stroke-width="1.8"/><ellipse cx="{cx+14}" cy="{cy+52}" rx="12" ry="6" fill="#f0a95a" stroke="{INK}" stroke-width="1.8"/>')
# ---------- wall decor: pictures, shoes, clock ----------
add('pictures',f'<rect x="190" y="180" width="130" height="100" fill="#f3e9d2" stroke="{INK}" stroke-width="2"/><rect x="180" y="170" width="150" height="120" fill="none" stroke="#9c6a3c" stroke-width="10"/><rect x="175" y="165" width="160" height="130" fill="none" stroke="{INK}" stroke-width="2"/>')
add('pictures',f'<path d="M196 260 Q230 220 260 244 Q280 226 314 258 V274 H196Z" fill="#9db77f"/><circle cx="292" cy="208" r="12" fill="#f0c86a"/>')
add('pictures',f'<ellipse cx="380" cy="220" rx="36" ry="46" fill="#efe1c2" stroke="#c99a45" stroke-width="8"/><ellipse cx="380" cy="220" rx="41" ry="51" fill="none" stroke="{INK}" stroke-width="2"/><path d="M380 190 q-12 14 0 34 q12 -20 0 -34" fill="#c9767a" stroke="{INK}" stroke-width="1.4"/><path d="M380 224 v26" stroke="#6f9a52" stroke-width="2.4"/>')
add('shoes',path('M78 200 L84 208',sw=3)+path('M84 208 C70 230 60 260 66 280 C70 296 88 292 90 278 C92 258 88 230 84 208Z','#f2b8c0')+path('M84 208 C96 232 104 262 100 282 C98 298 116 300 118 284 C120 262 104 230 84 208Z','#f2b8c0')+path('M84 208 C80 240 72 300 60 330 M84 208 C92 240 104 300 112 334',sw=2,stroke='#e08aa0'))
ccx=880
add('clock',path(f'M{ccx-34} 120 h68 v74 l-34 26 l-34 -26z','#a8703f')+f'<circle cx="{ccx}" cy="160" r="24" fill="#f6efe0" stroke="{INK}" stroke-width="2"/>'+path(f'M{ccx} 160 V144 M{ccx} 160 L{ccx+11} 166',sw=2.6)+path(f'M{ccx-14} 220 V300 M{ccx+14} 220 V280',sw=1.4)+f'<path d="M{ccx-20} 300 h12 l-2 26 h-8z M{ccx+8} 280 h12 l-2 26 h-8z" fill="#5e4a2c" stroke="{INK}" stroke-width="1.4"/>'+path(f'M{ccx} 220 L{ccx+4} 262',sw=1.4)+f'<circle cx="{ccx+4}" cy="268" r="8" fill="#d8b34a" stroke="{INK}" stroke-width="1.4"/>')
# ---------- shelf + books + plant ----------
sh=f'<rect x="950" y="300" width="100" height="12" fill="#8a5634" stroke="{INK}" stroke-width="1.8"/>'
bx=956
for c,w,h in(('#8c3f35',18,64),('#3e5f84',16,56),('#c99a45',20,70),('#4f7a4a',15,58)):
    sh+=f'<rect x="{bx}" y="{300-h}" width="{w}" height="{h}" fill="{c}" stroke="{INK}" stroke-width="1.6"/><line x1="{bx+3}" y1="{300-h+10}" x2="{bx+w-3}" y2="{300-h+10}" stroke="#f2e2b8" stroke-width="2"/>';bx+=w+2
sh+=path('M1024 300 l2 -26 h20 l2 26z','#c9673c')+''.join(f'<ellipse cx="{1036+dx}" cy="{262+dy}" rx="6" ry="14" fill="#6f9a52" stroke="{INK}" stroke-width="1.2" transform="rotate({r} {1036+dx} {262+dy})"/>' for dx,dy,r in((-8,0,-25),(8,0,25),(0,-6,0)))
add('shelf',sh)
# ---------- bed ----------
hb=f'<path d="M190 520 V380 C190 340 380 340 380 380 V520" fill="none" stroke="#ede4d2" stroke-width="12"/><path d="M190 520 V380 C190 340 380 340 380 380 V520" fill="none" stroke="{INK}" stroke-width="2" />'
hb+=''.join(f'<line x1="{x}" y1="372" x2="{x}" y2="520" stroke="#ede4d2" stroke-width="7"/><line x1="{x}" y1="372" x2="{x}" y2="520" stroke="{INK}" stroke-width="1.2" opacity=".7"/>' for x in range(222,360,26))
hb+=f'<path d="M210 372 C250 352 320 352 360 372" fill="none" stroke="#ede4d2" stroke-width="8"/>'+''.join(f'<circle cx="{x}" cy="{y}" r="11" fill="#d1a24a" stroke="{INK}" stroke-width="1.8"/>' for x,y in((190,372),(380,372)))
add('bed',hb)
b0,b1=(180,520),(392,520);f0,f1=fw(180,520,1),fw(392,520,1)
add('bed',poly([b0,b1,f1,f0],'#f6f1e6'))
add('bedframe',poly([(f0[0]-6,f0[1]+40),(f1[0]+6,f1[1]+40),(f1[0]+6,f1[1]+70),(f0[0]-6,f0[1]+70)],'#8a5634'));add('bedframe',f'<rect x="{f0[0]-4}" y="{f0[1]+70}" width="12" height="22" fill="#5e3a22" stroke="{INK}" stroke-width="1.4"/><rect x="{f1[0]-8}" y="{f1[1]+70}" width="12" height="22" fill="#5e3a22" stroke="{INK}" stroke-width="1.4"/>')
# quilt 3x3
q0,q1=(180,548),(392,548);r0,r1=fw(180,548,.95),fw(392,548,.95)
cols=['#b8584a','#e2b85a','#5f7f9a','#7f9a5a','#d48fa0','#e9dcc0','#c9767a','#4f7a6a','#e2b85a']
def lerp(a,b,t):return(a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t)
for i in range(3):
    for j in range(3):
        Q=lambda u,v:lerp(lerp(q0,q1,u),lerp(r0,r1,u),v)
        add('quilt',poly([Q(i/3,j/3),Q((i+1)/3,j/3),Q((i+1)/3,(j+1)/3),Q(i/3,(j+1)/3)],cols[i*3+j],sw=1.6))
add('quilt',poly([r0,r1,(r1[0],r1[1]+64),(r0[0],r0[1]+64)],'#a24f42'))
add('quilt',path(f'M{r0[0]} {r0[1]+64} '+' '.join(f'q{(r1[0]-r0[0])/16} 10 {(r1[0]-r0[0])/8} 0' for _ in range(8)),sw=1.6))
for px in(232,336):
    add('pillows',path(f'M{px-52} 528 Q{px} 504 {px+52} 528 Q{px+60} 548 {px+50} 566 Q{px} 578 {px-50} 566 Q{px-60} 548 {px-52} 528Z','#fbf7ee'))
    add('pillows',path(f'M{px-30} 546 Q{px} 538 {px+30} 546',sw=1,stroke='#c9b89a'))
# ---------- yarn basket ----------
add('basket',path('M130 780 L230 780 L220 846 L140 846Z','#c4914f')+''.join(f'<line x1="{x}" y1="782" x2="{x-2}" y2="844" stroke="{INK}" stroke-width="1" opacity=".5"/>' for x in range(146,224,12)))
for x,c in((152,'#b8584a'),(182,'#5f7f9a'),(208,'#e2b85a')):add('basket',f'<circle cx="{x}" cy="772" r="18" fill="{c}" stroke="{INK}" stroke-width="1.8"/>'+path(f'M{x-12} 766 Q{x} 780 {x+12} 764',sw=1,stroke='#00000055'))
# ---------- rug ----------
rug=''
for i,c in enumerate(['#b8584a','#e2b85a','#5f7f9a','#f0e3c6','#c9767a']):
    rug+=f'<ellipse cx="600" cy="800" rx="{300-i*56}" ry="{78-i*15}" fill="{c}" stroke="{INK}" stroke-width="{2 if i==0 else 1.2}"/>'
add('rug',rug)
# ---------- desk ----------
d0,d1=(820,540),(1040,540);e0,e1=fw(820,540,.55,150),fw(1040,540,.55,150)
add('desk',poly([d0,d1,e1,e0],'#a86c3e'))
add('desk',poly([e0,e1,(e1[0],e1[1]+96),(e0[0],e0[1]+96)],'#8a5634'))
for i in range(2):
    y=e0[1]+12+i*42;x0=e0[0]+130
    add('desk',f'<rect x="{x0}" y="{y}" width="{e1[0]-x0-14}" height="34" rx="3" fill="#9a6440" stroke="{INK}" stroke-width="1.6"/><circle cx="{(x0+e1[0]-14)/2}" cy="{y+17}" r="5" fill="#d1a24a" stroke="{INK}" stroke-width="1.2"/>')
add('desk',f'<rect x="{e0[0]+6}" y="{e0[1]+96}" width="14" height="120" fill="#7a4a2c" stroke="{INK}" stroke-width="1.6"/><rect x="{e1[0]-20}" y="{e1[1]+96}" width="14" height="120" fill="#7a4a2c" stroke="{INK}" stroke-width="1.6"/>')
# lamp
add('lamp',f'<ellipse cx="880" cy="548" rx="24" ry="7" fill="#3d3a3a" stroke="{INK}" stroke-width="1.6"/>'+path('M880 546 L870 486 L904 456',sw=4,stroke='#3d3a3a')+path('M876 446 L936 446 L952 482 L860 482Z','url(#green)'))
add('lampglow','<ellipse cx="905" cy="530" rx="110" ry="60" fill="url(#glowLamp)"/>')
# globe
add('globe',f'<circle cx="1000" cy="500" r="30" fill="#7fa8c0" stroke="{INK}" stroke-width="2"/>'+path('M978 488 Q1000 498 1022 484 M984 512 Q1000 506 1016 514',sw=1.6,stroke='#5f8a4f')+path('M1000 530 V548 M984 550 H1016 M968 500 Q966 466 1000 466',sw=2.4))
# books stack
add('books',''.join(f'<rect x="{930+o}" y="{y}" width="{w}" height="12" rx="2" fill="{c}" stroke="{INK}" stroke-width="1.4"/>' for o,y,w,c in((0,532,56,'#8c3f35'),(4,520,48,'#3e5f84'),(-2,508,52,'#c99a45'))))
# chair (Vienna)
ch=path('M890 700 C890 640 900 600 940 600 C980 600 990 640 990 700',sw=7,stroke='#5e3a22')+path('M890 700 C890 640 900 600 940 600 C980 600 990 640 990 700',sw=1.4)
ch+=f'<ellipse cx="940" cy="704" rx="62" ry="16" fill="#8a5634" stroke="{INK}" stroke-width="2"/>'+path('M884 708 L874 820 M996 708 L1006 820 M912 716 L906 812 M968 716 L974 812',sw=6,stroke='#5e3a22')+path('M890 770 Q940 786 990 770',sw=4,stroke='#5e3a22',fill='none')
add('chair',ch)
# ceiling lamp
add('abazhur',path('M300 0 V70',sw=2)+path('M262 70 L338 70 L378 122 L222 122Z','url(#shade)')+path('M232 104 Q300 112 368 104',sw=1.4,stroke='#f5c27a')+''.join(f'<line x1="{x}" y1="122" x2="{x}" y2="140" stroke="#c5672a" stroke-width="2"/>' for x in range(228,374,9)))
add('shadows','<g filter="url(#soft)" opacity=".35"><ellipse cx="230" cy="780" rx="230" ry="40" fill="#3a2416"/><ellipse cx="960" cy="830" rx="170" ry="30" fill="#3a2416"/><ellipse cx="600" cy="830" rx="300" ry="40" fill="#3a2416"/><rect x="150" y="40" width="900" height="40" fill="#5a3a20"/><rect x="120" y="40" width="60" height="560" fill="#5a3a20"/><rect x="1020" y="40" width="60" height="560" fill="#5a3a20"/></g>')
add('glow','<ellipse cx="300" cy="230" rx="300" ry="180" fill="url(#glowLamp)" opacity=".6"/><path d="M470 400 L730 400 L860 900 L330 900Z" fill="url(#glowWin)" opacity=".55"/>')

ORDER=['background','shadows','curtains','geraniums','cat','pictures','shoes','clock','shelf','bed','bedframe','quilt','pillows','rug','basket','desk','books','globe','lamp','chair','abazhur','glow','lampglow']
NOWC={'glow','lampglow','shadows'}
def layer(k):
    f='' if k in NOWC else ' filter="url(#wc)"'
    return f'<g id="{k}"{f}>{L[k]}</g>'
def svg(keys,paper=True):
    inner='\n'.join(layer(k) for k in keys)
    pp=f'<rect width="{W}" height="{H}" filter="url(#paper)" style="mix-blend-mode: multiply"/>' if paper else ''
    bgc=f'<rect width="{W}" height="{H}" fill="#f4ead6"/>' if paper else ''
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">{defs}{bgc}<g filter="url(#ink)">{inner}</g>{pp}</svg>'
open('room.svg','w').write(svg(ORDER))
import json
json.dump({k:svg([k],paper=False) for k in ORDER},open('layers.json','w'))
print('ok')
