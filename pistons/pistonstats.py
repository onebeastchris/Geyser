import sys, re, collections, statistics
f=sys.argv[1]
rows=[l.rstrip('\n').split('\t') for l in open(f)][1:]
rows=[r for r in rows if len(r)>=6]
# runtime id names from the "MARK id <block>" phase: the first UpdateBlock at (5, 110, 5) after each mark
ids={}; pending=None
for r in rows:
    if r[4]=='TextPacket':
        m=re.search(r'MARK id (\S+)',r[5]); pending=m.group(1).rstrip(',') if m else None
    elif pending and r[4]=='UpdateBlockPacket' and 'blockPosition=(5, 110, 5)' in r[5]:
        ids[re.search(r'block#(\d+)',r[5]).group(1)]=pending; pending=None
def named(s): return re.sub(r'block#(\d+)', lambda m: ids.get(m.group(1),'block#'+m.group(1)), s)
if '--ids' in sys.argv: print(ids)
def near(text):
    m=re.search(r'[pP]osition=\((-?[\d.]+), (-?[\d.]+), (-?[\d.]+)\)',text)
    if not m: return False
    x,y,z=[float(v) for v in m.groups()]
    return -1<=x<=41 and 96<=y<=126 and -1<=z<=41
def kind(r):
    n,t=r[4],r[5]
    if n=='UpdateBlockPacket':
        return 'UpdateBlock layer=%s flags=%s -> %s'%(re.search(r'dataLayer=(\d)',t).group(1), re.search(r'flags=\[(.*?)\]',t).group(1), named(re.search(r'block#\d+',t).group(0)))
    if n=='BlockEntityDataPacket':
        i=re.search(r'"id": "(\w+)"',t).group(1)
        if i=='PistonArm':
            g=lambda k: re.search(r'"%s": ([\d.]+)'%k,t).group(1)
            return 'BlockEntityData PistonArm State=%s NewState=%s Progress=%s LastProgress=%s attached=%d'%(g('State'),g('NewState'),g('Progress'),g('LastProgress'),len(re.findall(r'\d+i',re.search(r'"AttachedBlocks": \[(.*?)\]',t).group(1)))//3)
        return 'BlockEntityData '+i
    if n=='UpdateSubChunkBlocksPacket':
        return 'UpdateSubChunkBlocks standard=%d extra=%d'%(len(re.findall('BlockChangeEntry',t.split('extraBlocks=')[0])),len(re.findall('BlockChangeEntry',t.split('extraBlocks=')[1])))
    if n=='LevelSoundEventPacket': return 'LevelSoundEvent '+re.search(r'sound=(\w+)',t).group(1)
    if n=='LevelEventPacket': return 'LevelEvent '+re.search(r'type=(\w+)',t).group(1)
    return n.replace('Packet','')
segs=collections.OrderedDict(); cur=None
for r in rows:
    if r[4]=='TextPacket' and 'MARK' in r[5]:
        m=re.search(r'MARK (\w+) (power|unpower) (\d+)',r[5])
        cur=(m.group(1),int(m.group(3)),m.group(2)) if m else None
        if cur: segs[cur]=[]
        continue
    if cur is None or r[3] in ('121','9'): continue
    if r[4] in ('UpdateBlockPacket','BlockEntityDataPacket','UpdateSubChunkBlocksPacket','LevelSoundEventPacket','LevelEventPacket','BlockEventPacket','AddItemEntityPacket','AddEntityPacket') and not near(r[5]): continue
    if r[4] in ('CurrentStructureFeaturePacket','MoveEntityDeltaPacket','SetEntityDataPacket','SetEntityMotionPacket','UpdateAttributesPacket','SetTimePacket','PlayerListPacket'): continue
    segs[cur].append(r)
tests=collections.OrderedDict()
for (t,n,ph),rs in segs.items(): tests.setdefault(t,collections.OrderedDict()).setdefault(n,{})[ph]=rs
pistons={'d':50}
for t,cyc in tests.items():
    per_type=collections.defaultdict(list); detail=collections.defaultdict(list); spans=collections.defaultdict(list); entries=[]
    for n,ph in cyc.items():
        c=collections.Counter(); dc=collections.Counter(); e=0
        for p in ('power','unpower'):
            rs=ph.get(p,[])
            for r in rs:
                c[r[4].replace('Packet','')]+=1; dc[p+': '+kind(r)]+=1
                if r[4]=='UpdateSubChunkBlocksPacket': e+=len(re.findall('BlockChangeEntry',r[5]))
            # ticks from the first piston packet to the last in this half-cycle (skipping the redstone block's own UpdateBlock)
            ts=[int(r[2]) for r in rs if r[4]!='UpdateBlockPacket' or 'redstone_block' not in named(r[5])]
            if ts: spans[p].append(max(ts)-min(ts)+1)
        for k,v in c.items(): per_type[k].append(v)
        for k,v in dc.items(): detail[k].append(v)
        entries.append(e)
    N=len(cyc); k=pistons.get(t,1)
    print('\n== test (%s): %d cycles, %d piston(s)'%(t,N,k))
    print('  packets per cycle (median [min-max])'+(' and per piston' if k>1 else '')+':')
    tot=0
    for typ,v in sorted(per_type.items()):
        v=v+[0]*(N-len(v)); med=statistics.median(v); tot+=med
        print('    %-22s %6.1f [%d-%d]'%(typ,med,min(v),max(v))+('   = %.2f per piston'%(med/k) if k>1 else ''))
    print('    %-22s %6.1f'%('TOTAL',tot)+('   = %.2f per piston'%(tot/k) if k>1 else '')+'   | UpdateSubChunkBlocks block entries per cycle: %.0f'%statistics.median(entries))
    print('  ticks spanned by piston packets: extend median %s, retract median %s'%(statistics.median(spans['power']) if spans['power'] else '-',statistics.median(spans['unpower']) if spans['unpower'] else '-'))
    if '--detail' in sys.argv:
        for kk,v in sorted(detail.items()):
            v=v+[0]*(N-len(v)); print('    %5.1f  %s'%(statistics.median(v),kk))
