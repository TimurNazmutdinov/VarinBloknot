import detail as DT
import math,json,re
W,H=1200,900;CX=600;CY=205;F=866;ZC=-1.0;YC=1.8;XC=0.0
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
# AO
ao=''
for P in ([pr(-RX,0,RZ),pr(RX,0,RZ),pr(RX,.25,RZ),pr(-RX,.25,RZ)],[pr(-RX,0,0),pr(-RX,0,RZ),pr(-RX+.3,0,RZ),pr(-RX+.3,0,0)],[pr(RX,0,0),pr(RX,0,RZ),pr(RX-.3,0,RZ),pr(RX-.3,0,0)],[pr(-RX,RH,RZ),pr(RX,RH,RZ),pr(RX,RH-.3,RZ),pr(-RX,RH-.3,RZ)]):ao+=f'<polygon points="{pts(P)}" fill="#3a2416"/>'
add('ao',f'<g filter="url(#soft)" opacity=".35">{ao}</g>')
ray=''
for a,b in ((-.7,-.2),(-.1,.3),(.4,.72)):
    P=[pr(a,2.25,3.95),pr(b,2.25,3.95),pr(b+.9,0,1.0),pr(a+.6,0,1.0)];ray+=f'<polygon points="{pts(P)}" fill="#fff6d8" opacity=".16"/>'
add('rays','')
# bed
DT.rg=lambda col,*a,**k:col
def affine(TN,TF,BN,w,h,inner):
    a=(TF[0]-TN[0])/w;b=(TF[1]-TN[1])/w;c=(BN[0]-TN[0])/h;d=(BN[1]-TN[1])/h
    return f'<g transform="matrix({a:.4f} {b:.4f} {c:.4f} {d:.4f} {TN[0]:.1f} {TN[1]:.1f})">{inner}</g>'
# --- pictures on left wall
L['pictures']=affine(pr(-RX,1.98,2.55),pr(-RX,1.98,3.45),pr(-RX,1.36,2.55),150,112,DT.landscape())
L['pictures']+=affine(pr(-RX,1.92,1.95),pr(-RX,1.92,2.32),pr(-RX,1.45,1.95),60,80,f'<rect width="60" height="80" rx="30" fill="{DT.lg("#c99a45")}" stroke="#5e3a1e" stroke-width="1.2"/><rect x="7" y="7" width="46" height="66" rx="23" fill="#efe1c2"/>'+DT.place(DT.geranium(9,'#c9767a'),30,62,.55))
import detail as DT
DT.lg=lambda col,*a,**k:col
DT.rg=lambda col,*a,**k:col
GDX='''<pattern id="tile" width="34" height="34" patternUnits="userSpaceOnUse"><rect width="34" height="34" fill="#e6f4f1"/><rect width="34" height="34" fill="none" stroke="#c7e2dc" stroke-width="3"/><circle cx="17" cy="17" r="4" fill="#9fd6cc"/></pattern>
<pattern id="gingR" width="22" height="22" patternUnits="userSpaceOnUse"><rect width="22" height="22" fill="#ffffff"/><rect width="11" height="22" fill="#ff6b6b" opacity=".55"/><rect width="22" height="11" fill="#ff6b6b" opacity=".55"/></pattern>
<pattern id="pDotsC" width="18" height="18" patternUnits="userSpaceOnUse"><circle cx="5" cy="5" r="2.6" fill="#fff" opacity=".85"/><circle cx="14" cy="14" r="2.6" fill="#fff" opacity=".85"/></pattern>
<pattern id="floorChk" width="80" height="80" patternUnits="userSpaceOnUse"><rect width="80" height="80" fill="#f3e6cf"/><rect width="40" height="40" fill="#e0c9a6"/><rect x="40" y="40" width="40" height="40" fill="#e0c9a6"/></pattern>'''
GD.append(GDX)
# tiles on back wall (instead of wood panels), checker floor
add('background',poly([pr(-RX,0,RZ),pr(RX,0,RZ),pr(RX,1.45,RZ),pr(-RX,1.45,RZ)],'url(#tile)',sw=0))
fl=[pr(-RX,0,0),pr(RX,0,0),pr(RX,0,RZ),pr(-RX,0,RZ)]
# perspective checker floor
chk=''
for i in range(-13,13):
    for j in range(0,20):
        if (i+j)%2:continue
        x0,x1,z0,z1=i*.2,(i+1)*.2,j*.2,(j+1)*.2
        chk+=f'<polygon points="{pts([pr(x0,0,z0),pr(x1,0,z0),pr(x1,0,z1),pr(x0,0,z1)])}" fill="#dcc29c"/>'
