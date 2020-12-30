import requests
import json
import time
from typing import List


def req(s: str) -> List[str]:
    headers = {
        'authority': 'datasetsearch.research.google.com',
        'x-same-domain': '1',
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36',
        'content-type': 'application/x-www-form-urlencoded;charset=UTF-8',
        'accept': '*/*',
        'origin': 'https://datasetsearch.research.google.com',
        'x-client-data': 'CJK2yQEIpbbJAQjBtskBCKmdygEIhsLKAQisx8oBCPXHygEI+MfKAQi0y8oBCKTNygEIq83KAQjc1coBCJSaywEIwpzLAQjGnMsBCNWcywE=',
        'sec-fetch-site': 'same-origin',
        'sec-fetch-mode': 'cors',
        'sec-fetch-dest': 'empty',
        'referer': 'https://datasetsearch.research.google.com/',
        'accept-language': 'zh-CN,zh;q=0.9,en;q=0.8',
        'cookie': 'SEARCH_SAMESITE=CgQIjJEB; CONSENT=YES+CN.zh-CN+202011; ANID=AHWqTUlOmorN_BQG4a9fNhI1w8iIydz3Q9VR7OE7pgHb4SanBcACSRrVJeHXn4q7; _ga=GA1.3.364138201.1605599282; SID=4QdTtnFT5wZXtUGOnEHDrLFSJQdyLwJ8-R15YAig6m3qR6YaASV_qoKaP9LtVFdPHPHHig.; __Secure-3PSID=4QdTtnFT5wZXtUGOnEHDrLFSJQdyLwJ8-R15YAig6m3qR6YaP-eh-GwxKktm-yArGKsWCA.; HSID=A-RUsf1_Tyz_1Z8ZF; SSID=ANYorekOlVB9k1oIu; APISID=VrJWukve1KwkQNOM/Asqv-9znkYkGI12Ck; SAPISID=9mOXw--RAxlVwu4g/A5qnLE122vkzUW1KZ; __Secure-3PAPISID=9mOXw--RAxlVwu4g/A5qnLE122vkzUW1KZ; OTZ=5764814_24_24__24_; _gid=GA1.3.903015855.1609139634; NID=205=aaI5aqnbm2XaCX0AO7Fi71kaUzVSTMTpwwL6WRLONeHkIPPmxZ46d-E7-KDRP1iptUt_mU1nkw2URx7ZkSOTS9yHEdOmNYuDn8v4YfzwZT8L0_Op9NRzHb87J3VR7t6aud_E8ckgySipUr3STGYJ2GA7XHYLS90rgh7cimiTK0-pBBpeO3GJHJigWL229EOXhT7sn1CfUS2MpMsl-oj2o6cIMR6iOk9c-B6Fu4HozB8; 1P_JAR=2020-12-29-12; _gat_UA-139086621-1=1; SIDCC=AJi4QfHBSbbHq3Jrjm-E93SHvT-hWGUKEISuczAepTJudZYc7Dem5Xu2ZUFdYpcGvgQMXG_JJPo; __Secure-3PSIDCC=AJi4QfHA2Me-zoOwEvOcMIOfKzZ_Mb-SfgQYGKfQav9cxWWaXK1YeRG2t-t1r2LOM0P9P3F4jw',
    }

    params = (
        ('rpcids', 'GtRBPd'),
        ('f.sid', '9077632682441936921'),
        ('bl', 'boq_researchsciencesearchuiserver_20201218.16_p0'),
        ('hl', 'zh-CN'),
        ('_reqid', '573016'),
        ('rt', 'c'),
    )

    data = {
    'f.req': '[[["GtRBPd","[\\"{}\\"]",null,"generic"]]]'.format(s),
    'at': 'ADKUe9yg7svNpUXGGwdLI-theDKL:1609244210878',
    '': ''
    }

    proxies = {
    'http': 'http://127.0.0.1:1080',
    'https': 'http://127.0.0.1:1080',
    }

    s = requests.session()
    s.keep_alive = False
    response = requests.post('https://datasetsearch.research.google.com/_/ResearchSciencesearchDesktopUi/data/batchexecute', headers=headers, params=params, data=data, proxies=proxies)

    #NB. Original query string below. It seems impossible to parse and
    #reproduce query strings 100% accurately so the one below is given
    #in case the reproduced version is not "correct".
    # response = requests.post('https://datasetsearch.research.google.com/_/ResearchSciencesearchDesktopUi/data/batchexecute?rpcids=GtRBPd&f.sid=9077632682441936921&bl=boq_researchsciencesearchuiserver_20201218.16_p0&hl=zh-CN&_reqid=573016&rt=c', headers=headers, data=data)

    lines = response.text.split('\n')
    flag = False
    datas = ""
    for l in lines:
        if l.isdigit():
            if flag:
                break
            flag = True
            continue
        if flag:
            datas += l
    data_json = json.loads(datas)
    return json.loads(data_json[0][2])[0]

fin = open("terms.in","r",encoding='utf-8')
fout = open("queries.out","w",encoding='utf-8')
cnt = 0
while True:
    text = fin.readline()
    if not text:
        break
    cnt = cnt+1
    lis = req(text.strip('\n'))
    for s in lis:
        fout.write(text.strip('\n')+";"+s+"\n")
    print("No. %d %s finished" %(cnt,text.strip('n')))
    time.sleep(3)
fin.close()
fout.close()
