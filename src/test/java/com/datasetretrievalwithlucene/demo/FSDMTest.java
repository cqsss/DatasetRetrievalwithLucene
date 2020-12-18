package com.datasetretrievalwithlucene.demo;

import com.alibaba.fastjson.JSON;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import javafx.util.Pair;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
public class FSDMTest {
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private Map<String, Double> wT;
    private Map<String, Double> wO;
    private Map<String, Double> wU;
    private Map<Pair<String, String>, Long> fieldTermFreq;
    private Map<String, List<String>> fieldContent;
    private Map<String, Long> fieldDocLength;
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
    public void getCollectionStatistics(List<String> tokens) {
        try {
            for (Map.Entry jsonObject : GlobalVariances.getBoostWeights().entrySet()) {
                String field = jsonObject.getKey().toString();
                Double w = Double.parseDouble(jsonObject.getValue().toString());
                wT.put(field, w);
                wO.put(field, w);
                wU.put(field, w);
                for (String token : tokens) {
                    fieldTermFreq.put(new Pair<>(field, token), indexReader.totalTermFreq(new Term(field, new BytesRef(token))));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getDocumentStatistics(Integer doc_id) {
        try {
            fieldContent = new HashMap<>();
            fieldDocLength = new HashMap<>();

            for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
                String field = jsonObject.toString();
                Document document = indexReader.document(doc_id);
                fieldContent.put(field, Statistics.getTokens(document.get(field)));
                Terms terms = indexReader.getTermVector(doc_id, field);
                fieldDocLength.put(field, terms.getSumTotalTermFreq());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getTF_T(Integer doc_id, String field, String qi) {
        Double res = 0.0;
        try {
            Terms terms = indexReader.getTermVector(doc_id, field);
            BytesRef bytesRef = new BytesRef(qi);
            if (terms != null) {
                TermsEnum termsEnum = terms.iterator();
                if (termsEnum.seekExact(bytesRef))
                    res = (double) termsEnum.totalTermFreq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    public Double getTF_O(String field, String qi1, String qi2) {
        Double res = 0.0;
        List<String> content = fieldContent.get(field);
        for (Integer i = 0; i + 1 < content.size(); i++) {
            if (qi1.equals(content.get(i)) && qi2.equals(content.get(i + 1)))
                res += 1.0;
        }
        return res;
    }
    public Double getTF_U(String field, String qi1, String qi2) {
        Double res = 0.0;
        List<String> content = fieldContent.get(field);
        for (Integer i = 0; i < content.size(); i++) {
            Set<String> window = new HashSet<>();
            for (Integer j = 0; j < GlobalVariances.FSDMUWindowSize; j++) {
                window.add(content.get(i + j));
            }
            if(window.contains(qi1) && window.contains(qi2))
                res += 1.0;
        }
        return res;
    }

    public Double getFSDM_T(Integer doc_id, List<String> queries) {
        Double res = 0.0;
        try {
            for (String qi : queries) {
                for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    Double mu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    Double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    Double cf = (double) fieldTermFreq.get(qi);
                    Double Dj = (double) fieldDocLength.get(field);
                    res += wT.get(field) * (getTF_T(doc_id, field, qi) + mu * cf / Cj) / (Dj + mu);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Math.log(res);
    }
    public Double getFSDM_O(Integer doc_id, List<String> queries) {
        Double res = 0.0;
        try {
            for (Integer i = 0; i + 1 < queries.size(); i++) {
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    Double mu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    Double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    Double cf = (double) fieldTermFreq.get(qi1);//??
                    Double Dj = (double) fieldDocLength.get(field);
                    res += wO.get(field) * (getTF_O(field, qi1, qi2) + mu * cf / Cj) / (Dj + mu);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Math.log(res);
    }
    public Double getFSDM_U(Integer doc_id, List<String> queries) {
        Double res = 0.0;
        try {
            for (Integer i = 0; i + 1 < queries.size(); i++) {
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    Double mu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    Double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    Double cf = (double) fieldTermFreq.get(qi1);//??
                    Double Dj = (double) fieldDocLength.get(field);
                    res += wU.get(field) * (getTF_U(field, qi1, qi2) + mu * cf / Cj) / (Dj + mu);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Math.log(res);
    }
    public Double FSDM(Integer doc_id, List<String> tokens) {
        Double lambdaT = 1.0 / 3.0;
        Double lambdaO = 1.0 / 3.0;
        Double lambdaU = 1.0 / 3.0;
        getDocumentStatistics(doc_id);
        return lambdaT * getFSDM_T(doc_id, tokens) +
               lambdaO * getFSDM_O(doc_id, tokens) +
               lambdaU * getFSDM_U(doc_id, tokens);
    }
    @Test
    public void testFSDM() {
        init();
        /*try {
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
        }*/
    }
}
