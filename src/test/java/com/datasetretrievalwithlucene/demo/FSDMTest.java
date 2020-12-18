package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class FSDMTest {
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private Map<String, Double> wT;
    private Map<String, Double> wO;
    private Map<String, Double> wU;
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
    public Double getTF_T(Integer doc_id, String field, String qi) {
        Double res = 0.0;

        return res;
    }
    public Double getTF_O(Integer doc_id, String field, String qi) {
        Double res = 0.0;
        return res;
    }
    public Double getTF_U(Integer doc_id, String field, String qi) {
        Double res = 0.0;
        return res;
    }

    public Double getFSDM_T(Integer doc_id, List<String> queries) {
        Double res = 0.0;
        return Math.log(res);
    }
    public Double getFSDM_O(Integer doc_id, List<String> queries) {
        Double res = 0.0;
        return Math.log(res);
    }
    public Double getFSDM_U(Integer doc_id, List<String> queries) {
        Double res = 0.0;
        return Math.log(res);
    }
    public Double FSDM(Integer doc_id, List<String> tokens) {
        Double lambdaT = 1.0 / 3.0;
        Double lambdaO = 1.0 / 3.0;
        Double lambdaU = 1.0 / 3.0;
        return lambdaT * getFSDM_T(doc_id, tokens) +
               lambdaO * getFSDM_O(doc_id, tokens) +
               lambdaU * getFSDM_U(doc_id, tokens);
    }
    @Test
    public void testFSDM() {
        init();
        try {
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new QueryParser("content", analyzer);
            Query query = queryParser.parse("dog cat");
            TopDocs docsSearch = indexSearcher.search(query, 10);
            System.out.println("--- total ---: " + docsSearch.totalHits);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            for (ScoreDoc si : scoreDocs) {
                Integer docID = si.doc;
                System.out.println("doc_id: " + docID + ", score: " + si.score);
                Explanation e = indexSearcher.explain(query, si.doc);
                System.out.println("Explanation： \n" + e);
                System.out.println("********************************************************************");
                System.out.println("custom TFIDF: ");
                System.out.println(FSDM(docID, Statistics.getTokens("dog cat")));
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
