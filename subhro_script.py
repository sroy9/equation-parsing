# coding: utf-8
f=open('mistakes.raw')
lines=[]
for l in f:
    tmp=l[l.index('['):].strip().strip('[[]]')
    lines+=[tmp]
# print lines
ans=map(lambda x: x.strip().split(','),lines)
ans=[[k.strip() for k in i] for i in ans]
# print ans
from collections import Counter
cnt=Counter()
for line in ans:
    cnt.update(line)
# print cnt
print len(cnt.keys())
