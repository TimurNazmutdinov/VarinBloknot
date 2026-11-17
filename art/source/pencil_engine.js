function mulberry32(a){return function(){a|=0;a=a+0x6D2B79F5|0;let t=Math.imul(a^a>>>15,1|a);t=t+Math.imul(t^t>>>7,61|t)^t;return((t^t>>>14)>>>0)/4294967296}}
let R=mulberry32(1);const seed=s=>R=mulberry32(s);const rnd=(a=0,b=1)=>a+(b-a)*R();
const P2=s=>new Path2D(s);
function hx(h){h=h.replace('#','');return[0,2,4].map(i=>parseInt(h.substr(i,2),16))}
function shade(h,f){return'#'+hx(h).map(v=>Math.round(f<0?v*(1+f):v+(255-v)*f).toString(16).padStart(2,'0')).join('')}
function rr(x,y,w,h,r){return`M${x+r} ${y}H${x+w-r}Q${x+w} ${y} ${x+w} ${y+r}V${y+h-r}Q${x+w} ${y+h} ${x+w-r} ${y+h}H${x+r}Q${x} ${y+h} ${x} ${y+h-r}V${y+r}Q${x} ${y} ${x+r} ${y}Z`}
function ell(cx,cy,rx,ry,rot=0){const p=new Path2D();p.ellipse(cx,cy,rx,ry,rot,0,Math.PI*2);return p}

const halo=ell(300,350,240,310);
const hairBack=P2('M300 120 C200 120 150 190 160 290 C163 345 182 385 215 405 L385 405 C418 385 437 345 440 290 C450 190 400 120 300 120 Z');
const body=P2('M300 455 C260 455 230 445 215 450 C170 465 140 500 130 560 L115 760 L485 760 L470 560 C460 500 430 465 385 450 C370 445 340 455 300 455 Z');
const bodyShL=P2('M130 560 C140 500 170 465 215 450 C200 520 190 640 200 760 L115 760 Z');
const bodyShR=P2('M470 560 C460 500 430 465 385 450 C400 520 410 640 400 760 L485 760 Z');
const neck=P2('M268 385 L268 452 C285 464 315 464 332 452 L332 385 C315 400 285 400 268 385 Z');
const neckSh=P2('M268 385 C285 412 315 412 332 385 L332 425 C315 434 285 434 268 425 Z');
const collar=P2('M232 456 C252 504 290 508 300 478 C310 508 348 504 368 456 C342 470 320 468 300 466 C280 468 258 470 232 456 Z');
const lockL=P2('M207 270 C186 340 196 420 182 488 C204 474 218 430 224 382 C228 340 222 300 207 270 Z');
const lockR=P2('M393 270 C414 340 404 420 418 488 C396 474 382 430 376 382 C372 340 378 300 393 270 Z');
const face=P2('M300 168 C365 168 402 220 400 285 C398 350 360 400 300 405 C240 400 202 350 200 285 C198 220 235 168 300 168 Z');
const faceShR=P2('M360 230 C395 260 400 330 370 380 C345 400 320 404 300 405 C350 390 385 340 380 280 C378 260 372 245 360 230 Z');

const bangs=P2('M200 272 C198 200 240 158 300 156 C365 156 405 200 402 270 C385 236 360 216 330 212 C335 232 325 246 314 252 C312 228 295 214 270 214 C262 236 245 252 224 258 C227 242 230 230 236 220 C218 234 206 252 200 272 Z');
const faceShTop=new Path2D();faceShTop.addPath(bangs,new DOMMatrix().translate(0,11));
const band=P2('M180 262 C176 120 424 120 420 262 L405 262 C408 142 192 142 195 262 Z');
const cupL=P2(rr(166,236,40,76,18)),cupR=P2(rr(394,236,40,76,18));
const padL=P2(rr(196,248,12,52,6)),padR=P2(rr(392,248,12,52,6));
const mouth=P2('M279 349 C292 370 310 370 323 349 C308 356 292 356 279 349 Z');
const noteHead1=ell(484,202,10,7,-.4),noteHead2=ell(528,140,8,5.5,-.4);
const noteLines=P2('M493 199 L493 146 C505 154 512 164 503 178 M535 137 L535 98 C545 104 549 112 543 122');

