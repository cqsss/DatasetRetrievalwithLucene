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
    private Map<Integer, String> propertyText = new HashMap<>();
    private Map<Integer, String> classText = new HashMap<>();
    private Set<Integer> classSet = new HashSet<>();
    private IndexFactory indexF;
    private Integer datasetCountLimit = 1000000;
    /**
     * 统计实体出现次数
     * @param count
     * @param id
     */
    private void addCount(Map<Integer, Integer> count, Integer id) {
        if(!count.containsKey(id)) count.put(id, 0);
        count.put(id, count.get(id) + 1);
    }

    /**
     * 获取数量前几的实体label文本
     * @param count
     * @param limit
     * @return
     */
    private String getTopUnitText(Map<Integer, Integer> count, Integer limit) {
        List<Map.Entry<Integer, Integer>> countList = new ArrayList<>(count.entrySet());
        countList.sort(new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < countList.size() && i < limit; i++) {
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
    private String generateText(List<TripleID> datasetTriples) {
        Map<Integer, Integer> subMap = new HashMap<>();
        Map<Integer, Integer> preMap = new HashMap<>();
        Map<Integer, Integer> objMap = new HashMap<>();
        Map<Integer, Integer> sumMap = new HashMap<>();
        for (TripleID tri : datasetTriples) {
            addCount(subMap, tri.getSubject());
            addCount(preMap, tri.getPredicate());
            addCount(objMap, tri.getObject());
            addCount(sumMap, tri.getSubject());
            addCount(sumMap, tri.getObject());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getTopUnitText(subMap, GlobalVariances.maxEntityNumber)); sb.append(";");
        sb.append(getTopUnitText(objMap, GlobalVariances.maxEntityNumber)); sb.append(";");
        sb.append(getTopUnitText(sumMap, GlobalVariances.maxEntityNumber)); sb.append(";");
        sb.append(getTopUnitText(preMap, GlobalVariances.maxRelationNumber)); sb.append(";");
        return sb.toString();
    }
    private String generatePropertyText (List<Integer> propertyList) {
        Map<Integer, Integer> preMap = new HashMap<>();
        for (Integer i : propertyList) {
            addCount(preMap, i);
        }
        return getTopUnitText(preMap, GlobalVariances.maxRelationNumber);
    }
    private String generateClassText (Set<Integer> classList) {
        Map<Integer, Integer> claMap = new HashMap<>();
        for (Integer i : classList) {
            addCount(claMap, i);
        }
        return getTopUnitText(claMap, GlobalVariances.maxEntityNumber);
    }
    private void getClassIDSet() {
        List<Integer> classList = jdbcTemplate.queryForList("SELECT DISTINCT(object) FROM triple WHERE predicate IN (SELECT global_id FROM entity WHERE label LIKE '%rdf-syntax-ns#type%' AND is_literal=0)", Integer.class);
        classSet.addAll(classList);
    }
    /**
     * 得到数据集id到triple文本的映射
     */
    private void mapID2TripleText() {
        getClassIDSet();
        logger.info("Completed getClassIDSet");
        Integer tripleCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM triple;",Integer.class);
        Integer currentID = 1;
        List<TripleID> tripleIDS = new ArrayList<>();
        List<Integer> propertyIDS = new ArrayList<>();
        Set<Integer> classIDS = new HashSet<>();
        for (int i = 0; i <= tripleCount / GlobalVariances.maxListNumber; i++) {
            List<Map<String, Object>> queryList = jdbcTemplate.queryForList(String.format("SELECT dataset_id,subject,predicate,object FROM triple LIMIT %d,%d;", i * GlobalVariances.maxListNumber, GlobalVariances.maxListNumber));
            for (Map<String, Object> qi : queryList) {
                int dataset_id = Integer.parseInt(qi.get("dataset_id").toString());
                Integer sub = Integer.parseInt(qi.get("subject").toString());
                Integer pre = Integer.parseInt(qi.get("predicate").toString());
                Integer obj = Integer.parseInt(qi.get("object").toString());
                if (dataset_id > currentID) {
                    id2text.put(currentID, generateText(tripleIDS));
                    propertyText.put(currentID, generatePropertyText(propertyIDS));
                    classText.put(currentID, generateClassText(classIDS));
                    logger.info("Completed mapping dataset " + currentID);
                    currentID = dataset_id;
                    tripleIDS = new ArrayList<>();
                    propertyIDS = new ArrayList<>();
                    classIDS = new HashSet<>();
                }
                tripleIDS.add(new TripleID(sub, pre, obj));
                propertyIDS.add(pre);
                if (classSet.contains(obj)) {
                    classIDS.add(obj);
                }
            }
            logger.info("MapID2TripleText process: " + (((double) i * GlobalVariances.maxListNumber.doubleValue() + GlobalVariances.maxListNumber.doubleValue()) / tripleCount.doubleValue()));
        }
        if(tripleIDS.size() > 0)
            id2text.put(currentID, generateText(tripleIDS));
        logger.info("Completed MapID2TripleText!");
    }

    /**
     * 由local_id获取文本
     * @param local_id
     * @return
     */
    private String getTextFromLocalID(Integer local_id) {
        return id2text.getOrDefault(local_id, "");
    }
    private String getPropertyFromLocalID(Integer local_id) {
        return propertyText.getOrDefault(local_id, "");
    }
    private String getClassFromLocalID(Integer local_id) {
        return classText.getOrDefault(local_id, "");
    }

    /**
     * 生成文档并提交
     */
    private void generateDocument() {
        int all = 0;
        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorOffsets(true);
        fieldType.setStoreTermVectorPayloads(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList("SELECT * FROM metadata");
        for (Map<String, Object> qi : queryList) {
            Document document = new Document();
            all ++;
            // local ID
            Integer local_id = Integer.parseInt(qi.get("dataset_id").toString());
            document.add(new StoredField("dataset_id", local_id.toString()));

            // Dataset ID
            String id = qi.get("id").toString();
            document.add(new StoredField("id", id));

            // title & notes
            String title = qi.get("title").toString();
            String notes = qi.get("notes").toString();
            document.add(new Field("title_notes", title + ";" + notes, fieldType));

            // Content
            String content = getTextFromLocalID(local_id);
            document.add(new Field("content", content, fieldType));

            // property
            String property = getPropertyFromLocalID(local_id);
            document.add(new Field("property", property, fieldType));

            // class
            String _class = getClassFromLocalID(local_id);
            document.add(new Field("class", _class, fieldType));

            // property & class
            document.add(new Field("class_property", _class + ";" + property, fieldType));

            // Normal Fields
            for (Map.Entry<String, Object> entry : qi.entrySet()) {
                String name = entry.getKey();
                if (name.equals("dataset_id") || name.equals("id"))
                    continue;
                String value = "";
                if (entry.getValue() != null)
                    value = entry.getValue().toString();
                document.add(new Field(name, value, fieldType));
            }

            // commit document
            indexF.commitDocument(document);
            if (all % 10000 == 0)
                logger.info("Completed generating document: " + all);
            if (all > datasetCountLimit) break;

        }
        logger.info("Completed GenerateDocument All: " + all);
    }

    public void main() {
        logger.info("Start");
        id2text.clear();
        indexF = new IndexFactory();
        indexF.init(GlobalVariances.store_Dir, GlobalVariances.commit_limit, GlobalVariances.globeAnalyzer);
        mapID2TripleText();
        generateDocument();
        indexF.closeIndexWriter();
    }
}
