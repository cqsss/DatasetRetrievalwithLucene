import pandas as pd
 
if __name__ == '__main__':
    # 此处是读取中文数据，如果是英文数据，编码可能是'ISO 8859-1'
    pd_all = pd.read_csv("./datasets_with_triple.csv", sep=',', encoding='utf-8')
    # 保存为tsv文件，当然也可以保存为csv文件，二者区别在于sep为'\t'还是','
    pd_all.to_csv("./datasets_with_triple.tsv", index=False, sep='\t', encoding='utf-8')