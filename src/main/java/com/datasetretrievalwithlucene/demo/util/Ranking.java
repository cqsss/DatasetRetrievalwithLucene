package com.datasetretrievalwithlucene.demo.util;

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
import java.util.List;

public class Ranking {
    private static Directory directory;
    private static IndexReader indexReader;
    private static IndexSearcher indexSearcher;
    public static void init() {
        try {
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Double BM25(Integer doc_id, String field, List<String> tokens) {
        init();
        Double score = 0.0;
        Double k1 = 1.2;
        Double b = 0.75;
        try {
            Double N = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            Double D = 1.0;
            if(terms != null)
                D = (double) terms.getSumTotalTermFreq();
            Double avgdl = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
            //System.out.println("doc_id: " + doc_id);
            for (String token : tokens) {
                BytesRef bytesRef = new BytesRef(token);
                Double n = (double) indexReader.docFreq(new Term(field, bytesRef));
                Double idf = Math.log((N - n + 0.5) / (n + 0.5) + 1);
                Double f = 1.0;
                if(terms != null)
                {
                    TermsEnum termsIterator = terms.iterator();
                    if(termsIterator.seekExact(bytesRef)) f = (double)termsIterator.totalTermFreq();
                }
                Double tmp = idf * (f * (k1 + 1.0) / (f + k1 * (1.0 - b + b * D /avgdl)));
                score += tmp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(score);
        return score;
    }
    public static Double TFIDF(Integer doc_id, String field, List<String> tokens) {
        Double score = 0.0;
        try {
            Double docCount = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            Double D = 1.0;
            if(terms != null)
                D = (double) terms.getSumTotalTermFreq();
            Double lengthNorm = 1.0 / Math.sqrt(D);
            //System.out.println("doc_id: " + doc_id);
            for (String token : tokens) {
                BytesRef bytesRef = new BytesRef(token);
                Double docFreq = (double) indexReader.docFreq(new Term(field, bytesRef));
                Double idf = Math.log((docCount + 1.0) / (docFreq + 1.0)) + 1.0;
                Double tf = 1.0;
                if(terms != null)
                {
                    TermsEnum termsIterator = terms.iterator();
                    if(termsIterator.seekExact(bytesRef)) tf = Math.sqrt((double)termsIterator.totalTermFreq());
                }
                Double tmp = lengthNorm * idf * tf;
                score += tmp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(score);
        return score;
    }

}
