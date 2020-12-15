package com.datasetretrievalwithlucene.demo.util;

import com.datasetretrievalwithlucene.demo.Bean.TripleID;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

@Repository
public class DBIndexer {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DBIndexer.class);
    private Map<Integer, String> id2text = new HashMap<>();
    private IndexFactory indexF;
    private Integer datasetCountLimit = 1000000;
    /**
     * 统计实体出现次数
     * @param count
     * @param id
     */
    private void AddCount(Map<Integer, Integer> count, Integer id) {
        if(!count.containsKey(id)) count.put(id, 0);
        count.put(id, count.get(id) + 1);
    }

    /**
     * 获取数量前几的实体label文本
     * @param count
     * @param limit
     * @return
     */
    private String GetTopUnitText(Map<Integer, Integer> count, Integer limit) {
        List<Map.Entry<Integer, Integer>> countList = new ArrayList<>(count.entrySet());
        Collections.sort(countList, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        StringBuilder sb = new StringBuilder();

        for (Integer i = 0; i < countList.size() && i < limit; i++) {
            sb.append(LabelMap.query(countList.get(i).getKey(), jdbcTemplate));
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * 根据实体生成文本
     * @param datasetTriples
     * @return
     */
    private String GenerateText(List<TripleID> datasetTriples) {
        Map<Integer, Integer> subMap = new HashMap<>(); subMap.clear();
        Map<Integer, Integer> preMap = new HashMap<>(); preMap.clear();
        Map<Integer, Integer> objMap = new HashMap<>(); objMap.clear();
        Map<Integer, Integer> sumMap = new HashMap<>(); sumMap.clear();
        for (TripleID tri : datasetTriples) {
            AddCount(subMap, tri.getSubject());
            AddCount(preMap, tri.getPredicate());
            AddCount(objMap, tri.getObject());
            AddCount(sumMap, tri.getSubject());
            AddCount(sumMap, tri.getObject());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(GetTopUnitText(subMap, GlobalVariances.maxEntityNumber)); sb.append(";");
        sb.append(GetTopUnitText(objMap, GlobalVariances.maxEntityNumber)); sb.append(";");
        sb.append(GetTopUnitText(sumMap, GlobalVariances.maxEntityNumber)); sb.append(";");
        sb.append(GetTopUnitText(preMap, GlobalVariances.maxRelationNumber)); sb.append(";");
        return sb.toString();
    }

    /**
     * 得到数据集id到triple文本的映射
     */
    private void MapID2TripleText() {
        Integer tripleCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM triple;",Integer.class);
        Integer currentID = 1;
        List<TripleID> tripleIDS = new ArrayList<>();
        tripleIDS.clear();
        for (Integer i = 0; i <= tripleCount / GlobalVariances.maxListNumber; i++) {
            List<Map<String, Object>> queryList = jdbcTemplate.queryForList(String.format("SELECT dataset_id,subject,predicate,object FROM triple LIMIT %d,%d;", i * GlobalVariances.maxListNumber, GlobalVariances.maxListNumber));
            for (Map<String, Object> qi : queryList) {
                Integer dataset_id = Integer.parseInt(qi.get("dataset_id").toString());
                Integer sub = Integer.parseInt(qi.get("subject").toString());
                Integer pre = Integer.parseInt(qi.get("predicate").toString());
                Integer obj = Integer.parseInt(qi.get("object").toString());
                if (dataset_id > currentID) {
                    id2text.put(currentID, GenerateText(tripleIDS));
                    logger.info("Completed mapping dataset " + currentID);
                    currentID = dataset_id;
                    tripleIDS = new ArrayList<>();
                    tripleIDS.clear();
                }
                tripleIDS.add(new TripleID(sub, pre, obj));
            }
            logger.info("MapID2TripleText process: " + ((i.doubleValue() * GlobalVariances.maxListNumber.doubleValue() + GlobalVariances.maxListNumber.doubleValue()) / tripleCount.doubleValue()));
        }
        if(tripleIDS.size() > 0)
            id2text.put(currentID, GenerateText(tripleIDS));
        logger.info("Completed MapID2TripleText!");
    }

    /**
     * 由local_id获取文本
     * @param local_id
     * @return
     */
    private String GetTextFromLocalID(Integer local_id) {
        if(id2text.containsKey(local_id)) return id2text.get(local_id);
        else return "";
    }

    /**
     * 生成文档并提价
     */
    private void GenerateDocument() {
        Integer all = 0;
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList("SELECT * FROM metadata");
        for (Map<String, Object> qi : queryList) {
            Document document = new Document();

            all ++;
            if (all % 1000 == 0)
                logger.info("Start generating document: " + all);
            // local ID
            Integer local_id = Integer.parseInt(qi.get("dataset_id").toString());
            document.add(new StoredField("dataset_id", local_id.toString()));

            // Dataset ID
            String id = qi.get("id").toString();
            document.add(new StoredField("id", id));

            // Content
            String content = GetTextFromLocalID(local_id);
            FieldType fieldType = new FieldType();
            fieldType.setStored(true);
            fieldType.setTokenized(true);
            fieldType.setStoreTermVectorPositions(true);
            fieldType.setStoreTermVectorOffsets(true);
            fieldType.setStoreTermVectorPayloads(true);
            fieldType.setStoreTermVectors(true);
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            document.add(new Field("content", content, fieldType));

            // Normal Fields
            for (Map.Entry<String, Object> entry : qi.entrySet()) {
                String name = entry.getKey();
                if (name == "dataset_id" || name == "id")
                    continue;
                String value = "";
                if (entry.getValue() != null)
                    value = entry.getValue().toString();
                document.add(new Field(name, value, fieldType));
            }

            // commit document
            indexF.CommitDocument(document);
            if (all % 1000 == 0)
                logger.info("Completed generating document: " + all);
            if (all > datasetCountLimit) break;

        }
        logger.info("Completed GenerateDocument All: " + all);
    }

    public void main() {
        logger.info("Start");
        id2text.clear();
        indexF = new IndexFactory();
        indexF.Init(GlobalVariances.store_Dir, GlobalVariances.commit_limit, GlobalVariances.globeAnalyzer);
        MapID2TripleText();
        GenerateDocument();
        indexF.CloseIndexWriter();
    }
}