add('background',poly(fl,'#f3e6cf',sw=0)+chk)
add('background',poly([pr(-RX,1.42,RZ),pr(RX,1.42,RZ),pr(RX,1.48,RZ),pr(-RX,1.48,RZ)],'#7fc8b8',sw=0))
# window repaint (keep window from shell, add cafe curtains)
wl,wr=-.75,.75
for xa,xb in((-.8,-.02),(.02,.8)):
    a,b=pr(xa,1.62,3.92),pr(xb,.98,3.92)
    d=f'M{a[0]:.1f} {a[1]:.1f} H{b[0]:.1f} V{b[1]-6:.1f} '+''.join(f'q{(b[0]-a[0])/12:.1f} 12 {(b[0]-a[0])/6:.1f} 0 ' for _ in range(6))+f'L{a[0]:.1f} {b[1]-6:.1f}Z'
    add('curtains',f'<path d="{d}" fill="url(#gingR)"/>')
r0,r1=pr(-.85,1.64,3.92),pr(.85,1.64,3.92)
add('curtains',f'<line x1="{r0[0]:.1f}" y1="{r0[1]:.1f}" x2="{r1[0]:.1f}" y2="{r1[1]:.1f}" stroke="#c98a5a" stroke-width="5" stroke-linecap="round"/>')
v0,v1=pr(-.85,2.42,3.92),pr(.85,2.2,3.92)
add('curtains',f'<path d="M{v0[0]:.1f} {v0[1]:.1f} H{v1[0]:.1f} V{v1[1]:.1f} '+''.join(f'q{-(v1[0]-v0[0])/16:.1f} 14 {-(v1[0]-v0[0])/8:.1f} 0 ' for _ in range(8))+'Z" fill="url(#gingR)"/>')
# herbs + jam on sill
for gx,seed in((-.5,4),):
    b=pr(gx,1.0,3.86);add('sill',DT.place(DT.geranium(seed,'#ff4d6d'),b[0],b[1],sc(3.86)*.24/34))
j=pr(.45,1.0,3.88);s=sc(3.88)
add('sill',f'<rect x="{j[0]-.07*s:.1f}" y="{j[1]-.16*s:.1f}" width="{.14*s:.1f}" height="{.16*s:.1f}" rx="{.03*s:.1f}" fill="#c4455e"/><rect x="{j[0]-.08*s:.1f}" y="{j[1]-.2*s:.1f}" width="{.16*s:.1f}" height="{.05*s:.1f}" fill="url(#gingR)"/>')
j=pr(.15,1.0,3.88)
add('sill',f'<rect x="{j[0]-.06*s:.1f}" y="{j[1]-.12*s:.1f}" width="{.12*s:.1f}" height="{.12*s:.1f}" rx="{.02*s:.1f}" fill="#ffffff"/>'+''.join(f'<ellipse cx="{j[0]+dx*s:.1f}" cy="{j[1]-.17*s:.1f}" rx="{.03*s:.1f}" ry="{.07*s:.1f}" fill="#59c26a" transform="rotate({r} {j[0]+dx*s:.1f} {j[1]-.12*s:.1f})"/>' for dx,r in((-.03,-25),(0,0),(.03,25))))
# ---------- counter + stove (back right) ----------
CZ0,CZ1=3.38,3.98
add('counter',box(.95,2.6,0,.86,CZ0,CZ1,'#7fc8b8',sw=1))
for x0 in(1.75,2.18):
    add('counter',poly([pr(x0,.1,CZ0),pr(x0+.38,.1,CZ0),pr(x0+.38,.78,CZ0),pr(x0,.78,CZ0)],'#95d6c8',sw=1))
    h=pr(x0+.06,.55,CZ0);add('counter',f'<rect x="{h[0]-3:.1f}" y="{h[1]-10:.1f}" width="6" height="22" rx="3" fill="#ffffff"/>')
