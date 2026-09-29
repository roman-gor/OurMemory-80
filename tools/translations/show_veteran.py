import json,sys
v=json.load(open(sys.argv[1]))[sys.argv[2]]
info=v.get('veteransInfo',[])
if isinstance(info,dict): info=[info[k] for k in sorted(info,key=int)]
print('NAME:',v['name']); print('BASE:',v.get('baseInfo','')); print('ALL:',v.get('allInfo',''))
for i,x in enumerate(info):
    if 'http' in x: print(f'[{i}] LINK caption:', x.split('|',1)[1] if '|' in x else '')
    else: print(f'[{i}] TEXT:', x)
