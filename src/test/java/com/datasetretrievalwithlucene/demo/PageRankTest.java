package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class PageRankTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PageRankTest.class);
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private Map<Integer, Integer> outLinkCount = new HashMap<>();
    private Map<Integer, Integer> inLinkCount = new HashMap<>();
    private Map<Integer, List<Integer>> outLinks = new HashMap<>();
    private Map<Integer, List<Integer>> inLinks = new HashMap<>();
    private Integer maxID = 0;
    public void init() {
        try {
            Similarity similarity= new ClassicSimilarity();
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(similarity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addEdge(Integer u, Integer v, Integer c) {
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
    public void ReadDataBase() {
        try {
            List<Map<String, Object>> res;
            res = jdbcTemplate.queryForList("SELECT dataset1,dataset2,count FROM outerlink");
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
    public void PageRank(String field) {
        ReadDataBase();

        try {
            //Double N = (double) indexReader.getDocCount(field);
            maxID = jdbcTemplate.queryForObject("SELECT MAX(dataset1) FROM outerlink", Integer.class);
            maxID = Math.max(maxID, jdbcTemplate.queryForObject("SELECT MAX(dataset2) FROM outerlink", Integer.class));
            //maxID = 18;
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
    @Test
    public void testPageRank() {
        init();
        try {
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new QueryParser("content", analyzer);
            Query query = queryParser.parse("dog cat");
            TopDocs docsSearch = indexSearcher.search(query, 1000);
            System.out.println("--- total ---: " + docsSearch.totalHits);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            PageRank("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
