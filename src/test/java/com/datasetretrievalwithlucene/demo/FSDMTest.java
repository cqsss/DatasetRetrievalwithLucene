package com.datasetretrievalwithlucene.demo;

import com.alibaba.fastjson.JSON;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
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
            wT = new HashMap<>();
            wO = new HashMap<>();
            wU = new HashMap<>();
            fieldTermFreq = new HashMap<>();
            Double base = 0.0;
            for (Map.Entry jsonObject : GlobalVariances.getFSDMBoostWeights().entrySet()) {
                String field = jsonObject.getKey().toString();
                Double w = Double.parseDouble(jsonObject.getValue().toString());
                base += w;
                for (String token : tokens) {
                    fieldTermFreq.put(new Pair<>(field, token), indexReader.totalTermFreq(new Term(field, new BytesRef(token))));
                }
            }
            for (Map.Entry jsonObject : GlobalVariances.getFSDMBoostWeights().entrySet()) {
                String field = jsonObject.getKey().toString();
                Double w = Double.parseDouble(jsonObject.getValue().toString()) / base;
                wT.put(field, w);
                wO.put(field, w);
                wU.put(field, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getDocumentStatistics(Integer doc_id) {
        try {
            fieldContent = new HashMap<>();
            fieldDocLength = new HashMap<>();

            for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                String field = jsonObject.toString();
                Document document = indexReader.document(doc_id);
                fieldContent.put(field, Statistics.getTokens(document.get(field)));
                Terms terms = indexReader.getTermVector(doc_id, field);
                if (terms != null) fieldDocLength.put(field, terms.getSumTotalTermFreq());
                else fieldDocLength.put(field, 0L);
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
        //System.out.println(qi + " TF_T: " + res);
        return res;
    }
    public Double getTF_O(String field, String qi1, String qi2) {
        Double res = 0.0;
        List<String> content = fieldContent.get(field);
        for (Integer i = 0; i + 1 < content.size(); i++) {
            if (qi1.equals(content.get(i)) && qi2.equals(content.get(i + 1)))
                res += 1.0;
        }
        //System.out.println(qi1 + " " + qi2 + " TF_O: " + res);
        return res;
    }
    public Double getTF_U(String field, String qi1, String qi2) {
        Double res = 0.0;
        List<String> content = fieldContent.get(field);
        for (Integer i = 0; i + GlobalVariances.FSDMUWindowSize <= content.size(); i++) {
            Set<String> window = new HashSet<>();
            for (Integer j = 0; j < GlobalVariances.FSDMUWindowSize; j++) {
                window.add(content.get(i + j));
            }
            if(window.contains(qi1) && window.contains(qi2))
                res += 1.0;
        }
        //System.out.println(qi1 + " " + qi2 + " TF_U: " + res);
        return res;
    }

    public Double getFSDM_T(Integer doc_id, List<String> queries) {
        Double res = 0.0;

        try {
            for (String qi : queries) {
                Double tmp = 0.0;
                for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    if(wT.get(field) == 0.0) continue;
                    Double miu = (double) indexReader.getSumTotalTermFreq(field) / ((double) indexReader.getDocCount(field));
                    Double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    Double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi));
                    Double Dj = (double) fieldDocLength.get(field);
                    tmp += wT.get(field) * (getTF_T(doc_id, field, qi) + miu * cf / Cj) / (Dj + miu);
                }
                System.out.println("T: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FSDM_T: " + res);
        return res;
    }
    public Double getFSDM_O(List<String> queries) {
        Double res = 0.0;
        try {
            for (Integer i = 0; i + 1 < queries.size(); i++) {
                Double tmp = 0.0;
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    if(wO.get(field) == 0.0) continue;
                    Double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    Double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    Double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi1)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi1));
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi2)))
                        cf = Math.min(cf, (double) fieldTermFreq.get(new Pair<>(field, qi2)));
                    Double Dj = (double) fieldDocLength.get(field);
                    tmp += wO.get(field) * (getTF_O(field, qi1, qi2) + miu * cf / Cj) / (Dj + miu);
                }
                System.out.println("O: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FSDM_O: " + res);
        return res;
    }
    public Double getFSDM_U(List<String> queries) {
        Double res = 0.0;
        try {
            for (Integer i = 0; i + 1 < queries.size(); i++) {
                Double tmp = 0.0;
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    if(wU.get(field) == 0.0) continue;
                    Double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    Double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    Double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi1)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi1));
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi2)))
                        cf = Math.min(cf, (double) fieldTermFreq.get(new Pair<>(field, qi2)));
                    Double Dj = (double) fieldDocLength.get(field);
                    tmp += wU.get(field) * (getTF_U(field, qi1, qi2) + miu * cf / Cj) / (Dj + miu);
                }
                System.out.println("U: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FSDM_U: " + res);
        return res;
    }
    public Double FSDM(Integer doc_id, List<String> tokens) {
        Double lambdaT = 1.0 / 3.0;
        Double lambdaO = 1.0 / 3.0;
        Double lambdaU = 1.0 / 3.0;
        getDocumentStatistics(doc_id);
        return lambdaT * getFSDM_T(doc_id, tokens) +
               lambdaO * getFSDM_O(tokens) +
               lambdaU * getFSDM_U(tokens);
    }
    @Test
    public void testFSDM() {
        init();
        try {
            getCollectionStatistics(Statistics.getTokens("Top countries in production from aquaculture 2017"));
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new QueryParser("content", analyzer);
            Query query = queryParser.parse("Top countries in production from aquaculture 2017");
            TopDocs docsSearch = indexSearcher.search(query, 100);
            System.out.println("--- total ---: " + docsSearch.totalHits);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            for (ScoreDoc si : scoreDocs) {
                Integer docID = si.doc;
                System.out.println("doc_id: " + docID + ", score: " + si.score);
                Explanation e = indexSearcher.explain(query, si.doc);
                System.out.println("Explanation： \n" + e);
                System.out.println("********************************************************************");
                System.out.println("custom FSDM: ");
                System.out.println(FSDM(docID, Statistics.getTokens("Top countries in production from aquaculture 2017")));
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testFSDMRankingList() {
        System.out.println(RelevanceRanking.FSDMRankingList("Top countries in production from aquaculture 2017"));
    }
}
