import requests
import json
import time
from typing import List


def req(s: str) -> List[str]:
    headers = {
        'authority': 'datasetsearch.research.google.com',
        'sec-ch-ua': '"Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99"',
        'x-same-domain': '1',
        'sec-ch-ua-mobile': '?0',
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36',
        'content-type': 'application/x-www-form-urlencoded;charset=UTF-8',
        'accept': '*/*',
        'origin': 'https://datasetsearch.research.google.com',
        'x-client-data': 'CJK2yQEIpbbJAQjBtskBCKmdygEIhsLKAQj4x8oBCO/pygEIsprLAQjVnMsBCOScywEIqJ3LAQjf78sBGOCaywE=',
        'sec-fetch-site': 'same-origin',
        'sec-fetch-mode': 'cors',
        'sec-fetch-dest': 'empty',
        'referer': 'https://datasetsearch.research.google.com/',
        'accept-language': 'zh-CN,zh;q=0.9,en;q=0.8',
        'cookie': 'SEARCH_SAMESITE=CgQIjJEB; CONSENT=YES+CN.zh-CN+202011; ANID=AHWqTUlOmorN_BQG4a9fNhI1w8iIydz3Q9VR7OE7pgHb4SanBcACSRrVJeHXn4q7; _ga=GA1.3.364138201.1605599282; HSID=A-RUsf1_Tyz_1Z8ZF; SSID=ANYorekOlVB9k1oIu; APISID=VrJWukve1KwkQNOM/Asqv-9znkYkGI12Ck; SAPISID=9mOXw--RAxlVwu4g/A5qnLE122vkzUW1KZ; __Secure-3PAPISID=9mOXw--RAxlVwu4g/A5qnLE122vkzUW1KZ; SID=7QdTtimLZFiFMIvkMSutcPBlpcyb2EZHExUD7prloMZ_Zy9bT-TmrBbXXLMddRolfqG3mA.; __Secure-3PSID=7QdTtimLZFiFMIvkMSutcPBlpcyb2EZHExUD7prloMZ_Zy9bb_QJ3Wr9HtROfeckZhwiCQ.; OTZ=5905752_24_24__24_; _gid=GA1.3.942495978.1616649128; 1P_JAR=2021-03-25-05; NID=212=kh_mXhSv1spXsyz8pE3T7BfOY9ZUG-eXM97q1bEBw08uh2V817v4ZuJH1Gz9lvedtPGuqiBr65TCZyjJaE7on6cRjclX2M4Bmn2N5ASXPNjwBv4cGVdEc0JMxq_urEcz5zvSxZnAFOCl1fM9c8USaCfqU31MUrWFUxDZ3BSKvcLfDrN6PqtSTP36v9vJ9IvdIsKtygY7VKwy0hDEBno4ZM3hZc9C581NITnEaRx84jvsQg; _gat_UA-139086621-1=1; SIDCC=AJi4QfEVBuRNqAGHj-UuPqskv4JWkJ-5fJ0dZQKfS3zsiiXD73dS3V8q-0WCmuVXsmJrPrtfrO8; __Secure-3PSIDCC=AJi4QfFZg22nwm9Wt6JX1F0PyrtDzJHXR23R2-MjCp37KufAjjFXy9DvyIQvFYK0dGgWWWj3NA',
    }

    params = (
        ('rpcids', 'GtRBPd'),
        ('f.sid', '-1235849158922515585'),
        ('bl', 'boq_researchsciencesearchuiserver_20210320.16_p0'),
        ('hl', 'zh-CN'),
        ('_reqid', '150721'),
        ('rt', 'c'),
    )

    data = {
    'f.req': '[[["GtRBPd","[\\"{}\\"]",null,"generic"]]]'.format(s),
    'at': 'ADKUe9yzVCRZ-JKqOUT9mM4j_ahN:1616652313928'
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
    #print(lines)
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
    #print(datas)
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
        fout.write(text.strip('\n')+"\t"+s+"\n")
    print("No. %d %s finished" %(cnt,text.strip('\n')))
    time.sleep(3)
fin.close()
fout.close()
