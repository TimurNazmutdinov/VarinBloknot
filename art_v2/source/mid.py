import math,json,re
W,H=1200,900;CX=600;CY=232;F=866;ZC=-1.55;YC=1.8;XC=0.0
INK='#5a3d2a'
GD=[];GID=[0]
def grad(col,vert=True):
    return col
    if not col.startswith('#'):return col
    GID[0]+=1;i=f'g{GID[0]}'
    a,b=shade(col,.16),shade(col,-.14)
    GD.append(f'<linearGradient id="{i}" x1="0" y1="0" x2="{0 if vert else 1}" y2="{1 if vert else 0}"><stop offset="0" stop-color="{a}"/><stop offset="1" stop-color="{b}"/></linearGradient>')
    return f'url(#{i})'
def edge(col):
    return shade(col,-.1) if col.startswith('#') else 'none'
def pr(x,y,z):
    s=F/(z-ZC);return (CX+(x-XC)*s, CY-(y-YC)*s)
def sc(z):return F/(z-ZC)
def hx(c):c=c.lstrip('#');return [int(c[i:i+2],16) for i in (0,2,4)]
def shade(c,f):
    r=hx(c);return '#'+''.join('%02x'%max(0,min(255,round(v*(1+f) if f<0 else v+(255-v)*f))) for v in r)
def pts(P):return ' '.join(f'{x:.1f},{y:.1f}' for x,y in P)
def poly(P,fill,sw=2,op=''):return f'<polygon points="{pts(P)}" fill="{grad(fill)}" stroke="{edge(fill)}" stroke-width="{min(sw,1.2) if sw else 0}" stroke-linejoin="round" {op}/>'
def path(d,fill='none',sw=2,stroke=INK,extra=''):
    if stroke==INK and fill!='none':stroke=edge(fill)
    if fill!='none':sw=min(sw,1.4)
    return f'<path d="{d}" fill="{grad(fill)}" stroke="{stroke}" stroke-width="{sw}" stroke-linecap="round" stroke-linejoin="round" {extra}/>'
def box(x0,x1,y0,y1,z0,z1,col,sw=2,top=.14,side=-.2,front=0):
    o=[]
    if XC<x0:o.append(poly([pr(x0,y0,z0),pr(x0,y0,z1),pr(x0,y1,z1),pr(x0,y1,z0)],shade(col,side),sw))
    if XC>x1:o.append(poly([pr(x1,y0,z0),pr(x1,y0,z1),pr(x1,y1,z1),pr(x1,y1,z0)],shade(col,side),sw))
    if y1<YC:o.append(poly([pr(x0,y1,z0),pr(x1,y1,z0),pr(x1,y1,z1),pr(x0,y1,z1)],shade(col,top),sw))
    if y0>YC:o.append(poly([pr(x0,y0,z0),pr(x1,y0,z0),pr(x1,y0,z1),pr(x0,y0,z1)],shade(col,-.3),sw))
    o.append(poly([pr(x0,y0,z0),pr(x1,y0,z0),pr(x1,y1,z0),pr(x0,y1,z0)],shade(col,front),sw))
    return ''.join(o)
def lerp(a,b,t):return(a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t)
def grid(q,nx,ny,cols,sw=1.4):
    o=''
    for i in range(nx):
        for j in range(ny):
            Q=lambda u,v:lerp(lerp(q[0],q[1],u),lerp(q[3],q[2],u),v)
            o+=poly([Q(i/nx,j/ny),Q((i+1)/nx,j/ny),Q((i+1)/nx,(j+1)/ny),Q(i/nx,(j+1)/ny)],cols[(i*ny+j*2+i)%len(cols)],sw)
    return o
L={};ORDER=[]
def add(k,s):
    if k not in L:L[k]='';ORDER.append(k)
    L[k]+=s+'\n'
defs=open('defs.svg').read() if False else None
src=open('room.py').read();defs=re.search(r"defs='''(.*?)'''",src,re.S).group(1)
# ---------------- shell ----------------
RX,RZ,RH=2.6,4.0,2.8
bk=[pr(-RX,0,RZ),pr(RX,0,RZ),pr(RX,RH,RZ),pr(-RX,RH,RZ)]
add('ceiling',poly([pr(-RX,RH,0),pr(RX,RH,0),pr(RX,RH,RZ),pr(-RX,RH,RZ)],'#f1e8d4'))
add('background',poly(bk,'url(#wallG)'))
add('background',poly([pr(-RX,.9,RZ),pr(RX,.9,RZ),pr(RX,RH,RZ),pr(-RX,RH,RZ)],'url(#wp)',sw=0,op='opacity=".85"'))
for sx in(-RX,RX):
    side=[pr(sx,0,0),pr(sx,0,RZ),pr(sx,RH,RZ),pr(sx,RH,0)]
    add('background',poly(side,'#dfc9a2'))
    add('background',poly([pr(sx,.9,0),pr(sx,.9,RZ),pr(sx,RH,RZ),pr(sx,RH,0)],'url(#wp)',sw=0,op='opacity=".7"'))
    add('background',poly([pr(sx,0,0),pr(sx,0,RZ),pr(sx,.9,RZ),pr(sx,.9,0)],'#8e5c37'))
    add('background',poly([pr(sx,.88,0),pr(sx,.88,RZ),pr(sx,.94,RZ),pr(sx,.94,0)],'#c08e5a',sw=1.4))
add('background',poly([pr(-RX,0,RZ),pr(RX,0,RZ),pr(RX,.9,RZ),pr(-RX,.9,RZ)],'#a26d42'))
for i in range(6):
    x0=-RX+.12+i*1.0;add('background',poly([pr(x0,.1,RZ),pr(x0+.8,.1,RZ),pr(x0+.8,.78,RZ),pr(x0,.78,RZ)],'#ad7748',sw=1.4))
