package com.datasetretrievalwithlucene.demo.util;

import com.datasetretrievalwithlucene.demo.Bean.TripleID;
import org.apache.lucene.document.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Repository
@Service
public class DBIndexer {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private LabelMap labelMap;

    private Map<Integer, List<TripleID>> id2triplelist = new HashMap<>();
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
     * @param local_id
     * @return
     */
    private String GetTopUnitText(Map<Integer, Integer> count, Integer limit, Integer local_id) {
        List<Map.Entry<Integer, Integer>> countList = new ArrayList<>(count.entrySet());
        Collections.sort(countList, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        StringBuilder sb = new StringBuilder();

        for (Integer i = 0; i < countList.size() && i < limit; i++) {
            sb.append(labelMap.query(local_id, countList.get(i).getValue()));
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * 根据实体生成文本
     * @param datasettriple
     * @param local_id
     * @return
     */
    private String GenerateText(List<TripleID> datasettriple, Integer local_id) {
        Map<Integer, Integer> submap = new HashMap<>(); submap.clear();
        Map<Integer, Integer> premap = new HashMap<>(); premap.clear();
        Map<Integer, Integer> objmap = new HashMap<>(); objmap.clear();
        Map<Integer, Integer> summap = new HashMap<>(); summap.clear();
        for (TripleID tri : datasettriple) {
            AddCount(submap, tri.getSubject());
            AddCount(premap, tri.getPredicate());
            AddCount(objmap, tri.getObject());
            AddCount(submap, tri.getSubject());
            AddCount(summap, tri.getObject());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(GetTopUnitText(submap, GlobalVariances.maxEntityNumber, local_id)); sb.append(";");
        sb.append(GetTopUnitText(objmap, GlobalVariances.maxEntityNumber, local_id)); sb.append(";");
        sb.append(GetTopUnitText(submap, GlobalVariances.maxEntityNumber, local_id)); sb.append(";");
        sb.append(GetTopUnitText(premap, GlobalVariances.maxRelationNumber, local_id)); sb.append(";");
        return sb.toString();
    }

    /**
     * 得到数据集id到triple文本的映射
     */
    private void MapID2TripleText() {
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList("SELECT * FROM triple ORDER BY dataset_local_id;");
        System.out.println(queryList);
        List<TripleID> tripleIDS = new ArrayList<>(); tripleIDS.clear();
        Integer currentid = 1;
        for (Map<String, Object> qi : queryList) {
            Integer local_id = Integer.parseInt(qi.get("dataset_local_id").toString());
            Integer sub = Integer.parseInt(qi.get("subject").toString());
            Integer pre = Integer.parseInt(qi.get("predicate").toString());
            Integer obj = Integer.parseInt(qi.get("object").toString());
            if (local_id > currentid) {
                id2text.put(currentid, GenerateText(tripleIDS, currentid));
                tripleIDS = new ArrayList<>(); tripleIDS.clear();
                currentid = local_id;
            } else {
                tripleIDS.add(new TripleID(sub, pre, obj));
            }
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
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList("SELECT * FROM dataset");
        for (Map<String, Object> qi : queryList) {
            Document document = new Document();

            all ++;
            // local ID
            Integer local_id = Integer.parseInt(qi.get("local_id").toString());
            document.add(new StringField("local_id", local_id.toString(), Field.Store.YES));
            if (local_id > 0) cnt ++;

            // Content
            String content =GetTextFromLocalID(local_id);
            document.add(new TextField("content", content, Field.Store.YES));

            // Dataset ID
            String id = qi.get("id").toString();
            document.add(new StringField("id", id, Field.Store.YES));

            // Normal Fields
            for (Map.Entry<String, Object> entry : qi.entrySet()) {
                String name = entry.getKey();
                if (name == "local_id" || name == "content" || name == "id")
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
        System.out.println("All: " + all + "dataset number: " + cnt);

    }

    public void main() {
        id2triplelist.clear();
        id2text.clear();
        indexF = new IndexFactory();
        indexF.Init(GlobalVariances.store_Dir, GlobalVariances.commit_limit, GlobalVariances.globeAnalyzer);
        MapID2TripleText();
        GenerateDocument();
        indexF.CloseIndexWriter();
    }
}
