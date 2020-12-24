# DatasetRetrievalwithLucene
## Lucene框架的数据集搜索

### 数据集索引
- entity id到label的映射
- triple到text的映射
- dataset id到triple text的映射
- 生成索引文件
### 数据集排序
- [ ] 根据数据集质量排序
  - [x] DRank
  - [x] PageRank
  - [x] Ding(has bug) 
- [x] 根据数据集与查询相关性排序
  - [x] TF-IDF
  - [x] BM25
  - [x] FSDM
