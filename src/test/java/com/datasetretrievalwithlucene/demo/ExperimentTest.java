package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.Bean.DatasetScore;
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
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

@SpringBootTest
public class ExperimentTest {
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
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                queryList.add(str);
            }
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
            readQueries(GlobalVariances.testQueriesPath);
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
                if(cnt>20)
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
            for (double k = 0.0; k <= 2.0; k += 0.1) {
                int cnt = 0;
                for  (String qi : queryList){
                    String[] fields = GlobalVariances.queryFields;
                    Analyzer analyzer = new EnglishAnalyzer();
                    QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
                    Query query = queryParser.parse(qi);
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
    public void testPooling() {
        init();
        try {
            readQueries(GlobalVariances.testQueriesPath);
            Map<String, List<Integer>> poolingMap = new HashMap<>();
            for (String q : queryList) {
                List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(q);
                List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(q);
                List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(q);
                List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(q);
                if (TFIDFScoreList.size() < 100 || BM25ScoreList.size() < 100 ||FSDMScoreList.size() < 100) {
                    continue;
                }
                Set<Integer> scoreSet = new HashSet<>();
                List<Integer> tmpList = new ArrayList<>();
                for (int i = 0; i < GlobalVariances.queryPoolSize.length; i++) {
                    for (int j = 0; j < GlobalVariances.queryPoolSize[i]; j++) {
                        scoreSet.add(TFIDFScoreList.get(j).getKey());
                        scoreSet.add(BM25ScoreList.get(j).getKey());
                        scoreSet.add(FSDMScoreList.get(j).getKey());
                        scoreSet.add(DPRScoreList.get(j).getKey());
                    }
                    tmpList.add(scoreSet.size());
                    System.out.printf("%d ",scoreSet.size());
                }
                poolingMap.put(q, tmpList);
                System.out.print("\n");
            }
            logger.info("Completed all pooling. Total queries: " + poolingMap.size());
            for (int i = 0; i <= 20; i ++) {
                double k = i * 0.1;
                File query_file = new File("src/main/resources/out/poolsize_" + String.format("%.1f", k) + ".out");
                if (!query_file.exists()) {
                    query_file.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(query_file.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                getPoolingQueries(k);
                for (String q : poolingQueryList) {
                    List<Integer> tmpList = poolingMap.get(q);
                    bufferedWriter.write(q + ";");
                    for (int j : tmpList) {
                        bufferedWriter.write(j+";");
                    }
                    bufferedWriter.write("\n");
                }
                bufferedWriter.close();
                logger.info("Completed pooling k=" + String.format("%.1f", k) + " Total queries: " +poolingQueryList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
