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
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
public class ExperimentTest {
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private List<String> queries;
    public void init() {
        try {
            Directory directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readQueries(String fileName) {
        try {
            String str;
            queries = new ArrayList<>();
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                str=str.replaceAll("\\p{P}"," ");
                queries.add(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testTopK() {
        init();
        try {
            readQueries(GlobalVariances.queriesPath);
            for (double k = 0.0; k <= 2.0; k += 0.1) {
                int cnt = 0;
                for  (String qi : queries){
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
            readQueries(GlobalVariances.queriesPath);
            for (String query : queries) {
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
}