function pencil(g,path,{w=1,a=.55,passes=3,col='#2a2420'}={}){for(let i=0;i<passes;i++){g.save();g.translate(rnd(-1.2,1.2),rnd(-1.2,1.2));g.strokeStyle=col;g.lineWidth=w*rnd(.6,1.3);g.globalAlpha=a*rnd(.5,1);g.setLineDash([rnd(30,120),rnd(1,5),rnd(10,60),rnd(1,3)]);g.lineDashOffset=rnd(0,100);g.stroke(path);g.restore()}}
function hatch(g,path,bb,{ang=.6,gap=4,a=.35,w=.7,col='#2a2420',wob=1.6}={}){g.save();g.clip(path);const cx=(bb[0]+bb[2])/2,cy=(bb[1]+bb[3])/2,d=Math.hypot(bb[2]-bb[0],bb[3]-bb[1])/2+10;g.translate(cx,cy);g.rotate(ang);g.strokeStyle=col;for(let y=-d;y<d;y+=gap*rnd(.7,1.3)){g.beginPath();const s=-d+rnd(0,d*.25),e=d-rnd(0,d*.25);g.moveTo(s,y+rnd(-wob,wob));g.quadraticCurveTo((s+e)/2,y+rnd(-wob,wob),e,y+rnd(-wob,wob));g.lineWidth=w*rnd(.6,1.4);g.globalAlpha=a*rnd(.4,1);g.stroke()}g.restore()}
function erase(g,path){g.save();g.globalCompositeOperation='destination-out';g.globalAlpha=1;g.fill(path);g.restore()}
function strands(g,path,n,f,t,col='#2a2420',a=.5){g.save();g.clip(path);g.strokeStyle=col;for(let i=0;i<n;i++){const k=R();const x0=f[0]+(f[2]-f[0])*k+rnd(-6,6),y0=f[1]+rnd(-5,5),x1=t[0]+(t[2]-t[0])*k+rnd(-14,14),y1=t[1]+rnd(-18,18);g.beginPath();g.moveTo(x0,y0);g.bezierCurveTo(x0+(x1-x0)*.2+rnd(-18,18),y0+(y1-y0)*.35,x1+rnd(-22,22),y0+(y1-y0)*.7,x1,y1);g.lineWidth=rnd(.4,1.1);g.globalAlpha=a*rnd(.35,1);g.stroke()}g.restore()}
function crayon(c,path,bb,col,{ang=.5,a=.3,w=3,base=.35,edge=.22}={}){const n=Math.round((bb[2]-bb[0])*(bb[3]-bb[1])/16);c.save();c.clip(path);c.fillStyle=col;c.globalAlpha=base;c.fill(path);for(let i=0;i<n;i++){const x=rnd(bb[0],bb[2]),y=rnd(bb[1],bb[3]),an=ang+rnd(-.35,.35),L=rnd(10,36);c.beginPath();c.moveTo(x,y);c.lineTo(x+Math.cos(an)*L,y+Math.sin(an)*L);c.strokeStyle=shade(col,rnd(-.2,.18));c.lineWidth=w*rnd(.5,1.2);c.globalAlpha=a*rnd(.35,1);c.stroke()}if(edge){c.strokeStyle=shade(col,-.3);c.lineWidth=8;c.globalAlpha=edge;c.stroke(path)}c.restore()}

function eyeP(cx){return P2(`M${cx-25} 292 C${cx-17} 274 ${cx+15} 272 ${cx+25} 290 C${cx+15} 304 ${cx-15} 306 ${cx-25} 292 Z`)}