add('background',poly([pr(-RX,.88,RZ),pr(RX,.88,RZ),pr(RX,.94,RZ),pr(-RX,.94,RZ)],'#c08e5a',sw=1.4))
add('background',poly([pr(-RX,RH-.08,RZ),pr(RX,RH-.08,RZ),pr(RX,RH,RZ),pr(-RX,RH,RZ)],'#f6efe0',sw=1.4))
add('background',poly([pr(-RX,0,0),pr(RX,0,0),pr(RX,0,RZ),pr(-RX,0,RZ)],'#b57d4c'))
pl=''.join(f'<line x1="{pr(x,0,0)[0]:.1f}" y1="{pr(x,0,0)[1]:.1f}" x2="{pr(x,0,RZ)[0]:.1f}" y2="{pr(x,0,RZ)[1]:.1f}" stroke="#7a4b2a" stroke-width="1.3" opacity=".5"/>' for x in [i*0.3-3 for i in range(21)])
for z in(0.9,1.9,2.9):pl+=''.join(f'<line x1="{pr(x,0,z+((x*7)%3)*.25)[0]:.1f}" y1="{pr(x,0,z+((x*7)%3)*.25)[1]:.1f}" x2="{pr(x+.3,0,z+((x*7)%3)*.25)[0]:.1f}" y2="{pr(x+.3,0,z+((x*7)%3)*.25)[1]:.1f}" stroke="#7a4b2a" stroke-width="1.1" opacity=".45"/>' for x in [i*0.3-3 for i in range(20)])
add('background',pl)
# window
wl,wr,wb,wt=-.75,.75,1.0,2.3
w0,w1=pr(wl,wt,RZ),pr(wr,wb,RZ);wx0,wy0,wx1,wy1=w0[0],w0[1],w1[0],w1[1]
ww,wh=wx1-wx0,wy1-wy0
defs=defs.replace('</defs>',f'<clipPath id="wc2"><rect x="{wx0}" y="{wy0}" width="{ww}" height="{wh}"/></clipPath></defs>')
X=lambda u:wx0+ww*u;Y=lambda v:wy0+wh*v
view=f'<rect x="{wx0}" y="{wy0}" width="{ww}" height="{wh}" fill="url(#sky)"/><g clip-path="url(#wc2)">'
view+=f'<path d="M{X(0)} {Y(.78)} Q{X(.4)} {Y(.7)} {X(.7)} {Y(.74)} T{X(1)} {Y(.72)} V{Y(1)} H{X(0)}Z" fill="#9db77f"/>'
view+=f'<path d="M{X(.05)} {Y(.8)} h{ww*.15} v-{wh*.1} l{ww*.08} -{wh*.07} l{ww*.08} {wh*.07} v{wh*.1} h{ww*.12} v-{wh*.08} h{ww*.15} v{wh*.08} h{ww*.3} V{Y(1)} H{X(.05)}Z" fill="#d79a74" stroke="{INK}" stroke-width="1.2" opacity=".85"/>'
view+=f'<path d="M{X(.74)} {Y(.72)} v-{wh*.14} q0 -{wh*.05} {ww*.04} -{wh*.06} q-{ww*.01} -{wh*.06} {ww*.035} -{wh*.09} q{ww*.045} {wh*.03} {ww*.035} {wh*.09} q{ww*.04} {wh*.01} {ww*.04} {wh*.06} v{wh*.14}z" fill="#e8ecee" stroke="{INK}" stroke-width="1.2"/><path d="M{X(.815)} {Y(.43)} v-{wh*.04}" stroke="#c9a23a" stroke-width="2.4"/>'
view+=f'<path d="M{X(.3)} {Y(1)} C{X(.31)} {Y(.7)} {X(.29)} {Y(.35)} {X(.33)} 0" stroke="#f3f0e8" stroke-width="9" fill="none"/><path d="M{X(.3)} {Y(1)} C{X(.31)} {Y(.7)} {X(.29)} {Y(.35)} {X(.33)} 0" stroke="{INK}" stroke-width="1" stroke-dasharray="5 16" fill="none"/>'
leaf=''
import random;random.seed(3)
for bx,by in((X(.15),Y(.25)),(X(.45),Y(.15)),(X(.2),Y(.5)),(X(.5),Y(.42))):
    for k in range(14):leaf+=f'<ellipse cx="{bx+random.uniform(-30,30):.1f}" cy="{by+random.uniform(-18,18):.1f}" rx="4" ry="7" fill="{random.choice(["#a8c46a","#b9cf7a","#8fb35a"])}" transform="rotate({random.randint(-40,40)} {bx:.0f} {by:.0f})"/>'
view+=leaf+'</g>'
add('background',view)
add('background',f'<rect x="{wx0-8}" y="{wy0-8}" width="{ww+16}" height="{wh+16}" fill="none" stroke="#f4ecdb" stroke-width="14"/><rect x="{wx0-15}" y="{wy0-15}" width="{ww+30}" height="{wh+30}" fill="none" stroke="{INK}" stroke-width="2"/><rect x="{wx0-1}" y="{wy0-1}" width="{ww+2}" height="{wh+2}" fill="none" stroke="{INK}" stroke-width="1.4"/>')
add('background',f'<path d="M{X(.5)} {wy0} V{wy1} M{wx0} {Y(.38)} H{wx1}" stroke="#f4ecdb" stroke-width="9"/><path d="M{X(.5)} {wy0} V{wy1} M{wx0} {Y(.38)} H{wx1}" stroke="{INK}" stroke-width="1" opacity=".55"/>')
add('background',box(-.88,.88,.94,1.0,3.78,RZ,'#f4ecdb',sw=1.6))
add('radiator',''.join(box(x,x+.09,.15,.72,3.86,3.95,'#ffffff',sw=1.3,side=-.08) for x in [-.6+i*.12 for i in range(10)]))
# curtains
s=sc(3.9)
def curtain(xa,xb,flip):
    a,b=pr(xa,2.45,3.9),pr(xb,.85,3.9)
    x0,x1,y0,y1=a[0],b[0],a[1],b[1]
    if flip:x0,x1=x1,x0
    d=f'M{x0} {y0} C{x0+(x1-x0)*.7} {y0} {x1} {y0+8} {x1} {y0+14} C{x1-(x1-x0)*.25} {y0+(y1-y0)*.45} {x1+(x1-x0)*.05} {y0+(y1-y0)*.75} {x1-(x1-x0)*.1} {y1} L{x0} {y1} Z'
    folds=''.join(path(f'M{x0+(x1-x0)*t} {y0+6} Q{x0+(x1-x0)*(t+.05)} {(y0+y1)/2} {x0+(x1-x0)*(t-.02)} {y1}',sw=1.2,stroke='#5e2c40') for t in(.3,.6))
    tie=path(f'M{x0} {y0+(y1-y0)*.55} Q{(x0+x1)/2} {y0+(y1-y0)*.6} {x1-(x1-x0)*.05} {y0+(y1-y0)*.52}',sw=4,stroke='#d9b25a')
    return path(d,'#4fbfb7')+f'<path d="{d}" fill="url(#pDotsC)"/>'+folds.replace('#5e2c40','#3aa39c')+tie.replace('#d9b25a','#ffb547')