add('counter',box(.93,2.62,.86,.92,CZ0-.03,CZ1,'#e8b07a',sw=1))
# stove
SX0,SX1=.98,1.66
add('stove',box(SX0,SX1,0,.92,CZ0-.02,CZ1,'#fbfbf7',sw=1))
d0=[pr(SX0+.07,.12,CZ0-.02),pr(SX1-.07,.12,CZ0-.02),pr(SX1-.07,.58,CZ0-.02),pr(SX0+.07,.58,CZ0-.02)]
add('stove',poly(d0,'#e9ece8',sw=1)+poly([lerp(lerp(d0[0],d0[1],.15),lerp(d0[3],d0[2],.15),.25),lerp(lerp(d0[0],d0[1],.85),lerp(d0[3],d0[2],.85),.25),lerp(lerp(d0[0],d0[1],.85),lerp(d0[3],d0[2],.85),.75),lerp(lerp(d0[0],d0[1],.15),lerp(d0[3],d0[2],.15),.75)],'#5a5a66',sw=0))
hb=pr(SX0+.12,.66,CZ0-.02);he=pr(SX1-.12,.66,CZ0-.02);add('stove',f'<line x1="{hb[0]:.1f}" y1="{hb[1]:.1f}" x2="{he[0]:.1f}" y2="{he[1]:.1f}" stroke="#c0c4c0" stroke-width="7" stroke-linecap="round"/>')
for k in range(4):
    q=pr(SX0+.12+k*.15,.8,CZ0-.02);add('stove',f'<circle cx="{q[0]:.1f}" cy="{q[1]:.1f}" r="7" fill="#3a3a44"/><rect x="{q[0]-1.5:.1f}" y="{q[1]-6:.1f}" width="3" height="7" fill="#fff"/>')
for bx_,bz in((SX0+.17,CZ0+.15),(SX1-.17,CZ0+.15),(SX0+.17,CZ1-.17),(SX1-.17,CZ1-.17)):
    q=pr(bx_,.925,bz);r=.11*sc(bz);add('stove',f'<ellipse cx="{q[0]:.1f}" cy="{q[1]:.1f}" rx="{r:.1f}" ry="{r*.32:.1f}" fill="#3a3a44"/><ellipse cx="{q[0]:.1f}" cy="{q[1]:.1f}" rx="{r*.6:.1f}" ry="{r*.19:.1f}" fill="#5a5a66"/>')
