---
marp: true
---

### 预实验感受1
- 很多数据集的title已经高度概括数据集内容，如*Mathematics Subject Classification*。
- 很难从class和property中获取有用信息，因为大多是非自然语言词语或较为宽泛的词语（如number，year，rowID等）。
- 一些数据集的title相同或notes相同，可能是同一数据集的不同part或者为同一内容不同年份、不同地区或按不同标准统计的的数据集，认为是合理的搜索结果。
- 主要编写依据为title和notes。
- 编写10个query大约用时1小时。

---

### 预实验感受2
- 建议在用户指南中写明要求需要仔细查看的模块，不然可能有些模块用户看不懂可能就直接跳过了。
- 仅看content写summary的难度较大，勉强能够提取若干词语写keywork query。
- 在给出title的情况下，title能提供主要信息，很难做到写的summary不包含title中的关键词。
- 平均编写10个数据集的summary和query用时约为1.5-2小时。

---

### 关于该任务对每个模块的感受
- Overview
    - Statistics, Degree distribution, Namespace   统计数据对该任务无可用信息
    - Class & Property    能提取少量可用词语
- Data Ptrrerns
    - EDP, LP, PAnDA+, ExpLOD   对该任务的可用信息主要是Class&Property中的部分词语，相对比较有用
- Data Samples
    - HITS, PageRank, TripleRank    很多数据集显示的是entity的编号，对该任务无可用信息。
    - ROCKER    可用信息也主要是Class&Property中的少量词语
    - IlluSnip  看不懂...
- Entities
    - 大量实体为数字，编码等，能够从某一实体的部分属性及属性值中提取一部分信息来猜测该数据集的内容。

---
### bugs
- 全选title, all metadata, data content搜索某一查询后，仅选择data content，不修改查询内容点击搜索按钮，结果不变。但刷新后仅选择data content搜索该查询，得到不同结果。
- 有的数据集的data pattern会叠字。
- 一些含标点符号（如":"）的查询会搜不到数据集，建议对输入的查询进行必要的处理。