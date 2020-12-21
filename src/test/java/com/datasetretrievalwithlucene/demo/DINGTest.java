package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.QualityRanking;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class DINGTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(QualityRanking.class);
    private static Map<Pair<Integer, Integer>, Integer> edgeCountSet = new HashMap();
    private static Map<Pair<Integer, Integer>, Map<Integer, Integer>> edgeSet = new HashMap<>();
    private static Map<Integer, Integer> outLinkCount = new HashMap<>();
    private static Map<Integer, Integer> inLinkCount = new HashMap<>();
    private static Map<Integer, Integer> predicateCount = new HashMap<>();
    private static Map<Integer, List<Integer>> outLinks = new HashMap<>();
    private static Map<Integer, List<Integer>> inLinks = new HashMap<>();
    private static Integer maxID = 0;
    private static Integer linkCount = 0;
    public static void addEdge(Integer u, Integer v, Integer c, Integer p) {
        Integer tmp = 0;
        Pair<Integer, Integer> uv = new Pair<>(u, v);
        if (edgeSet.containsKey(uv)) {
            if (edgeSet.get(uv).containsKey(p)) {
                tmp = edgeSet.get(uv).get(p);
            }
            edgeSet.get(uv).put(p, tmp + c);
        } else {
            edgeSet.put(uv, new HashMap<>(p, c));
        }
        tmp = 0;
        if (edgeCountSet.containsKey(uv)) {
            tmp = edgeCountSet.get(uv);
        }
        edgeCountSet.put(uv, tmp + c);
        if (outLinks.containsKey(u)) {
            outLinks.get(u).add(v);
        } else {
            List<Integer> tmpList = new ArrayList<>();
            tmpList.add(v);
            outLinks.put(u, tmpList);
        }
        if (inLinks.containsKey(v)) {
            inLinks.get(v).add(u);
        } else {
            List<Integer> tmpList = new ArrayList<>();
            tmpList.add(u);
            inLinks.put(v, tmpList);
        }
        tmp = 0;
        if (outLinkCount.containsKey(u))
            tmp = outLinkCount.get(u);
        outLinkCount.put(u, tmp + c);
        tmp = 0;
        if (inLinkCount.containsKey(v))
            tmp = inLinkCount.get(v);
        inLinkCount.put(v, tmp + c);
        tmp = 0;
        if (predicateCount.containsKey(p)) {
            tmp = predicateCount.get(p);
        }
        linkCount ++;
        predicateCount.put(p, tmp + 1);
    }
    public static void readDataBase(JdbcTemplate jdbcTemplate) {
        try {
            List<Map<String, Object>> res;
            //res = jdbcTemplate.queryForList("SELECT sub_ds,obj_ds,predicate,count FROM outerlink3 LIMIT 0,10");
            res = jdbcTemplate.queryForList("SELECT sub_ds,obj_ds,predicate,count FROM sample_outerlink");
            for (Map<String, Object> ri : res) {
                Integer dataset1 = Integer.parseInt(ri.get("sub_ds").toString());
                Integer dataset2 = Integer.parseInt(ri.get("obj_ds").toString());
                maxID = Math.max(maxID, dataset1);
                maxID = Math.max(maxID, dataset2);
                Integer predicate = Integer.parseInt(ri.get("predicate").toString());
                Integer count = Integer.parseInt(ri.get("count").toString());
                addEdge(dataset1, dataset2, count, predicate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Double getTF(Integer i, Integer j) {
        Double res = 0.0;
        for (Integer k : outLinks.get(i)) {
            res = Math.max(res, edgeCountSet.get(new Pair<>(i, k)));
        }
        res = edgeCountSet.get(new Pair<>(i, j)) / res;
        return res;
    }
    public static Double getIDF(Integer i, Integer j) {
        Double tmp = 0.0;
        for (Integer p : edgeSet.get(new Pair<>(i, j)).keySet()) {
            tmp += predicateCount.get(p);
        }
        return Math.log((double) linkCount / (1.0 + tmp));
    }
    public static Double getW(Integer i, Integer j) {
        return getTF(i, j) * getIDF(i, j);
    }
    public static Double getP(Integer i, Integer j) {
        Double res = 0.0;
        for (Integer k : outLinks.get(i)) {
            res += getW(i, k);
        }
        res = getW(i, j) / res;
        return res;
    }
    public static void DING(String field, JdbcTemplate jdbcTemplate) {
        readDataBase(jdbcTemplate);
        try {
            //Double N = (double) indexReader.getDocCount(field);
//            maxID = jdbcTemplate.queryForObject("SELECT MAX(dataset1) FROM outerlink", Integer.class);
//            maxID = Math.max(maxID, jdbcTemplate.queryForObject("SELECT MAX(dataset2) FROM outerlink", Integer.class));
            maxID = 15;
            Double N = (double) maxID;
            Double d = 0.85;
            List<Double> pr = new ArrayList<>();
            List<Double> tmp = new ArrayList<>();
            for (Integer i = 0; i < maxID; i++) {
                pr.add(1.0 / N);
                tmp.add(1.0 / N);
            }
            Integer cnt = 0;
            Integer t = 0;
            while(cnt != maxID) {
                cnt = 0;
                t ++;
                Double sumPR;
                for (Integer i = 0; i < maxID; i++) {
                    sumPR = 0.0;
                    if(inLinks.containsKey(i + 1)) {
                        for (Integer j : inLinks.get(i + 1)) {
                            sumPR += pr.get(j - 1) * getP(j, i+1);
                        }
                    }
                    sumPR *= d;
                    sumPR += (1.0 - d) / N;
                    if(sumPR.equals(pr.get(i)))
                        cnt++;
                    tmp.set(i, sumPR);
                }
                pr = tmp;
                System.out.println(pr);
            }
            System.out.println(pr);
            //logger.info(pr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testDING() {
        try {
            DING("content", jdbcTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