# kettle (red enamel with dots) on stove
k=pr(SX0+.17,.925,CZ0+.15);s=sc(CZ0+.15)
kt=f'<g transform="translate({k[0]:.1f} {k[1]:.1f}) scale({s/260:.3f})"><path d="M-46 0 Q-56 -60 -30 -78 H30 Q56 -60 46 0Z" fill="#ff6b6b"/><path d="M-46 0 Q-56 -60 -30 -78 H30 Q56 -60 46 0Z" fill="url(#pDotsC)"/><path d="M44 -40 Q78 -46 86 -76" fill="none" stroke="#ff6b6b" stroke-width="12" stroke-linecap="round"/><ellipse cx="0" cy="-80" rx="32" ry="8" fill="#e85a5a"/><circle cx="0" cy="-90" r="8" fill="#3a3a44"/><path d="M-34 -84 Q0 -140 34 -84" fill="none" stroke="#3a3a44" stroke-width="7"/></g>'
add('kettle',kt)
# pot on back burner
k=pr(SX1-.17,.925,CZ1-.17);s=sc(CZ1-.17)
add('kettle',f'<g transform="translate({k[0]:.1f} {k[1]:.1f}) scale({s/260:.3f})"><rect x="-50" y="-70" width="100" height="70" rx="10" fill="#4d96ff"/><rect x="-56" y="-76" width="112" height="12" rx="6" fill="#6fb0ff"/><rect x="-72" y="-62" width="22" height="10" rx="5" fill="#3a3a44"/><rect x="50" y="-62" width="22" height="10" rx="5" fill="#3a3a44"/><circle cx="0" cy="-30" r="12" fill="#ffffff" opacity=".7"/></g>')
# shelf with jars + utensil rail
add('shelf',box(1.0,2.55,1.72,1.77,3.75,RZ,'#c98a5a',sw=1))
lbl=[('#ffd166','Сахар'),('#e8b07a','Мука'),('#c4455e','Варенье'),('#6bcb77','Чай')]
for i,(col,t) in enumerate(lbl):
    x0=1.06+i*.37;q0=pr(x0,1.77,3.88);q1=pr(x0+.24,1.77,3.88);s=sc(3.88);h=.26*s
    add('shelf',f'<rect x="{q0[0]:.1f}" y="{q0[1]-h:.1f}" width="{q1[0]-q0[0]:.1f}" height="{h:.1f}" rx="{.04*s:.1f}" fill="#eef7f6"/><rect x="{q0[0]+2:.1f}" y="{q0[1]-h*.75:.1f}" width="{q1[0]-q0[0]-4:.1f}" height="{h*.72:.1f}" rx="{.03*s:.1f}" fill="{col}"/><rect x="{q0[0]-2:.1f}" y="{q0[1]-h-8:.1f}" width="{q1[0]-q0[0]+4:.1f}" height="10" rx="4" fill="#ff6b6b"/><rect x="{q0[0]+5:.1f}" y="{q0[1]-h*.55:.1f}" width="{q1[0]-q0[0]-10:.1f}" height="{h*.28:.1f}" rx="3" fill="#fffaf0"/><text x="{(q0[0]+q1[0])/2:.1f}" y="{q0[1]-h*.35:.1f}" text-anchor="middle" font-family="sans-serif" font-size="{h*.17:.1f}" font-weight="700" fill="#5a3d4a">{t}</text>')
rl0,rl1=pr(1.0,1.45,3.97),pr(1.7,1.45,3.97);add('rail',f'<line x1="{rl0[0]:.1f}" y1="{rl0[1]:.1f}" x2="{rl1[0]:.1f}" y2="{rl1[1]:.1f}" stroke="#a0a4a8" stroke-width="5" stroke-linecap="round"/>')
for i,(kind,col) in enumerate((('ladle','#c0c4c8'),('spoon','#c98a5a'),('whisk','#c0c4c8'),('pan','#3a3a44'))):
    q=pr(1.08+i*.18,1.45,3.97);s=sc(3.97)
    if kind=='pan':add('rail',f'<line x1="{q[0]:.1f}" y1="{q[1]:.1f}" x2="{q[0]:.1f}" y2="{q[1]+.12*s:.1f}" stroke="{col}" stroke-width="5"/><circle cx="{q[0]:.1f}" cy="{q[1]+.2*s:.1f}" r="{.09*s:.1f}" fill="{col}"/><circle cx="{q[0]:.1f}" cy="{q[1]+.2*s:.1f}" r="{.06*s:.1f}" fill="#5a5a66"/>')
    else:add('rail',f'<line x1="{q[0]:.1f}" y1="{q[1]:.1f}" x2="{q[0]:.1f}" y2="{q[1]+.22*s:.1f}" stroke="{col}" stroke-width="4"/><ellipse cx="{q[0]:.1f}" cy="{q[1]+.26*s:.1f}" rx="{.035*s:.1f}" ry="{.05*s:.1f}" fill="{col}"/>')