add('curtains',curtain(-1.25,-.68,False)+curtain(.68,1.25,True))
r0,r1=pr(-1.35,2.47,3.9),pr(1.35,2.47,3.9)
add('curtains',f'<line x1="{r0[0]}" y1="{r0[1]}" x2="{r1[0]}" y2="{r1[1]}" stroke="{INK}" stroke-width="5" stroke-linecap="round"/><circle cx="{r0[0]}" cy="{r0[1]}" r="7" fill="#c99a45" stroke="{INK}" stroke-width="1.4"/><circle cx="{r1[0]}" cy="{r1[1]}" r="7" fill="#c99a45" stroke="{INK}" stroke-width="1.4"/>')
# geraniums on sill
for gx in(-.55,-.2):
    base=pr(gx,1.0,3.88);s=sc(3.88);w=.22*s;h=.18*s
    g=path(f'M{base[0]-w/2} {base[1]-h} h{w} l-{w*.12} {h} h-{w*.76}z','#c9673c',sw=1.6)
    for dx,dy,r in((-.5,-.2,-25),(.5,-.15,25),(0,-.45,0)):g+=f'<ellipse cx="{base[0]+dx*w*.6:.1f}" cy="{base[1]-h+dy*h*.9:.1f}" rx="{w*.38:.1f}" ry="{w*.24:.1f}" fill="#6f9a52" stroke="{INK}" stroke-width="1.2" transform="rotate({r} {base[0]+dx*w*.6:.1f} {base[1]-h+dy*h*.9:.1f})"/>'
    for dx,dy in((-.25,-.9),(.2,-1.0),(0,-1.25)):g+=f'<circle cx="{base[0]+dx*w:.1f}" cy="{base[1]-h+dy*h:.1f}" r="{w*.2:.1f}" fill="#d9434e" stroke="{INK}" stroke-width="1.1"/>'
    add('geraniums',g)
# cat on sill (reuse drawing, scaled)
cs=src[src.index("cx,cy=668,352"):src.index("# ---------- wall decor")]
catpos=pr(.6,1.0,3.88)
cat_code=cs.replace("cx,cy=668,352","cx,cy=0,-52")
Lc={};exec(cat_code.replace("add(","addc("),{'path':path,'INK':INK,'addc':lambda k,v:Lc.__setitem__(k,Lc.get(k,'')+v)})
add('cat',f'<g transform="translate({catpos[0]:.1f} {catpos[1]:.1f}) scale({sc(3.88)/210:.3f})">{Lc["cat"]}</g>')
# clock on back wall
c=pr(-1.8,2.3,RZ);s=sc(RZ)
add('clock',f'<g transform="translate({c[0]:.1f} {c[1]:.1f}) scale({s/150:.3f})">'+path('M-34 -40 h68 v74 l-34 26 l-34 -26z','#a8703f')+f'<circle cx="0" cy="0" r="24" fill="#f6efe0" stroke="{INK}" stroke-width="2"/>'+path('M0 0 V-16 M0 0 L11 6',sw=2.6)+path('M-14 60 V150 M14 60 V128',sw=1.4)+f'<path d="M-20 150 h12 l-2 28 h-8z M8 128 h12 l-2 28 h-8z" fill="#5e4a2c" stroke="{INK}" stroke-width="1.4"/>'+path('M0 60 L4 104',sw=1.4)+f'<circle cx="4" cy="110" r="8" fill="#d8b34a" stroke="{INK}" stroke-width="1.4"/></g>')
# shelf + books
add('shelf',box(1.1,2.2,1.62,1.67,3.78,RZ,'#8a5634',sw=1.6))
bx=1.15
for col,w,h in(('#8c3f35',.09,.32),('#3e5f84',.08,.28),('#c99a45',.1,.36),('#4f7a4a',.07,.27),('#7d3f55',.09,.31)):
    add('shelf',box(bx,bx+w,1.67,1.67+h,3.8,3.97,col,sw=1.3));bx+=w+.012
add('shelf',box(1.9,2.05,1.67,1.8,3.84,3.95,'#c9673c',sw=1.3))
p=pr(1.97,1.8,3.9);s=sc(3.9)
add('shelf',''.join(f'<ellipse cx="{p[0]+dx*s:.1f}" cy="{p[1]-dy*s:.1f}" rx="{.035*s:.1f}" ry="{.09*s:.1f}" fill="#6f9a52" stroke="{INK}" stroke-width="1.1" transform="rotate({r} {p[0]+dx*s:.1f} {p[1]-dy*s:.1f})"/>' for dx,dy,r in((-.06,.06,-25),(.06,.06,25),(0,.1,0))))
# pictures on left wall (plane x=-3)
def wallquad(z0,z1,y0,y1):return [pr(-RX,y0,z0),pr(-RX,y0,z1),pr(-RX,y1,z1),pr(-RX,y1,z0)]
add('pictures',poly(wallquad(2.55,3.45,1.35,1.95),'#9c6a3c',sw=2)+poly(wallquad(2.62,3.38,1.42,1.88),'#f1e6cc',sw=1.4))
q=wallquad(2.62,3.38,1.42,1.88)
add('pictures',poly([lerp(q[0],q[1],0),lerp(q[0],q[1],1),lerp(q[1],q[2],.45),lerp(q[1],q[2],.2)*0 if False else lerp(q[0],q[3],.35)],'#9db77f',sw=1))
add('pictures',poly(wallquad(1.95,2.35,1.45,1.9),'#c99a45',sw=2)+poly(wallquad(2.0,2.3,1.5,1.85),'#efe1c2',sw=1.2))
# ballet shoes on left wall
p=pr(-RX,1.75,2.0);s=sc(2.0)
add('shoes',f'<g transform="translate({p[0]:.1f} {p[1]:.1f}) scale({s/300:.3f}) skewY(18)">'+path('M0 0 L6 8',sw=3)+path('M6 8 C-8 30 -18 60 -12 80 C-8 96 10 92 12 78 C14 58 10 30 6 8Z','#f2b8c0')+path('M6 8 C18 32 26 62 22 82 C20 98 38 100 40 84 C42 62 26 30 6 8Z','#f2b8c0')+path('M6 8 C2 40 -6 100 -18 130 M6 8 C14 40 26 100 34 134',sw=2,stroke='#e08aa0')+'</g>')
# wardrobe on right wall
add('wardrobe',box(2.02,RX,0,2.05,1.15,2.35,'#9a6440',sw=2,side=-.05))
for z0,z1 in((1.2,1.73),(1.77,2.3)):
    add('wardrobe',poly([pr(2.02,.15,z0),pr(2.02,.15,z1),pr(2.02,1.9,z1),pr(2.02,1.9,z0)],'#a8724a',sw=1.4))
    add('wardrobe',poly([pr(2.02,.3,z0+.06),pr(2.02,.3,z1-.06),pr(2.02,1.75,z1-.06),pr(2.02,1.75,z0+.06)],'#ad7a50',sw=1))
