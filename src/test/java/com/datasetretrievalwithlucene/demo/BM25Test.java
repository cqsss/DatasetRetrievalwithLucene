package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class BM25Test {
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    public void init() {
        try {
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Double BM25(Integer doc_id, String field, List<String> tokens) {
        Double score = 0.0;
        Double k1 = 1.2;
        Double b = 0.75;
        try {
            Double N = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            Double D = 1.0;
            if(terms != null)
                D = (double) terms.getSumTotalTermFreq();
            System.out.println(terms.getStats());
            Double avgdl = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
            System.out.println("doc_id: " + doc_id);
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
                System.out.println("token: " + token);
                System.out.println("N: " + N);
                System.out.println("n: " + n);
                System.out.println("D: " + D);
                System.out.println("avgdl: " + avgdl);
                System.out.println("f: " + f);
                System.out.println("idf: " + idf);
                System.out.println("score: " + tmp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }
    @Test
    public void testBM25() {
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
                Document document = indexReader.document(docID);
                System.out.println("dataset_id: " + document.get("dataset_id") + ", score: " + si.score);
                Explanation e = indexSearcher.explain(query, si.doc);
                System.out.println("Explanation： \n" + e);
                System.out.println("********************************************************************");
                System.out.println("custom BM25: ");
                System.out.println(BM25(docID, "content", Statistics.getTokens("dog cat")));
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
