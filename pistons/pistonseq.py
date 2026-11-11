import sys,re
f,test,n=sys.argv[1],sys.argv[2],sys.argv[3]
ids={'2150698529':'stone','3690217760':'air','1961230256':'piston[up]','2301637631':'sticky_piston[up]','512666773':'redstone_block','4223730116':'piston_arm_collision[up]','917858187':'sticky_piston_arm_collision[up]','3651099133':'moving_block'}
rows=[l.rstrip('\n').split('\t') for l in open(f)][1:]
on=False; t0=None; k0=None
for r in rows:
    if len(r)<6: continue
    if r[4]=='TextPacket' and 'MARK' in r[5]:
        m=re.search(r'MARK (\w+) (\w+) (\d+)',r[5])
        was=on
        on=bool(m and m.group(1)==test and m.group(3)==n and m.group(2) in('power','unpower'))
        if on: print('--- console: %s'%m.group(2))
        continue
    if not on or r[3]=='121': continue
    m=re.search(r'[pP]osition=\((-?[\d.]+), (-?[\d.]+), (-?[\d.]+)\)',r[5])
    if not m or not(0<=float(m.group(1))<=41 and 96<=float(m.group(2))<=126 and 0<=float(m.group(3))<=41): continue
    if t0 is None: t0=float(r[0]); k0=int(r[2])
    text=re.sub(r'block#(\d+)',lambda m: ids.get(m.group(1),'block#'+m.group(1)),r[5])
    text=re.sub(r'\s+',' ',text)
    print('tick+%d (%+.0f ms)  %s'%(int(r[2])-k0,float(r[0])-t0,text))