for z in(1.69,1.81):h=pr(2.02,1.05,z);add('wardrobe',f'<circle cx="{h[0]:.1f}" cy="{h[1]:.1f}" r="5" fill="#d1a24a" stroke="{INK}" stroke-width="1.2"/>')
add('wardrobe',box(1.98,RX,2.05,2.12,1.1,2.4,'#8a5634',sw=1.6))
add('wardrobe',box(2.1,2.5,2.12,2.35,1.4,2.0,'#c4914f',sw=1.6))
# rug
rug=''
for i,c in enumerate(['#ff6b6b','#ffd166','#4d96ff','#ffffff','#ff8fab']):
    rx,rz=1.25-i*.22,.8-i*.14
    P=[pr(.1+rx*math.cos(a),0.002,2.35+rz*math.sin(a)) for a in [k*math.pi/36 for k in range(72)]]
    rug+=poly(P,c,sw=2 if i==0 else 1.2)
add('rug',rug)
# shadows
def fp(x0,x1,z0,z1,g=.12):return poly([pr(x0-g,0,z0-g),pr(x1+g,0,z0-g),pr(x1+g,0,z1),pr(x0-g,0,z1)],'#3a2416',sw=0)
add('shadows',f'<g filter="url(#soft)" opacity=".38">{fp(-2.6,-1.48,1.7,4)}{fp(1.0,2.33,3.25,4)}{fp(2.02,2.6,1.1,2.4,.15)}{fp(1.35,1.9,2.55,3.1,.08)}<rect x="0" y="{pr(0,RH,RZ)[1]-6}" width="1200" height="30" fill="#5a3a20"/></g>')
# AO
ao=''
for P in ([pr(-RX,0,RZ),pr(RX,0,RZ),pr(RX,.25,RZ),pr(-RX,.25,RZ)],[pr(-RX,0,0),pr(-RX,0,RZ),pr(-RX+.3,0,RZ),pr(-RX+.3,0,0)],[pr(RX,0,0),pr(RX,0,RZ),pr(RX-.3,0,RZ),pr(RX-.3,0,0)],[pr(-RX,RH,RZ),pr(RX,RH,RZ),pr(RX,RH-.3,RZ),pr(-RX,RH-.3,RZ)]):ao+=f'<polygon points="{pts(P)}" fill="#3a2416"/>'
add('ao',f'<g filter="url(#soft)" opacity=".35">{ao}</g>')
ray=''
for a,b in ((-.7,-.2),(-.1,.3),(.4,.72)):
    P=[pr(a,2.25,3.95),pr(b,2.25,3.95),pr(b+.9,0,1.0),pr(a+.6,0,1.0)];ray+=f'<polygon points="{pts(P)}" fill="#fff6d8" opacity=".16"/>'
add('rays','')
# bed
bx0,bx1,bz0,bz1=-RX+.02,-1.5,1.75,3.94
for x in(bx0+.04,bx1-.08):
    for z in(bz0+.02,bz1-.08):add('bed',box(x,x+.06,0,.26,z,z+.06,'#5e3a22',sw=1.3))
add('bed',box(bx0,bx1,.24,.44,bz0,bz1,'#8a5634',sw=2))
# headboard iron
def ironend(z,hgt):
    s=''
    pa,pb=pr(bx0+.02,0,z),pr(bx1-.02,0,z)
    for x in(bx0+.02,bx1-.03):
        a,b=pr(x,0,z),pr(x,hgt,z);s+=f'<line x1="{a[0]:.1f}" y1="{a[1]:.1f}" x2="{b[0]:.1f}" y2="{b[1]:.1f}" stroke="#efe8da" stroke-width="{.05*sc(z):.1f}" stroke-linecap="round"/><line x1="{a[0]:.1f}" y1="{a[1]:.1f}" x2="{b[0]:.1f}" y2="{b[1]:.1f}" stroke="{INK}" stroke-width="1.2" opacity=".7"/>'
    t0,t1=pr(bx0+.02,hgt-.06,z),pr(bx1-.03,hgt-.06,z);mid=pr((bx0+bx1)/2,hgt+.12,z)
    s+=path(f'M{t0[0]:.1f} {t0[1]:.1f} Q{mid[0]:.1f} {mid[1]-(t0[1]-mid[1]):.1f} {t1[0]:.1f} {t1[1]:.1f}',sw=.035*sc(z),stroke='#efe8da')+path(f'M{t0[0]:.1f} {t0[1]:.1f} Q{mid[0]:.1f} {mid[1]-(t0[1]-mid[1]):.1f} {t1[0]:.1f} {t1[1]:.1f}',sw=1.2)
    for i in range(1,7):
        x=bx0+(bx1-bx0)*i/7;a,b=pr(x,.44,z),pr(x,hgt-.05,z);s+=f'<line x1="{a[0]:.1f}" y1="{a[1]:.1f}" x2="{b[0]:.1f}" y2="{b[1]:.1f}" stroke="#efe8da" stroke-width="{.02*sc(z):.1f}"/><line x1="{a[0]:.1f}" y1="{a[1]:.1f}" x2="{b[0]:.1f}" y2="{b[1]:.1f}" stroke="{INK}" stroke-width=".9" opacity=".6"/>'
    for x in(bx0+.02,bx1-.03):k=pr(x,hgt+.02,z);s+=f'<circle cx="{k[0]:.1f}" cy="{k[1]:.1f}" r="{.045*sc(z):.1f}" fill="#d1a24a" stroke="{INK}" stroke-width="1.3"/>'
    return s
add('bedhead',ironend(3.93,1.15))
add('mattress',box(bx0+.02,bx1-.02,.44,.6,bz0+.03,bz1-.05,'#f6f1e6',sw=1.6))
for pz in(3.55,):
    for px0,px1 in((bx0+.08,(bx0+bx1)/2-.02),((bx0+bx1)/2+.02,bx1-.08)):
        a,b,c_,d=pr(px0,.6,pz+.33),pr(px1,.6,pz+.33),pr(px1,.62,pz),pr(px0,.62,pz)
        add('pillows',path(f'M{d[0]:.1f} {d[1]:.1f} Q{(d[0]+c_[0])/2:.1f} {d[1]-.16*sc(pz):.1f} {c_[0]:.1f} {c_[1]:.1f} L{b[0]:.1f} {b[1]-.1*sc(pz):.1f} Q{(a[0]+b[0])/2:.1f} {a[1]-.2*sc(pz+.3):.1f} {a[0]:.1f} {a[1]-.1*sc(pz):.1f} Z','#fbf7ee',sw=1.8))