# clock
c=pr(-1.6,2.15,RZ);s=sc(RZ)
add('clock',f'<circle cx="{c[0]:.1f}" cy="{c[1]:.1f}" r="{.22*s:.1f}" fill="#ff6b6b"/><circle cx="{c[0]:.1f}" cy="{c[1]:.1f}" r="{.17*s:.1f}" fill="#fff"/>'+''.join(f'<circle cx="{c[0]+.13*s*math.sin(k/12*6.283):.1f}" cy="{c[1]-.13*s*math.cos(k/12*6.283):.1f}" r="2.4" fill="#ff8fab"/>' for k in range(12))+f'<path d="M{c[0]:.1f} {c[1]:.1f} V{c[1]-.11*s:.1f} M{c[0]:.1f} {c[1]:.1f} L{c[0]+.08*s:.1f} {c[1]+.03*s:.1f}" stroke="#3a2a3a" stroke-width="4" stroke-linecap="round"/>')
# fridge (left)
add('fridge',box(-2.58,-1.9,0,1.62,3.3,3.96,'#fbfbf7',sw=1))
fz=3.3
for y0,y1 in((.05,1.0),(1.04,1.57)):
    add('fridge',poly([pr(-2.54,y0,fz),pr(-1.94,y0,fz),pr(-1.94,y1,fz),pr(-2.54,y1,fz)],'#f2f2ec',sw=1))
h=pr(-2.0,1.12,fz);h2=pr(-2.0,1.4,fz);add('fridge',f'<line x1="{h[0]:.1f}" y1="{h[1]:.1f}" x2="{h2[0]:.1f}" y2="{h2[1]:.1f}" stroke="#c0c4c8" stroke-width="8" stroke-linecap="round"/>')
lg_=pr(-2.24,1.3,fz);add('fridge',f'<rect x="{lg_[0]-24:.1f}" y="{lg_[1]-8:.1f}" width="48" height="16" rx="4" fill="#c0c4c8"/>')
for i,(col,shape) in enumerate((('#ffd166','circle'),('#4d96ff','rect'),('#ff8fab','circle'))):
    q=pr(-2.4+i*.13,.75-i*.08,fz);add('fridge',f'<circle cx="{q[0]:.1f}" cy="{q[1]:.1f}" r="8" fill="{col}"/>' if shape=='circle' else f'<rect x="{q[0]-9:.1f}" y="{q[1]-7:.1f}" width="18" height="14" rx="3" fill="{col}"/>')
q=pr(-2.3,.92,fz);add('fridge',f'<rect x="{q[0]-26:.1f}" y="{q[1]-30:.1f}" width="52" height="40" rx="3" fill="#ffffff" transform="rotate(-6 {q[0]:.1f} {q[1]:.1f})"/><path d="M{q[0]-16:.1f} {q[1]-10:.1f} l12 -12 l10 14 l8 -6 l10 14" fill="none" stroke="#6bcb77" stroke-width="3" transform="rotate(-6 {q[0]:.1f} {q[1]:.1f})"/>')
# ---------- table with gingham cloth ----------
TX0,TX1,TZ0,TZ1,TY=-.75,.85,1.95,2.85,.76
for x in(TX0+.08,TX1-.14):
    for z in(TZ0+.1,TZ1-.16):add('table',box(x,x+.06,0,TY,z,z+.06,'#c98a5a',sw=1))
