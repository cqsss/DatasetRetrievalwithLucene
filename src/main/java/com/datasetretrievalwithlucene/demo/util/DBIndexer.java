package com.datasetretrievalwithlucene.demo.util;

import com.datasetretrievalwithlucene.demo.Bean.TripleID;
import org.apache.lucene.document.*;
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
        Integer maxID = jdbcTemplate.queryForObject("SELECT MAX(dataset_id) FROM triple;",Integer.class);
        for (Integer i = 1; i <= maxID; i++) {
            List<Map<String, Object>> queryList = jdbcTemplate.queryForList(String.format("SELECT * FROM triple WHERE dataset_id = %d;", i));
            List<TripleID> tripleIDS = new ArrayList<>();
            tripleIDS.clear();
            for (Map<String, Object> qi : queryList) {
                Integer local_id = Integer.parseInt(qi.get("dataset_id").toString());
                Integer sub = Integer.parseInt(qi.get("subject").toString());
                Integer pre = Integer.parseInt(qi.get("predicate").toString());
                Integer obj = Integer.parseInt(qi.get("object").toString());
                tripleIDS.add(new TripleID(sub, pre, obj));
            }
            id2text.put(i, GenerateText(tripleIDS));
            System.out.println("Completed mapping dataset " + i);
            tripleIDS = new ArrayList<>();
            tripleIDS.clear();
        }
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
        Integer cnt = 0;
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList("SELECT * FROM metadata");
        for (Map<String, Object> qi : queryList) {
            Document document = new Document();

            all ++;
            // local ID
            Integer local_id = Integer.parseInt(qi.get("dataset_id").toString());
            document.add(new StoredField("dataset_id", local_id.toString()));
            if (local_id > 0) cnt ++;

            // Dataset ID
            String id = qi.get("id").toString();
            document.add(new StoredField("id", id));

            // Content
            String content =GetTextFromLocalID(local_id);
            document.add(new TextField("content", content, Field.Store.YES));

            // Normal Fields
            for (Map.Entry<String, Object> entry : qi.entrySet()) {
                String name = entry.getKey();
                if (name == "dataset_id" || name == "content" || name == "id")
                    continue;
                String value = "";
                if (entry.getValue() != null)
                    value = entry.getValue().toString();
                document.add(new TextField(name, value, Field.Store.YES));
            }

            // commit document
            indexF.CommitDocument(document);

            if (all > datasetCountLimit) break;

        }
        System.out.println("All: " + all + " dataset number: " + cnt);

    }

    public void main() {
        id2text.clear();
        indexF = new IndexFactory();
        indexF.Init(GlobalVariances.store_Dir, GlobalVariances.commit_limit, GlobalVariances.globeAnalyzer);
        MapID2TripleText();
        GenerateDocument();
        indexF.CloseIndexWriter();
    }
}