# quilt: top + side drape + foot drape
qx0,qx1,qy,qz0,qz1=bx0,bx1+.03,.63,bz0-.02,3.42
cols=['#b8584a','#e2b85a','#5f7f9a','#7f9a5a','#d48fa0','#e9dcc0','#c9767a','#4f7a6a']
top=[pr(qx0,qy,qz0),pr(qx1,qy,qz0),pr(qx1,qy,qz1),pr(qx0,qy,qz1)]
add('quilt',grid(top,3,4,cols))
side=[pr(qx1,qy,qz0),pr(qx1,qy,qz1),pr(qx1,.3,qz1),pr(qx1,.3,qz0)]
add('quilt',grid([side[0],side[1],side[2],side[3]],4,1,cols[2:]+cols[:2]))
foot=[pr(qx0,qy,qz0),pr(qx1,qy,qz0),pr(qx1,.3,qz0),pr(qx0,.3,qz0)]
add('quilt',grid(foot,3,1,cols[3:]+cols))
fe=pr(qx0,.3,qz0);fe2=pr(qx1,.3,qz0);n=8;dx=(fe2[0]-fe[0])/n
add('quilt',path(f'M{fe[0]:.1f} {fe[1]:.1f} '+' '.join(f'q{dx/2:.1f} 7 {dx:.1f} 0' for _ in range(n)),sw=1.4))
add('bedfoot',ironend(bz0,.82))
# basket
p=pr(-1.2,0,1.35);s=sc(1.35)
bw,bh=.42*s,.26*s
bk_='';bk_+=path(f'M{p[0]-bw/2:.1f} {p[1]-bh:.1f} L{p[0]+bw/2:.1f} {p[1]-bh:.1f} L{p[0]+bw*.42:.1f} {p[1]:.1f} L{p[0]-bw*.42:.1f} {p[1]:.1f}Z','#c4914f',sw=1.8)
bk_+=''.join(f'<line x1="{p[0]-bw/2+i*bw/8:.1f}" y1="{p[1]-bh:.1f}" x2="{p[0]-bw*.42+i*bw*.84/8:.1f}" y2="{p[1]:.1f}" stroke="{INK}" stroke-width="1" opacity=".45"/>' for i in range(1,8))
for dx,c in((-.3,'#b8584a'),(0,'#5f7f9a'),(.28,'#e2b85a')):bk_+=f'<circle cx="{p[0]+dx*bw:.1f}" cy="{p[1]-bh-.06*s:.1f}" r="{.08*s:.1f}" fill="{c}" stroke="{INK}" stroke-width="1.6"/>'
add('basket',bk_)
# desk
dx0,dx1,dz0,dz1,dy=1.0,2.3,3.28,3.96,.76
for x in(dx0+.03,dx1-.08):
    for z in(dz0+.03,dz1-.08):add('desk',box(x,x+.05,0,dy-.04,z,z+.05,'#6e4228',sw=1.3))
add('desk',box(1.8,dx1-.03,.38,dy-.04,dz0+.02,dz1-.04,'#8a5634',sw=1.6))
for y0 in(.42,.58):
    add('desk',poly([pr(1.84,y0,dz0+.02),pr(dx1-.07,y0,dz0+.02),pr(dx1-.07,y0+.13,dz0+.02),pr(1.84,y0+.13,dz0+.02)],'#9a6440',sw=1.2))
    h=pr(2.05,y0+.065,dz0+.02);add('desk',f'<circle cx="{h[0]:.1f}" cy="{h[1]:.1f}" r="4.5" fill="#d1a24a" stroke="{INK}" stroke-width="1.1"/>')
add('desk',box(dx0,dx1,dy-.04,dy,dz0,dz1,'#a86c3e',sw=2))
# desk items
b=pr(1.32,dy,3.72);s=sc(3.72)
add('lamp',f'<ellipse cx="{b[0]:.1f}" cy="{b[1]:.1f}" rx="{.1*s:.1f}" ry="{.03*s:.1f}" fill="#3d3a3a" stroke="{INK}" stroke-width="1.4"/>'+path(f'M{b[0]:.1f} {b[1]:.1f} L{b[0]-.04*s:.1f} {b[1]-.3*s:.1f} L{b[0]+.12*s:.1f} {b[1]-.44*s:.1f}',sw=3.4,stroke='#3d3a3a')+path(f'M{b[0]+.06*s:.1f} {b[1]-.5*s:.1f} h{.2*s:.1f} l{.07*s:.1f} {.16*s:.1f} h-{.34*s:.1f}z','url(#green)',sw=1.8))
add('lampglow',f'<ellipse cx="{b[0]+.15*s:.1f}" cy="{b[1]-.05*s:.1f}" rx="{.6*s:.1f}" ry="{.3*s:.1f}" fill="url(#glowLamp)"/>')
g=pr(2.08,dy+.17,3.7);s=sc(3.7)
add('globe',path(f'M{g[0]:.1f} {g[1]+.15*s:.1f} V{g[1]+.2*s:.1f} M{g[0]-.07*s:.1f} {g[1]+.2*s:.1f} H{g[0]+.07*s:.1f}',sw=2.6)+f'<circle cx="{g[0]:.1f}" cy="{g[1]:.1f}" r="{.13*s:.1f}" fill="#7fa8c0" stroke="{INK}" stroke-width="1.8"/>'+path(f'M{g[0]-.1*s:.1f} {g[1]-.04*s:.1f} Q{g[0]:.1f} {g[1]+.02*s:.1f} {g[0]+.1*s:.1f} {g[1]-.06*s:.1f} M{g[0]-.06*s:.1f} {g[1]+.07*s:.1f} Q{g[0]:.1f} {g[1]+.03*s:.1f} {g[0]+.07*s:.1f} {g[1]+.08*s:.1f}',sw=1.5,stroke='#5f8a4f')+path(f'M{g[0]-.16*s:.1f} {g[1]:.1f} A{.16*s:.1f} {.16*s:.1f} 0 0 1 {g[0]+.02*s:.1f} {g[1]-.16*s:.1f}',sw=2))
for i,(c,w) in enumerate((('#8c3f35',.32),('#3e5f84',.28),('#c99a45',.3))):add('books',box(1.55+i*.01,1.55+w,dy+i*.045,dy+.04+i*.045,3.5,3.72,c,sw=1.2))
# chair (front of desk, back toward viewer)
cx0,cx1,cz0,cz1,sy=1.42,1.88,2.62,3.06,.46
for x in(cx0+.02,cx1-.06):
    for z in(cz0+.02,cz1-.06):add('chair',box(x,x+.04,0,sy,z,z+.04,'#5e3a22',sw=1.2))
