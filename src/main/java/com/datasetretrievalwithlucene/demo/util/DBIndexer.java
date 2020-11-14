package com.datasetretrievalwithlucene.demo.util;

import com.datasetretrievalwithlucene.demo.Bean.TripleID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

public class DBIndexer {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<Integer, List<TripleID>> id2triplelist = new HashMap<>();
    private Map<Integer, String> id2text = new HashMap<>();
    private IndexFactory indexF;

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
            sb.append(LabelMap.query(i, countList.get(i).getValue()));
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
            addCount(submap, tri.getSubject());
            addCount(premap, tri.getPredicate());
            addCount(objmap, tri.getObject());
            addCount(submap, tri.getSubject());
            addCount(summap, tri.getObject());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(GetTopUnitText(submap, GlobalVariances.maxEntityNumber, local_id));
        sb.append(";");
        sb.append(GetTopUnitText(objmap, GlobalVariances.maxEntityNumber, local_id));
        sb.append(";");
        sb.append(GetTopUnitText(submap, GlobalVariances.maxEntityNumber, local_id));
        sb.append(";");
        sb.append(GetTopUnitText(premap, GlobalVariances.maxRelationNumber, local_id));
        sb.append(";");
        return sb.toString();
    }

    /**
     * 得到数据集id到triple文本的映射
     */
    private void Mapid2tripletext() {
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList("SELECT * FROM triple ORDER BY dataset_local_id;");
        List<TripleID> tripleIDS = new ArrayList<>(); tripleIDS.clear();
        Integer currentid = 1;
        for (Map<String, Object> qi : queryList) {
            Integer local_id = Integer.parseInt(qi.get("dataset_local_id").toString());
            Integer sub = Integer.parseInt(qi.get("subject").toString());
            Integer pre = Integer.parseInt(qi.get("predicate").toString());
            Integer obj = Integer.parseInt(qi.get("object").toString());
            if(local_id > currentid) {
                id2text.put(currentid, GenerateText(tripleIDS, currentid));
                tripleIDS = new ArrayList<>(); tripleIDS.clear();
                currentid = local_id;
            } else {
                tripleIDS.add(new TripleID(sub, pre, obj));
            }
        }

    }
    public void main() {
        id2triplelist.clear();
        id2text.clear();
        indexF = new IndexFactory();
        indexF.Init(GlobalVariances.store_Dir, GlobalVariances.commit_limit, GlobalVariances.globeAnalyzer);
        Mapid2tripletext();

    }
}
