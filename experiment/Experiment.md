---
marp: true
---
## Query导向的排序方法评价实验
---
### Data Collection

- 根据数据库内容编写query
    - query的数量
    - query的长度
    - query的质量
    - 包含该query的字段
- 根据query得到hits
    - 对hits根据query进行打分（3分制或4分制或5分制）
    - 3-5人对同一数据集进行打分，不同取多数，无多数取平均
    - 给用户呈现哪些内容
    - 质量和相关性评判标准（打分标准）
    - 是否区分质量分数与相关性分数
---
- 具体流程：
    1. 统计**class&property**字段、**title&notes**字段中出现次数最多的前100个有效term（去除**含数字的或长度为1的**term）；
    2. 在google dataset search上搜索这些term，取**搜索下拉框推荐的相关查询**作为origin queries（每个term10个，个别term不足10个）；
    3. 取origin queries中**去掉标点符号**后使用Lucene(8.7.0)中English Analyzer **parse后长度小于等于8**的且**只包含英语和数字**的作为test queries；
    4. 取test queries中使用Lucene默认评分函数**平均每词每字段得分大于阈值k的hits数多于20的**作为实验用查询；
    5. 取这些查询不同baseline的hits前20集合；
    6. 将得到的hits随机提供给用户（3人以上）进行打分，根据该数据集与该query的相关程度进行打分。

---

- property：所有predicate
- class：所有'%rdf-syntax-ns#type%'的predicate指向的object

---

开发集：https://github.com/xiaxia-wang/data-requests-query-dataset
来源：https://dl.acm.org/doi/10.1145/3184558.3191597

---

### Pooling

- Setup
    - BM25, TFIDF的计算字段为title, notes, class, property(简单求和 or 加权平均)；
    - FSDM微调class和property字段的权重；
    - BM25, TFIDF, FSDM使用Lucene默认方法检索得到前500个hits后进行rerank；DPR直接对全部数据集文本进行检索；

---

- Result
![image.png](https://i.loli.net/2021/03/29/udABaH6MrwVSxPX.png)

---

- Result

| k    | queries | mean@5 | sum@5 | mean@10 | sum@10 | mean@20 | sum@20 |
| ---- | ------- | ------ | ----- | ------- | ------ | ------- | ------ |
| 0    | 766     | 14.06  | 10770 | 27.46   | 21031  | 53.68   | 41121  |
| 0.1  | 766     | 14.06  | 10770 | 27.46   | 21031  | 53.68   | 41121  |
| 0.2  | 764     | 14.07  | 10747 | 27.47   | 20986  | 53.71   | 41031  |
| 0.3  | 689     | 14.09  | 9706  | 27.51   | 18951  | 53.68   | 36985  |
| 0.4  | 391     | 14.12  | 5522  | 27.40   | 10715  | 53.14   | 20778  |
| 0.5  | 210     | 14.18  | 2977  | 27.26   | 5725   | 52.46   | 11016  |
| 0.6  | 113     | 13.96  | 1577  | 26.79   | 3027   | 51.58   | 5829   |
| 0.7  | 59      | 14.19  | 837   | 26.64   | **1572**   | 51.07   | **3013**   |
| 0.8  | 35      | 13.83  | 484   | 25.66   | 898    | 49.77   | **1742**   |
| 0.9  | 20      | 14.15  | 283   | 26.10   | 522    | 49.10   | 982    |
| 1    | 12      | 13.00  | 156   | 24.42   | 293    | 46.92   | 563    |

---

### labeling guidance

- a dataset is off topic (0) if the information does not satisfy the information need, and should not be listed in the search results from a search engine;
- a dataset is poor (1) if a search engine were to include this in the search results, but it should not be listed at the top;
- a dataset is good (2) if you would expect this dataset to be included in the search results from a search engine;
- a dataset is excellent (3) if you would expect this dataset ranked near the top of the search results from a search engine.

---

### Baseline

- 单一方法排序
    - TF-IDF
    - BM25
    - FSDM
    - DPR
    - PageRank
    - DING
    - DRank (仅根据数据集的度数排序的naive rank)
---
- 混合方法排序
    - Quality(PageRank、DING、DRank) + Relevance(TF-IDF、BM25、FSDM)

- 多filed与单field
    - 全部field
    - 仅content
    - 仅title和description
    - 仅content、title和description
---
### Research Questions

- **Q1** 不同方法的效果比较（在单field上对比和在多field上对比）
- **Q2** 不同field对排序效果的影响（相同方法下不同field上搜索的比较）
- **Q3** 自主设计方法的效果

---
### Evaluation Metrics

- nDCG@k (k=5,10,15,20*)
- Precision@k
- Recall@k
- F值 or MAP