add('chair',box(cx0,cx1,sy,sy+.05,cz0,cz1,'#8a5634',sw=1.8))
for x in(cx0+.03,cx1-.07):add('chair',box(x,x+.04,sy+.05,1.0,cz0+.01,cz0+.05,'#5e3a22',sw=1.2))
a,b_,m=pr(cx0+.05,.98,cz0+.03),pr(cx1-.05,.98,cz0+.03),pr((cx0+cx1)/2,1.1,cz0+.03)
add('chair',path(f'M{a[0]:.1f} {a[1]:.1f} Q{m[0]:.1f} {m[1]-(a[1]-m[1]):.1f} {b_[0]:.1f} {b_[1]:.1f}',sw=.045*sc(cz0),stroke='#5e3a22')+path(f'M{a[0]:.1f} {a[1]:.1f} Q{m[0]:.1f} {m[1]-(a[1]-m[1]):.1f} {b_[0]:.1f} {b_[1]:.1f}',sw=1.2))
a,b_=pr(cx0+.05,.72,cz0+.03),pr(cx1-.05,.72,cz0+.03);add('chair',path(f'M{a[0]:.1f} {a[1]:.1f} Q{(a[0]+b_[0])/2:.1f} {a[1]+10:.1f} {b_[0]:.1f} {b_[1]:.1f}',sw=5,stroke='#5e3a22'))
# abazhur
top_=pr(0,RH,1.6);s=sc(1.6);c=pr(0,2.42,1.6)
ab=path(f'M{top_[0]:.1f} {top_[1]:.1f} V{c[1]-.1*s:.1f}',sw=1.8)
ab+=path(f'M{c[0]-.14*s:.1f} {c[1]-.1*s:.1f} L{c[0]+.14*s:.1f} {c[1]-.1*s:.1f} L{c[0]+.34*s:.1f} {c[1]+.12*s:.1f} Q{c[0]:.1f} {c[1]+.2*s:.1f} {c[0]-.34*s:.1f} {c[1]+.12*s:.1f}Z','url(#shade)',sw=1.8)
ab+=''.join(f'<line x1="{c[0]+x*s:.1f}" y1="{c[1]+.12*s+ (0.08*s)*(1-(x/.34)**2):.1f}" x2="{c[0]+x*s:.1f}" y2="{c[1]+.2*s+(0.08*s)*(1-(x/.34)**2):.1f}" stroke="#b55a22" stroke-width="2"/>' for x in [i*.04-.32 for i in range(17)])
add('abazhur',ab)
wp0,wp1=pr(-.75,0,3.9),pr(.75,0,3.9);fp1,fp0=pr(1.5,0,.6),pr(-.4,0,.6)
add('glow','')
# appended into rich.py before LAY
import detail as DT
DT.lg=lambda col,*a,**k:col
DT.rg=lambda col,*a,**k:col
def affine(TN,TF,BN,w,h,inner):
    a=(TF[0]-TN[0])/w;b=(TF[1]-TN[1])/w;c=(BN[0]-TN[0])/h;d=(BN[1]-TN[1])/h
    return f'<g transform="matrix({a:.4f} {b:.4f} {c:.4f} {d:.4f} {TN[0]:.1f} {TN[1]:.1f})">{inner}</g>'
# --- pictures on left wall
L['pictures']=affine(pr(-RX,1.98,2.55),pr(-RX,1.98,3.45),pr(-RX,1.36,2.55),150,112,DT.landscape())
L['pictures']+=affine(pr(-RX,1.92,1.95),pr(-RX,1.92,2.32),pr(-RX,1.45,1.95),60,80,f'<rect width="60" height="80" rx="30" fill="{DT.lg("#c99a45")}" stroke="#5e3a1e" stroke-width="1.2"/><rect x="7" y="7" width="46" height="66" rx="23" fill="#efe1c2"/>'+DT.place(DT.geranium(9,'#c9767a'),30,62,.55))
# --- shoes
p=pr(-RX,1.85,2.15);L['shoes']=affine(p,pr(-RX,1.85,2.45),pr(-RX,1.3,2.15),60,110,DT.place(DT.pointes(),30,4,.9))
# --- geraniums
L['geraniums']=''
for gx,seed,col in((-.55,1,'#d93f4c'),(-.2,2,'#e2566a')):
    b=pr(gx,1.0,3.86);L['geraniums']+=DT.place(DT.geranium(seed,col),b[0],b[1],sc(3.86)*.24/34)
# --- cat
cp=pr(.6,1.0,3.86);L['cat']=DT.place(DT.cat(),cp[0],cp[1],sc(3.86)*.42/120)
# --- clock
cc=pr(-1.8,2.3,RZ);L['clock']=DT.place(DT.clock(),cc[0],cc[1],sc(RZ)*.28/50)
# --- basket
bp=pr(-1.2,0,1.45);L['basket']=DT.place(DT.basket(),bp[0],bp[1],sc(1.45)*.46/90)
# --- one pillow with lace
pz0,pz1=3.5,3.88;px0,px1=bx0+.12,bx1-.12
A,B,C,D=pr(px0,.62,pz1),pr(px1,.62,pz1),pr(px1,.62,pz0),pr(px0,.62,pz0)
lift=.2*sc(3.7)
pd=f'M{D[0]:.1f} {D[1]:.1f} Q{(D[0]+A[0])/2-6:.1f} {(D[1]+A[1])/2-lift*.6:.1f} {A[0]:.1f} {A[1]-lift*.3:.1f} Q{(A[0]+B[0])/2:.1f} {A[1]-lift*.9:.1f} {B[0]:.1f} {B[1]-lift*.3:.1f} Q{(B[0]+C[0])/2+6:.1f} {(B[1]+C[1])/2-lift*.6:.1f} {C[0]:.1f} {C[1]:.1f} Q{(C[0]+D[0])/2:.1f} {C[1]+lift*.25:.1f} {D[0]:.1f} {D[1]:.1f}Z'
lace=''
n=14
for i in range(n+1):
    t=i/n;x=D[0]+(C[0]-D[0])*t;y=D[1]+(C[1]-D[1])*t+lift*.25*math.sin(math.pi*t)
    lace+=f'<circle cx="{x:.1f}" cy="{y+3:.1f}" r="4.2" fill="#fbf7ee" stroke="#cdbfa6" stroke-width=".8"/><circle cx="{x:.1f}" cy="{y+3:.1f}" r="1.3" fill="#e6dccb"/>'
