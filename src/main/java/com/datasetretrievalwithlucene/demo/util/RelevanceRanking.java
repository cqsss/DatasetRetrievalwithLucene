package com.datasetretrievalwithlucene.demo.util;

import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

import java.nio.file.Paths;
import java.util.*;

public class RelevanceRanking {
    private static Directory directory;
    private static IndexReader indexReader;
    private static Map<String, Double> wT;
    private static Map<String, Double> wO;
    private static Map<String, Double> wU;
    private static Map<Pair<String, String>, Long> fieldTermFreq;
    private static Map<String, List<String>> fieldContent;
    private static Map<String, Long> fieldDocLength;
    public static void init() {
        try {
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void getCollectionStatistics(List<String> tokens) {
        try {
            wT = new HashMap<>();
            wO = new HashMap<>();
            wU = new HashMap<>();
            fieldTermFreq = new HashMap<>();
            double base = 0.0;
            for (Map.Entry jsonObject : GlobalVariances.getBoostWeights().entrySet()) {
                String field = jsonObject.getKey().toString();
                Double w = Double.parseDouble(jsonObject.getValue().toString());
                base += w;
                for (String token : tokens) {
                    fieldTermFreq.put(new Pair<>(field, token), indexReader.totalTermFreq(new Term(field, new BytesRef(token))));
                }
            }
            for (Map.Entry jsonObject : GlobalVariances.getBoostWeights().entrySet()) {
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
    public static void getDocumentStatistics(Integer doc_id) {
        try {
            fieldContent = new HashMap<>();
            fieldDocLength = new HashMap<>();

            for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
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

    public static Double getTF_T(Integer doc_id, String field, String qi) {
        double res = 0.0;
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
    public static Double getTF_O(String field, String qi1, String qi2) {
        double res = 0.0;
        List<String> content = fieldContent.get(field);
        for (int i = 0; i + 1 < content.size(); i++) {
            if (qi1.equals(content.get(i)) && qi2.equals(content.get(i + 1)))
                res += 1.0;
        }
        //System.out.println(qi1 + " " + qi2 + " TF_O: " + res);
        return res;
    }
    public static Double getTF_U(String field, String qi1, String qi2) {
        double res = 0.0;
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

    public static Double getFSDM_T(Integer doc_id, List<String> queries) {
        double res = 0.0;
        try {
            for (String qi : queries) {
                double tmp = 0.0;
                for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi));
                    double Dj = (double) fieldDocLength.get(field);
                    tmp += wT.get(field) * (getTF_T(doc_id, field, qi) + miu * cf / Cj) / (Dj + miu);
                }
                //System.out.println("T: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("FSDM_T: " + res);
        return res;
    }
    public static Double getFSDM_O(List<String> queries) {
        double res = 0.0;
        try {
            for (int i = 0; i + 1 < queries.size(); i++) {
                double tmp = 0.0;
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi1)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi1));
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi2)))
                        cf = Math.min(cf, (double) fieldTermFreq.get(new Pair<>(field, qi2)));
                    double Dj = (double) fieldDocLength.get(field);
                    tmp += wO.get(field) * (getTF_O(field, qi1, qi2) + miu * cf / Cj) / (Dj + miu);
                }
                //System.out.println("O: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("FSDM_O: " + res);
        return res;
    }
    public static Double getFSDM_U(List<String> queries) {
        double res = 0.0;
        try {
            for (int i = 0; i + 1 < queries.size(); i++) {
                double tmp = 0.0;
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi1)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi1));
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi2)))
                        cf = Math.min(cf, (double) fieldTermFreq.get(new Pair<>(field, qi2)));
                    double Dj = (double) fieldDocLength.get(field);
                    tmp += wU.get(field) * (getTF_U(field, qi1, qi2) + miu * cf / Cj) / (Dj + miu);
                }
                //System.out.println("U: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("FSDM_U: " + res);
        return res;
    }
    public static Double FSDM(Integer doc_id, List<String> tokens) {
        Double lambdaT = 1.0 / 3.0;
        Double lambdaO = 1.0 / 3.0;
        Double lambdaU = 1.0 / 3.0;
        getCollectionStatistics(tokens);
        getDocumentStatistics(doc_id);
        return lambdaT * getFSDM_T(doc_id, tokens) +
                lambdaO * getFSDM_O(tokens) +
                lambdaU * getFSDM_U(tokens);
    }
    public static Double BM25(Integer doc_id, String field, List<String> tokens) {
        init();
        double score = 0.0;
        double k1 = 1.2;
        double b = 0.75;
        try {
            double N = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            double D = 0.0;
            if(terms != null)
                D = (double) terms.getSumTotalTermFreq();
            double avgdl = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
            //System.out.println("doc_id: " + doc_id);
            for (String token : tokens) {
                BytesRef bytesRef = new BytesRef(token);
                double n = (double) indexReader.docFreq(new Term(field, bytesRef));
                double idf = Math.log((N - n + 0.5) / (n + 0.5) + 1);
                double f = 0.0;
                if(terms != null)
                {
                    TermsEnum termsIterator = terms.iterator();
                    if(termsIterator.seekExact(bytesRef)) f = (double)termsIterator.totalTermFreq();
                }
                double tmp = idf * (f * (k1 + 1.0) / (f + k1 * (1.0 - b + b * D /avgdl)));
                score += tmp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(score);
        return score;
    }
    public static Double TFIDF(Integer doc_id, String field, List<String> tokens) {
        double score = 0.0;
        try {
            double docCount = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            double D = 0.0;
            if(terms != null)
                D = (double) terms.getSumTotalTermFreq();
            double lengthNorm = 1.0 / (Math.sqrt(D) + 1.0);
            //System.out.println("doc_id: " + doc_id);
            for (String token : tokens) {
                BytesRef bytesRef = new BytesRef(token);
                double docFreq = (double) indexReader.docFreq(new Term(field, bytesRef));
                double idf = Math.log((docCount + 1.0) / (docFreq + 1.0)) + 1.0;
                double tf = 0.0;
                if(terms != null)
                {
                    TermsEnum termsIterator = terms.iterator();
                    if(termsIterator.seekExact(bytesRef)) tf = Math.sqrt((double)termsIterator.totalTermFreq());
                }
                double tmp = lengthNorm * idf * tf;
                score += tmp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(score);
        return score;
    }

}
