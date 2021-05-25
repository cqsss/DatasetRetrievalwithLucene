package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
public class ExperimentTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(ExperimentTest.class);
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private List<String> queryList;
    private List<String> poolingQueryList;
    private Directory directory;

    public void init() {
        try {
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readQueries(String fileName) {
        try {
            String str;
            queryList = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            while ((str = bufferedReader.readLine()) != null) {
                queryList.add(str);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getPoolingQueries(double k) {
        try {
            poolingQueryList = new ArrayList<>();
            String[] fields = GlobalVariances.queryFields;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            for (String qi : queryList) {
                String tmp =qi.replaceAll("\\p{P}"," ");
                Query query = queryParser.parse(tmp);
                int queryLength = query.toString().split(" ").length / fields.length;
                TopDocs docsSearch = indexSearcher.search(query, 500);
                ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
                int cnt = 0;
                for (ScoreDoc si : scoreDocs) {
                    double averageScore = si.score / (double) queryLength / (double) fields.length;
                    if (averageScore >= k) cnt++;
                    //System.out.println(e);
                }
                if(cnt >= 20)
                    poolingQueryList.add(qi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testTopK() {
        init();
        try {
            readQueries(GlobalVariances.testQueriesPath);
            for (double k = 0.0; k <= 1.0; k += 0.1) {
                int cnt = 0;
                for  (String qi : queryList){
                    String[] fields = GlobalVariances.queryFields;
                    Analyzer analyzer = new EnglishAnalyzer();
                    QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
                    String tmp =qi.replaceAll("\\p{P}"," ");
                    Query query = queryParser.parse(tmp);
                    int queryLength = query.toString().split(" ").length / fields.length;
                    TopDocs docsSearch = indexSearcher.search(query, 5);
                    ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
                    boolean flag = false;
                    for (ScoreDoc si : scoreDocs) {
                        double averageScore = si.score / (double) queryLength / (double) fields.length;
                        if (averageScore < k) flag = true;
                        //System.out.println(e);
                    }
                    if (flag) cnt ++;
                }
                System.out.printf("%d\t",cnt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testExperiment() {
        init();
        try {
            readQueries(GlobalVariances.testQueriesPath);
            for (String query : queryList) {
                String[] fields = GlobalVariances.queryFields;
                Analyzer analyzer = new EnglishAnalyzer();
                QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
                Query parsedQuery = queryParser.parse(query);
                TopDocs docsSearch = indexSearcher.search(parsedQuery, 500);
                ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
                Set<Integer> scoreSet = new HashSet<>();
                List<Pair<Integer, Double>> BM25scoreList = new ArrayList<>();
                for (ScoreDoc si : scoreDocs) {
                    Integer docID = si.doc;
                    Document document = indexReader.document(docID);
                    Integer datasetID = Integer.parseInt(document.get("dataset_id"));
                    Double score = 0.0;
//                    System.out.println("dataset_id: " + document.get("dataset_id") + ", score: " + si.score);
//                    Explanation e = indexSearcher.explain(parsedQuery, si.doc);
//                    System.out.println("Explanation： \n" + e);
                    for (String field : fields) {
                        score += RelevanceRanking.BM25(docID, field, Statistics.getTokens(query));
                    }
                    BM25scoreList.add(new Pair<>(datasetID, score));
                }
                BM25scoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                List<Pair<Integer, Double>> TFIDFscoreList = new ArrayList<>();
                for (ScoreDoc si : scoreDocs) {
                    Integer docID = si.doc;
                    Document document = indexReader.document(docID);    
                    Integer datasetID = Integer.parseInt(document.get("dataset_id"));
                    Double score = 0.0;
                    for (String field : fields) {
                        score += RelevanceRanking.TFIDF(docID, field, Statistics.getTokens(query));
                    }
                    TFIDFscoreList.add(new Pair<>(datasetID, score));
                }
                TFIDFscoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                List<Pair<Integer, Double>> FSDMscoreList = new ArrayList<>();
                for (ScoreDoc si : scoreDocs) {
                    int docID = si.doc;
                    Document document = indexReader.document(docID);
                    Integer datasetID = Integer.parseInt(document.get("dataset_id"));
                    Double score = RelevanceRanking.FSDM(docID, Statistics.getTokens(query));
                    FSDMscoreList.add(new Pair<>(datasetID, score));
                }
                FSDMscoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                for (int i = 0; i < GlobalVariances.queryPoolSize.length; i++) {
                    if (scoreDocs.length<GlobalVariances.queryPoolSize[i]) break;
                    for (int j = 0; j < GlobalVariances.queryPoolSize[i]; j++) {
                        scoreSet.add(BM25scoreList.get(j).getKey());
                        scoreSet.add(TFIDFscoreList.get(j).getKey());
                        scoreSet.add(FSDMscoreList.get(j).getKey());
                    }
                    System.out.printf("%d ",scoreSet.size());
                }
                System.out.printf("\n");
//                System.out.println("BM25-------------------------------------------");
//                for (Integer i = 0; i < 10; i++) {
//                    System.out.println(BM25scoreList.get(i));
//                }
//                System.out.println("TFIDF------------------------------------------");
//                for (Integer i = 0; i < 10; i++) {
//                    System.out.println(TFIDFscoreList.get(i));
//                }
//                System.out.println("FSDM-------------------------------------------");
//                for (Integer i = 0; i < 10; i++) {
//                    System.out.println(FSDMscoreList.get(i));
//                }
//                System.out.println("------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testPoolingSize() {
        init();
        try {
            readQueries(GlobalVariances.testQueriesPath);
            Map<String, List<Integer>> poolingMap = new HashMap<>();
            for (String q : queryList) {
                System.out.printf("%s\t",q);
                List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(q);
                List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(q);
                List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(q);
                List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(q);
                if (TFIDFScoreList.size() < 20 || BM25ScoreList.size() < 20 ||FSDMScoreList.size() < 20) {
                    continue;
                }
                Set<Integer> scoreSet = new HashSet<>();
                List<Integer> tmpList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < GlobalVariances.queryPoolSize[i]; j++) {
                        scoreSet.add(TFIDFScoreList.get(j).getKey());
                        scoreSet.add(BM25ScoreList.get(j).getKey());
                        scoreSet.add(FSDMScoreList.get(j).getKey());
                        scoreSet.add(DPRScoreList.get(j).getKey());
                    }
                    tmpList.add(scoreSet.size());
                    System.out.printf("%d\t",scoreSet.size());
                }
                poolingMap.put(q, tmpList);
                System.out.print("\n");
            }

            logger.info("Completed all pooling. Total queries: " + poolingMap.size());
            for (int i = 0; i <= 10; i ++) {
                double k = i * 0.1;
                File query_file = new File(GlobalVariances.poolSizePath + String.format("%.1f", k) + ".out");
                if (!query_file.exists()) {
                    query_file.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(query_file.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                getPoolingQueries(k);
                for (String q : poolingQueryList) {
                    if (!poolingMap.containsKey(q))
                        continue;
                    List<Integer> tmpList = poolingMap.get(q);
                    bufferedWriter.write(q + ";");
                    for (int j : tmpList) {
                        bufferedWriter.write(j+";");
                    }
                    bufferedWriter.write("\n");
                }
                bufferedWriter.close();
                logger.info("Completed pooling k=" + String.format("%.1f", k) + " Total queries: " + poolingQueryList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBM25Results() {
        List<Map<String, Object>> res = jdbcTemplate.queryForList("SELECT * FROM query ORDER BY query_id");
        for (Map<String, Object> qi : res) {
            int query_id = Integer.parseInt(qi.get("query_id").toString());
            String query_text = qi.get("query_text").toString();
            List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(query_text);
            String sql = "INSERT INTO results VALUES (?, ?, ?, ?, ?);";
            for (int i = 0; i < 20; i++) {
                jdbcTemplate.update(sql, query_id, BM25ScoreList.get(i).getKey(), i + 1, "BM25_ALL", null);
            }
            System.out.println("query: " + query_id);
        }
    }

    @Test
    public void testTFIDFResults() {
        List<Map<String, Object>> res = jdbcTemplate.queryForList("SELECT * FROM query ORDER BY query_id");
        for (Map<String, Object> qi : res) {
            int query_id = Integer.parseInt(qi.get("query_id").toString());
            String query_text = qi.get("query_text").toString();
            List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(query_text);
            String sql = "INSERT INTO results VALUES (?, ?, ?, ?, ?);";
            for (int i = 0; i < 20; i++) {
                jdbcTemplate.update(sql, query_id, TFIDFScoreList.get(i).getKey(), i + 1, "TFIDF_ALL", null);
            }
            System.out.println("query: " + query_id);
        }
    }

    @Test
    public void testFSDMResults() {
        List<Map<String, Object>> res = jdbcTemplate.queryForList("SELECT * FROM query ORDER BY query_id");
        for (Map<String, Object> qi : res) {
            int query_id = Integer.parseInt(qi.get("query_id").toString());
            String query_text = qi.get("query_text").toString();
            List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(query_text);
            String sql = "INSERT INTO results VALUES (?, ?, ?, ?, ?);";
            for (int i = 0; i < 20; i++) {
                jdbcTemplate.update(sql, query_id, FSDMScoreList.get(i).getKey(), i + 1, "FSDM_ALL", null);
            }
            System.out.println("query: " + query_id);
        }
    }

    @Test
    public void testDPRResults() {
        List<Map<String, Object>> res = jdbcTemplate.queryForList("SELECT * FROM query ORDER BY query_id");
        for (Map<String, Object> qi : res) {
            int query_id = Integer.parseInt(qi.get("query_id").toString());
            String query_text = qi.get("query_text").toString();
            List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(query_text);
            String sql = "INSERT INTO results VALUES (?, ?, ?, ?, ?);";
            for (int i = 0; i < 20; i++) {
                jdbcTemplate.update(sql, query_id, DPRScoreList.get(i).getKey(), i + 1, "DPR", null);
            }
            System.out.println("query: " + query_id);
        }
    }

    public double calculateDCG(List<Integer> ratingList, int k) {
        double res = 0.0;
        for (int i = 1; i <= k; i++) {
            res += (Math.pow(2.0, ratingList.get(i - 1)) - 1.0) / (Math.log(i + 1.0) / Math.log(2.0));
        }
        return res;
    }

    public double calculateNDCG(String method, int k, int query_id) {
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT * FROM results WHERE method='" + method + "' AND query_id=" + query_id + " ORDER BY ranknum");
        List<Integer> resultRating = new ArrayList<>();
        List<Integer> idealRating = jdbcTemplate.queryForList("SELECT rating FROM annotation WHERE query_id=" + query_id + " ORDER BY rating DESC", Integer.class);
        for (Map<String, Object> qi : resultList) {
            int dataset_id = Integer.parseInt(qi.get("dataset_id").toString());
            List<Integer> ratingList = jdbcTemplate.queryForList("SELECT rating FROM annotation WHERE query_id=" + query_id + " AND dataset_id=" + dataset_id, Integer.class);
            if (ratingList.size() == 0)
                resultRating.add(0);
            else
                resultRating.add(ratingList.get(0));
        }
        double DCG = calculateDCG(resultRating, k);
        double iDCG = calculateDCG(idealRating, k);
        return DCG / iDCG;
    }

    public double calculateAP(String method, int k, int query_id) {
        double res = 0.0;
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("SELECT * FROM results WHERE method='" + method + "' AND query_id=" + query_id + " ORDER BY ranknum");
        List<Integer> resultRating = new ArrayList<>();
        for (Map<String, Object> qi : resultList) {
            int dataset_id = Integer.parseInt(qi.get("dataset_id").toString());
            List<Integer> ratingList = jdbcTemplate.queryForList("SELECT rating FROM annotation WHERE query_id=" + query_id + " AND dataset_id=" + dataset_id, Integer.class);
            if (ratingList.size() == 0)
                resultRating.add(0);
            else
                resultRating.add(ratingList.get(0));
        }
        int cnt = 0;
        for (int i = 1; i <= k; i++) {
            if (resultRating.get(i - 1) != 0) {
                cnt++;
                res += (double) cnt / (double) i;
            }
        }
        res /= (double) k;
        return res;
    }

    @Test
    public void calculateMetrics() {
        List<Integer> queryList = jdbcTemplate.queryForList("SELECT query_id FROM query ORDER BY query_id", Integer.class);
        for (String mi : GlobalVariances.methodList) {
            System.out.println("method: " + mi);
            for (int qi : queryList) {
                System.out.println("\tquery_id: " + qi);
                for (int i : GlobalVariances.metricsK) {
                    double nDCG = calculateNDCG(mi, i, qi);
                    String sql = "INSERT INTO ndcg VALUES (?, ?, ?, ?, ?);";
                    jdbcTemplate.update(sql, mi, qi, i, nDCG, null);
                    System.out.println("\t\tnDCG@" + i + " :" + nDCG);
                    double AP = calculateAP(mi, i, qi);
                    sql = "INSERT INTO ap VALUES (?, ?, ?, ?, ?);";
                    jdbcTemplate.update(sql, mi, qi, i, AP, null);
                    System.out.println("\t\tAP@" + i + " :" + AP);
                }
            }
        }
    }

    @Test
    public void calculateMeanMetrics() {
        int queryNumber = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM query", Integer.class);
        for (String mi : GlobalVariances.methodList) {
            System.out.println("method: " + mi);
            for (int i : GlobalVariances.metricsK) {
                List<Integer> nDCGList = jdbcTemplate.queryForList("SELECT ndcg_score FROM ndcg WHERE method='" + mi + "' AND k=" + i, Integer.class);
                List<Integer> APList = jdbcTemplate.queryForList("SELECT ap_score FROM ap WHERE method='" + mi + "' AND k=" + i, Integer.class);
                double mean_nDCG = 0.0;
                double mean_AP = 0.0;
                for (int j = 0; j < queryNumber; j++) {
                    mean_nDCG += nDCGList.get(j);
                    mean_AP += APList.get(j);
                }
                mean_nDCG /= (double) queryNumber;
                mean_AP /= (double) queryNumber;
                String sql = "INSERT INTO metric VALUES (?, ?, ?, ?, ?);";
                jdbcTemplate.update(sql, mi, i, mean_nDCG, mean_AP, null);
                System.out.println("\tnDCG@" + i + " :" + mean_nDCG + "\tMAP@" + i + " :" + mean_AP);
            }
        }
    }
}