L['pillows']=lace+f'<path d="{pd}" fill="{DT.lg("#fbf7ee",.1,-.12)}" stroke="#b9ab92" stroke-width="1.2"/>'
mid=((A[0]+B[0]+C[0]+D[0])/4,(A[1]+B[1]+C[1]+D[1])/4-lift*.3)
L['pillows']+=f'<path d="M{mid[0]-24:.1f} {mid[1]+6:.1f} q24 -10 48 0" fill="none" stroke="#d9cbb2" stroke-width="1.2"/>'+''.join(f'<circle cx="{mid[0]+dx:.1f}" cy="{mid[1]-4:.1f}" r="2.4" fill="#e2a0aa"/>' for dx in (-5,0,5))+f'<circle cx="{mid[0]:.1f}" cy="{mid[1]-4:.1f}" r="1.4" fill="#e2b85a"/>'
# --- soft quilt
qx0,qx1,qy,qz0,qz1=bx0,bx1+.03,.64,bz0-.02,3.4
Aq,Bq,Cq,Dq=pr(qx0,qy,qz1),pr(qx1,qy,qz1),pr(qx1,qy,qz0),pr(qx0,qy,qz0)
s0=sc(qz0)
topd=f'M{Aq[0]:.1f} {Aq[1]:.1f} L{Bq[0]:.1f} {Bq[1]:.1f} Q{Bq[0]+(Cq[0]-Bq[0])*.5+4:.1f} {(Bq[1]+Cq[1])/2-3:.1f} {Cq[0]-4:.1f} {Cq[1]-2:.1f} Q{Cq[0]-12:.1f} {Cq[1]+2:.1f} {Cq[0]-22:.1f} {Cq[1]+1:.1f} L{Dq[0]:.1f} {Dq[1]:.1f}Z'
GD.append(f'<clipPath id="qtop"><path d="{topd}"/></clipPath>')
cols=['#ff6b6b','#ffd166','#4d96ff','#6bcb77','#ff8fab','#ffffff','#b98aff','#4fbfb7']
pats=['pDots','pFlow','pStr','pGing']
qt=f'<path d="{topd}" fill="#ef7d95"/><g clip-path="url(#qtop)">'
k=0
for i in range(3):
    for j in range(4):
        Q=lambda u,v:lerp(lerp(Dq,Cq,u),lerp(Aq,Bq,u),v)
        P=[Q(i/3,j/4),Q((i+1)/3,j/4),Q((i+1)/3,(j+1)/4),Q(i/3,(j+1)/4)]
        pass
for i in range(1,3):
    a,b=lerp(Dq,Cq,i/3),lerp(Aq,Bq,i/3);qt+=f'<line x1="{a[0]:.1f}" y1="{a[1]:.1f}" x2="{b[0]:.1f}" y2="{b[1]:.1f}" stroke="none"/>'
for j in range(1,4):
    a,b=lerp(Dq,Aq,j/4),lerp(Cq,Bq,j/4);qt+=f'<line x1="{a[0]:.1f}" y1="{a[1]:.1f}" x2="{b[0]:.1f}" y2="{b[1]:.1f}" stroke="none"/>'
qt+=f'<ellipse cx="{(Aq[0]+Cq[0])/2:.1f}" cy="{(Aq[1]+Cq[1])/2:.1f}" rx="{abs(Cq[0]-Aq[0])*.45:.1f}" ry="{abs(Cq[1]-Aq[1])*.35:.1f}" fill="#fff" opacity=".12"/></g>'
qt+=f'<path d="{topd}" fill="none" stroke="#c95570" stroke-width="1"/>'
# drape: side (x=qx1) + rounded corner + foot (z=qz0)
Bh,Ch,Dh=pr(qx1+.04,.4,qz1),pr(qx1+.05,.37,qz0-.05),pr(qx0,.39,qz0-.05)
hem=f'{Bh[0]:.1f} {Bh[1]:.1f}'
dr=f'M{Bq[0]:.1f} {Bq[1]:.1f} Q{Bq[0]+(Cq[0]-Bq[0])*.5+4:.1f} {(Bq[1]+Cq[1])/2-3:.1f} {Cq[0]-4:.1f} {Cq[1]-2:.1f} Q{Cq[0]-12:.1f} {Cq[1]+2:.1f} {Cq[0]-22:.1f} {Cq[1]+1:.1f} L{Dq[0]:.1f} {Dq[1]:.1f} L{Dh[0]:.1f} {Dh[1]:.1f} '
nw=10
for i in range(1,nw+1):
    t=i/nw;x=Dh[0]+(Ch[0]-14-Dh[0])*t;y=Dh[1]+(Ch[1]+6-Dh[1])*t+(5 if i%2 else -2)
    dr+=f'Q{x-(Ch[0]-Dh[0])/nw/2:.1f} {y+6:.1f} {x:.1f} {y:.1f} '
dr+=f'Q{Ch[0]+2:.1f} {Ch[1]+8:.1f} {Ch[0]+4:.1f} {Ch[1]-6:.1f} '
for i in range(1,7):
    t=i/6;x=Ch[0]+4+(Bh[0]-Ch[0]-4)*t;y=Ch[1]-6+(Bh[1]-Ch[1]+6)*t+(3 if i%2 else -1)
    dr+=f'Q{x+2:.1f} {y+4:.1f} {x:.1f} {y:.1f} '
dr+='Z'
GD.append(f'<clipPath id="qdr"><path d="{dr}"/></clipPath><pattern id="pPatch" width="96" height="76" patternUnits="userSpaceOnUse"><rect width="48" height="38" fill="#ff6b6b"/><rect x="48" width="48" height="38" fill="#ffd166"/><rect y="38" width="48" height="38" fill="#4d96ff"/><rect x="48" y="38" width="48" height="38" fill="#ff8fab"/><path d="M48 0V76M0 38H96" stroke="#fff6e6" stroke-width="1.2" stroke-dasharray="4 3"/></pattern><linearGradient id="drG" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#3a1a10" stop-opacity=".35"/></linearGradient>')
qd=f'<path d="{dr}" fill="#e06a84"/><g clip-path="url(#qdr)"><rect x="0" y="0" width="1200" height="900" fill="url(#drG)" opacity=".7"/>'
for i in range(1,9):
    t=i/9;a=lerp(Dq,(Cq[0]-22,Cq[1]+1),t);b=lerp(Dh,(Ch[0]-14,Ch[1]+6),t)
    qd+=f'<path d="M{a[0]:.1f} {a[1]+2:.1f} Q{(a[0]+b[0])/2+4:.1f} {(a[1]+b[1])/2:.1f} {b[0]:.1f} {b[1]+2:.1f}" stroke="#3a1a10" stroke-width="5" opacity=".14" fill="none"/>'
for i in range(1,5):
    t=i/5;a=lerp(Cq,Bq,t);b=lerp(Ch,Bh,t)
    qd+=f'<path d="M{a[0]:.1f} {a[1]:.1f} Q{(a[0]+b[0])/2-3:.1f} {(a[1]+b[1])/2:.1f} {b[0]:.1f} {b[1]:.1f}" stroke="#3a1a10" stroke-width="4" opacity=".14" fill="none"/>'
