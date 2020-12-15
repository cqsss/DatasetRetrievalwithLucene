package com.datasetretrievalwithlucene.demo.util;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRank {
    private static final Logger logger = LoggerFactory.getLogger(PageRank.class);
    private static Map<Integer, Integer> outLinkCount = new HashMap<>();
    private static Map<Integer, Integer> inLinkCount = new HashMap<>();
    private static Map<Integer, List<Integer>> outLinks = new HashMap<>();
    private static Map<Integer, List<Integer>> inLinks = new HashMap<>();
    private static Integer maxID = 0;
    public static void addEdge(Integer u, Integer v, Integer c) {
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
        outLinkCount.put(u, tmp + c);
        tmp = 0;
        if (inLinkCount.containsKey(v))
            tmp = inLinkCount.get(v);
        inLinkCount.put(v, tmp + c);
    }
    public static void ReadDataBase(JdbcTemplate jdbcTemplate) {
        try {
            List<Map<String, Object>> res;
            res = jdbcTemplate.queryForList("SELECT dataset1,dataset2,count FROM outerlink LIMIT 0,10");
            for (Map<String, Object> ri : res) {
                Integer dataset1 = Integer.parseInt(ri.get("dataset1").toString());
                Integer dataset2 = Integer.parseInt(ri.get("dataset2").toString());
                maxID = Math.max(maxID, dataset1);
                maxID = Math.max(maxID, dataset2);
                Integer count = Integer.parseInt(ri.get("count").toString());
                addEdge(dataset1, dataset2, count);
            }
//            addEdge(13,9,10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单向边版本的PageRank
     * @param field
     */
    public static void IterativePageRank(String field, JdbcTemplate jdbcTemplate) {
        ReadDataBase(jdbcTemplate);

        try {
            //Double N = (double) indexReader.getDocCount(field);
//            maxID = jdbcTemplate.queryForObject("SELECT MAX(dataset1) FROM outerlink", Integer.class);
//            maxID = Math.max(maxID, jdbcTemplate.queryForObject("SELECT MAX(dataset2) FROM outerlink", Integer.class));
            maxID = 18;
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
                            sumPR += pr.get(j - 1) / outLinkCount.get(j);
                        }
                    }
                    sumPR *= d;
                    sumPR += (1.0 - d) / N;
                    if(sumPR.equals(pr.get(i)))
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
}
