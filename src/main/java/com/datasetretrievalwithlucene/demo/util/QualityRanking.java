package com.datasetretrievalwithlucene.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QualityRanking {
    private static final Logger logger = LoggerFactory.getLogger(QualityRanking.class);
    /**
     * outLinks.get(i): i指向的点的集合
     * inLinks.get(i): 指向i的点的集合
     * outLinkCount.get(i): i指出的边数
     * inLinkCount.get(i): 指向i的边数
     */
    private static final Map<Integer, Integer> outLinkCount = new HashMap<>();
    private static final Map<Integer, Integer> inLinkCount = new HashMap<>();
    private static final Map<Integer, List<Integer>> outLinks = new HashMap<>();
    private static final Map<Integer, List<Integer>> inLinks = new HashMap<>();
    private static Integer maxID = 0;

    public static void addEdge(Integer u, Integer v) {
        if (outLinks.containsKey(u)) {
            outLinks.get(u).add(v);
        } else {
            List<Integer> tmp = new ArrayList<>();
            tmp.add(v);
            outLinks.put(u, tmp);
        }
        if (inLinks.containsKey(v)) {
            inLinks.get(v).add(u);
        } else {
            List<Integer> tmp = new ArrayList<>();
            tmp.add(u);
            inLinks.put(v, tmp);
        }
        Integer tmp = 0;
        if (outLinkCount.containsKey(u))
            tmp = outLinkCount.get(u);
        outLinkCount.put(u, tmp + 1);
        tmp = 0;
        if (inLinkCount.containsKey(v))
            tmp = inLinkCount.get(v);
        inLinkCount.put(v, tmp + 1);
    }

    public static void readDataBase(JdbcTemplate jdbcTemplate) {
        try {
            List<Map<String, Object>> res;
            res = jdbcTemplate.queryForList("SELECT sub_ds,obj_ds,count FROM sample_outerlink");
            for (Map<String, Object> ri : res) {
                Integer dataset1 = Integer.parseInt(ri.get("sub_ds").toString());
                Integer dataset2 = Integer.parseInt(ri.get("obj_ds").toString());
                maxID = Math.max(maxID, dataset1);
                maxID = Math.max(maxID, dataset2);
                addEdge(dataset1, dataset2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单向边版本的PageRank
     *
     * @param field
     */
    public static void iterativePageRank(String field, JdbcTemplate jdbcTemplate) {
        readDataBase(jdbcTemplate);
        try {
            //Double N = (double) indexReader.getDocCount(field);
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
            while (cnt != maxID) {
                cnt = 0;
                t++;
                Double sumPR;
                for (Integer i = 0; i < maxID; i++) {
                    sumPR = 0.0;
                    if (inLinks.containsKey(i + 1)) {
                        for (Integer j : inLinks.get(i + 1)) {
                            sumPR += pr.get(j - 1) / outLinkCount.get(j);
                        }
                    }
                    sumPR *= d;
                    sumPR += (1.0 - d) / N;
                    if (sumPR.equals(pr.get(i)))
                        cnt++;
                    tmp.set(i, sumPR);
                }
                pr = tmp;
            }
            logger.info(pr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 仅考虑度数的DRank
     *
     * @param field
     * @param jdbcTemplate
     */
    public static void DRank(String field, JdbcTemplate jdbcTemplate) {
        readDataBase(jdbcTemplate);
        try {
            //Double N = (double) indexReader.getDocCount(field);
            maxID = 15;
            Double tmp;
            List<Double> pr = new ArrayList<>();
            for (Integer i = 0; i < maxID; i++) {
                tmp = 0.0;
                if (outLinkCount.containsKey(i))
                    tmp += outLinkCount.get(i);
                if (inLinkCount.containsKey(i))
                    tmp += inLinkCount.get(i);
                pr.add(tmp);
            }
            System.out.println(pr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