qd+=f'<path d="M{Cq[0]-10:.1f} {Cq[1]:.1f} Q{Ch[0]-8:.1f} {(Cq[1]+Ch[1])/2:.1f} {Ch[0]:.1f} {Ch[1]:.1f}" stroke="#fff" stroke-width="6" opacity=".18" fill="none"/></g>'
qd+=f'<path d="{dr}" fill="none" stroke="#c95570" stroke-width="1"/>'
L['quilt']=qd+qt
# --- books bands
L['shelf']=L['shelf']
# --- floor grain + rug braid
fl=[pr(-RX,0,0),pr(RX,0,0),pr(RX,0,RZ),pr(-RX,0,RZ)]
L['background']+=f'<polygon points="{pts(fl)}" fill="url(#pGrain)" opacity=".8"/>'
br=''
for i in range(5):
    rx,rz=1.25-i*.22,.8-i*.14
    for k in range(0,90):
        a=k/90*2*math.pi;p1=pr(.1+rx*math.cos(a),.004,2.35+rz*math.sin(a));p2=pr(.1+(rx-.06)*math.cos(a+.03),.004,2.35+(rz-.06)*math.sin(a+.03))
        br+=f'<line x1="{p1[0]:.1f}" y1="{p1[1]:.1f}" x2="{p2[0]:.1f}" y2="{p2[1]:.1f}" stroke="#3a1a10" stroke-width="1" opacity=".22"/>'
L['rug']+=br
# --- curtains pattern overlay
L['curtains']=L['curtains'] or re.sub(r'(<path d="([^"]+)" fill="url\(#g\d+\)"[^>]*/>)',lambda m:m.group(1)+f'<path d="{m.group(2)}" fill="url(#pCurt)"/>',L['curtains'])
GD.append(DT.patterns()+'<pattern id="pDotsC" width="18" height="18" patternUnits="userSpaceOnUse"><circle cx="5" cy="5" r="2.6" fill="#fff" opacity=".75"/><circle cx="14" cy="14" r="2.6" fill="#fff" opacity=".75"/></pattern>');GD.extend(DT.GDEFS)

WCM='<filter id="wcm" x="-3%" y="-3%" width="106%" height="106%"><feTurbulence type="fractalNoise" baseFrequency="0.04" numOctaves="2" seed="4" result="n"/><feDisplacementMap in="SourceGraphic" in2="n" scale="1.8" xChannelSelector="R" yChannelSelector="G" result="d"/><feTurbulence type="fractalNoise" baseFrequency="0.08" numOctaves="3" seed="11" result="g"/><feColorMatrix in="g" type="matrix" values="0 0 0 0 .86  0 0 0 0 .84  0 0 0 0 .82  -1 -1 0 0 1.05" result="gm"/><feComposite in="gm" in2="d" operator="in" result="gmi"/><feBlend in="d" in2="gmi" mode="multiply"/></filter>'
VO=[]
def va(t):VO.append(t)
def varya():
    SH='rgba(60,30,60,.16)'
    va(f'<ellipse cx="0" cy="6" rx="56" ry="10" fill="{SH}"/>')
    va('<rect x="-28" y="-70" width="22" height="72" rx="10" fill="#ffe0c7"/><rect x="6" y="-70" width="22" height="72" rx="10" fill="#ffe0c7"/>')
    va('<rect x="-34" y="-8" width="30" height="14" rx="7" fill="#4a3a5a"/><rect x="4" y="-8" width="30" height="14" rx="7" fill="#4a3a5a"/>')
    d='M-48 -64 Q-50 -150 0 -160 Q50 -150 48 -64Z'
    va(f'<path d="{d}" fill="#8fb8ff"/><path d="{d}" fill="url(#pDotsC)"/><rect x="-46" y="-80" width="92" height="12" rx="6" fill="#6f9bf0"/>')
    for sx in(-1,1):va(f'<rect x="{sx*44-9}" y="-150" width="18" height="74" rx="9" fill="#8fb8ff"/><circle cx="{sx*44}" cy="-74" r="11" fill="#ffe0c7"/>')
    va('<path d="M-22 -160 Q0 -146 22 -160 L18 -168 H-18Z" fill="#ffffff"/><rect x="-12" y="-182" width="24" height="26" rx="10" fill="#ffe0c7"/>')
    for k in range(6):va(f'<ellipse cx="56" cy="{-210+k*22}" rx="13" ry="15" fill="#a0522d"/>')
    va('<circle cx="56" cy="-82" r="7" fill="#ff6b8b"/><circle cx="0" cy="-240" r="64" fill="#a0522d"/><ellipse cx="0" cy="-226" rx="56" ry="54" fill="#ffe0c7"/>')
    va('<path d="M-58 -246 Q-50 -306 0 -306 Q50 -306 58 -246 Q40 -268 14 -262 Q20 -250 8 -244 Q2 -264 -24 -262 Q-40 -258 -58 -246Z" fill="#a0522d"/>')
    va('<ellipse cx="-20" cy="-226" rx="7" ry="9" fill="#3a2a3a"/><ellipse cx="20" cy="-226" rx="7" ry="9" fill="#3a2a3a"/><circle cx="-17" cy="-230" r="2.6" fill="#fff"/><circle cx="23" cy="-230" r="2.6" fill="#fff"/>')
    va('<ellipse cx="-34" cy="-206" rx="10" ry="6" fill="#ff9fb3" opacity=".75"/><ellipse cx="34" cy="-206" rx="10" ry="6" fill="#ff9fb3" opacity=".75"/><circle cx="0" cy="-214" r="4" fill="#f2b39a"/>')
    va('<path d="M-12 -198 Q0 -186 12 -198" fill="none" stroke="#c4455e" stroke-width="4" stroke-linecap="round"/>')
    return ''.join(VO)
vp=pr(.62,0,1.7);add('varya',f'<g transform="translate({vp[0]:.1f} {vp[1]:.1f}) scale({sc(1.7)*1.36/306:.3f})">{varya()}</g>')
LAY=['ceiling','background','ao','shadows','radiator','curtains','geraniums','cat','clock','shelf','pictures','shoes','wardrobe','rug','desk','books','globe','lamp','chair','bed','bedhead','mattress','pillows','quilt','bedfoot','basket','abazhur','varya','rays','glow','lampglow']
NOWC={'glow','lampglow','shadows','ceiling'}
def layer(k):
    if k in ('glow','lampglow','rays'):return f'<g id="{k}" style="mix-blend-mode:screen">{L[k]}</g>'
    if k in ('ao','shadows','ceiling'):return f'<g id="{k}">{L[k]}</g>'
    return f'<g id="{k}">{L[k]}</g>'
def svg(keys,paper=True):
    inner='\n'.join(layer(k) for k in keys)
    pp=f'<rect x="0" y="0" width="{W}" height="{H}" filter="url(#paper)" opacity="0"/>' if False else ''
    bgc=f'<rect width="{W}" height="{H}" fill="#f4ead6"/>' if paper else ''
    D=defs.replace('</defs>',''.join(GD)+WCM+'</defs>');return f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="750" viewBox="0 20 {W} 750">{D}{bgc}<g>{inner}</g>{pp}</svg>'
open('mid.svg','w').write(svg(LAY))
json.dump({k:svg([k],paper=False) for k in LAY},open('layersmid.json','w'))
print('ok',len(LAY))