top=[pr(TX0,TY,TZ0),pr(TX1,TY,TZ0),pr(TX1,TY,TZ1),pr(TX0,TY,TZ1)]
add('table',poly(top,'#ffffff',sw=0)+poly(top,'url(#gingR)',sw=0))
fr=[pr(TX0-.03,TY,TZ0-.03),pr(TX1+.03,TY,TZ0-.03),pr(TX1+.03,TY-.28,TZ0-.03),pr(TX0-.03,TY-.28,TZ0-.03)]
d=f'M{fr[0][0]:.1f} {fr[0][1]:.1f} H{fr[1][0]:.1f} V{fr[2][1]:.1f} '+''.join(f'q{-(fr[1][0]-fr[0][0])/24:.1f} 10 {-(fr[1][0]-fr[0][0])/12:.1f} 0 ' for _ in range(12))+'Z'
add('table',f'<path d="{d}" fill="#ffffff"/><path d="{d}" fill="url(#gingR)"/><path d="{d}" fill="rgba(80,20,20,.08)"/>')
sd=[pr(TX1+.03,TY,TZ0-.03),pr(TX1+.03,TY,TZ1),pr(TX1+.03,TY-.28,TZ1),pr(TX1+.03,TY-.28,TZ0-.03)]
add('table',f'<polygon points="{pts(sd)}" fill="url(#gingR)"/><polygon points="{pts(sd)}" fill="rgba(80,20,20,.16)"/>')
# bowl with rising dough + towel
bw=pr(.05,TY,2.42);s=sc(2.42)/560
bowl=f'<g transform="translate({bw[0]:.1f} {bw[1]:.1f}) scale({s:.3f})">'
bowl+='<ellipse cx="0" cy="4" rx="150" ry="20" fill="rgba(60,30,30,.15)"/>'
bowl+='<path d="M-118 -96 C-140 -180 -60 -230 0 -214 C30 -250 120 -230 120 -160 C150 -150 140 -110 118 -96Z" fill="#fbe7c2"/><path d="M-80 -180 q20 -18 40 -4 M30 -200 q22 -10 40 6" fill="none" stroke="#e8cfa0" stroke-width="6" stroke-linecap="round"/><circle cx="-40" cy="-150" r="6" fill="#efd8ad"/><circle cx="40" cy="-140" r="5" fill="#efd8ad"/><circle cx="70" cy="-176" r="4" fill="#efd8ad"/>'
bowl+='<path d="M-126 -100 H126 C120 -30 80 0 0 0 C-80 0 -120 -30 -126 -100Z" fill="#4d96ff"/><ellipse cx="0" cy="-100" rx="126" ry="18" fill="#6fb0ff"/><path d="M-126 -100 H126 C120 -30 80 0 0 0 C-80 0 -120 -30 -126 -100Z" fill="url(#pDotsC)"/>'
bowl+='<path d="M60 -206 C100 -220 140 -190 136 -150 C134 -120 128 -96 120 -70 L96 -78 C108 -110 106 -150 70 -176Z" fill="#ffffff"/><path d="M60 -206 C100 -220 140 -190 136 -150 C134 -120 128 -96 120 -70 L96 -78 C108 -110 106 -150 70 -176Z" fill="url(#gingR)"/></g>'
add('bowl',bowl)
# flour bag, eggs, rolling pin, sugar
fb=pr(.58,TY,2.6);s=sc(2.6)/600
add('props',f'<g transform="translate({fb[0]:.1f} {fb[1]:.1f}) scale({s:.3f})"><path d="M-60 0 L-52 -150 L-34 -164 L-14 -150 L4 -166 L22 -150 L40 -164 L52 -150 L60 0Z" fill="#f6efe0"/><rect x="-40" y="-110" width="80" height="54" rx="8" fill="#ffffff"/><text x="0" y="-74" text-anchor="middle" font-family="sans-serif" font-weight="800" font-size="28" fill="#c4455e">МУКА</text><ellipse cx="-10" cy="-160" rx="30" ry="6" fill="#ffffff" opacity=".8"/></g>')
for i,(x,z) in enumerate(((-.42,2.2),(-.34,2.16),(-.38,2.26))):
    q=pr(x,TY,z);r=.045*sc(z);add('props',f'<ellipse cx="{q[0]:.1f}" cy="{q[1]-r*.9:.1f}" rx="{r*.8:.1f}" ry="{r:.1f}" fill="#fff8ea"/><ellipse cx="{q[0]-r*.25:.1f}" cy="{q[1]-r*1.2:.1f}" rx="{r*.22:.1f}" ry="{r*.3:.1f}" fill="#ffffff"/>')
p1,p2=pr(-.55,TY+.03,2.05),pr(-.05,TY+.03,2.02)
add('props',f'<line x1="{p1[0]:.1f}" y1="{p1[1]:.1f}" x2="{p2[0]:.1f}" y2="{p2[1]:.1f}" stroke="#f2bb72" stroke-width="{.06*sc(2.03):.1f}" stroke-linecap="round"/><line x1="{p1[0]-14:.1f}" y1="{p1[1]:.1f}" x2="{p1[0]:.1f}" y2="{p1[1]:.1f}" stroke="#c98a5a" stroke-width="{.035*sc(2.03):.1f}" stroke-linecap="round"/><line x1="{p2[0]:.1f}" y1="{p2[1]:.1f}" x2="{p2[0]+14:.1f}" y2="{p2[1]:.1f}" stroke="#c98a5a" stroke-width="{.035*sc(2.03):.1f}" stroke-linecap="round"/>')
# stools
for sx_,sz in((-1.2,2.3),(1.25,2.5)):
    for dx in(-.12,.1):
        for dz in(-.12,.1):add('stools',box(sx_+dx,sx_+dx+.04,0,.46,sz+dz,sz+dz+.04,'#c98a5a',sw=1))
    add('stools',box(sx_-.17,sx_+.17,.46,.51,sz-.17,sz+.17,'#ffb36b',sw=1))
# Timosha on stool, looking at the bowl
cp=pr(-1.2,.51,2.3);add('catk',f'<g transform="translate({cp[0]:.1f} {cp[1]:.1f}) scale({sc(2.3)*.45/120:.3f})">'+DT.cat()+'</g>')
# ceiling lamp
top_=pr(.05,RH,2.3);s=sc(2.3);c=pr(.05,2.45,2.3)
add('lamp',f'<line x1="{top_[0]:.1f}" y1="{top_[1]:.1f}" x2="{c[0]:.1f}" y2="{c[1]-.1*s:.1f}" stroke="#8a6a8a" stroke-width="3"/><path d="M{c[0]-.3*s:.1f} {c[1]+.1*s:.1f} Q{c[0]:.1f} {c[1]-.32*s:.1f} {c[0]+.3*s:.1f} {c[1]+.1*s:.1f}Z" fill="#6bcb77"/><rect x="{c[0]-.32*s:.1f}" y="{c[1]+.07*s:.1f}" width="{.64*s:.1f}" height="{.06*s:.1f}" rx="{.03*s:.1f}" fill="#4fb35f"/><circle cx="{c[0]:.1f}" cy="{c[1]+.16*s:.1f}" r="{.05*s:.1f}" fill="#fff3a6"/>')
# shadows
def fp(x0,x1,z0,z1,g=.1):return f'<polygon points="{pts([pr(x0-g,0,z0-g),pr(x1+g,0,z0-g),pr(x1+g,0,z1),pr(x0-g,0,z1)])}" fill="#3a2416"/>'
add('kshadows',f'<g filter="url(#soft)" opacity=".28">{fp(TX0,TX1,TZ0,TZ1)}{fp(.95,2.6,3.3,4)}{fp(-2.58,-1.9,3.2,4)}{fp(-1.37,-1.03,2.13,2.47,.05)}{fp(1.08,1.42,2.33,2.67,.05)}</g>')
WCM=''
LAY=['ceiling','background','ao','kshadows','curtains','sill','clock','shelf','rail','fridge','counter','stove','kettle','stools','catk','table','bowl','props','lamp']
def layer(k):return f'<g id="{k}">{L[k]}</g>'
def svg(keys,paper=True):
    inner='\n'.join(layer(k) for k in keys)
    D=defs.replace('</defs>',''.join(GD)+''.join(DT.GDEFS)+DT.patterns()+'</defs>')
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="750" viewBox="0 20 {W} 750">{D}{"<rect x=\"0\" y=\"0\" width=\"1200\" height=\"900\" fill=\"#f4ead6\"/>" if paper else ""}<g>{inner}</g></svg>'
open('kitchen.svg','w').write(svg(LAY))
import json;json.dump({k:svg([k],paper=False) for k in LAY},open('layerskitchen.json','w'))
print('ok',len(LAY))
