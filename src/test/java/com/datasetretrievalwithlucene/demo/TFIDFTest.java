package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class TFIDFTest {
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
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
    public Double TFIDF(Integer doc_id, String field, List<String> tokens) {
        Double score = 0.0;
        try {
            Double docCount = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            Double D = 1.0;
            if(terms != null)
                D = (double) terms.getSumTotalTermFreq();
            Double lengthNorm = 1.0 / Math.sqrt(D);
            System.out.println(terms.getStats());

            System.out.println("doc_id: " + doc_id);
            for (String token : tokens) {
                BytesRef bytesRef = new BytesRef(token);
                Double docFreq = (double) indexReader.docFreq(new Term(field, bytesRef));
                Double idf = Math.log((docCount + 1.0) / (docFreq + 1.0)) + 1.0;
                Double tf = 0.0;
                if(terms != null)
                {
                    TermsEnum termsIterator = terms.iterator();
                    if(termsIterator.seekExact(bytesRef)) tf = Math.sqrt((double)termsIterator.totalTermFreq());
                }
                Double tmp = lengthNorm * idf * tf;
                score += tmp;
                System.out.println("token: " + token);
                System.out.println("docCount: " + docCount);
                System.out.println("docFreq: " + docFreq);
                System.out.println("lengthNorm: " + lengthNorm);
                System.out.println("idf: " + idf);
                System.out.println("tf: " + tf);
                System.out.println("score: " + tmp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }
    @Test
    public void testTFIDF() {
        init();
        try {
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new QueryParser("content", analyzer);
            Query query = queryParser.parse("Dataset: Wohnungspreise in Ungarn");
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
                System.out.println(TFIDF(docID, "content", Statistics.getTokens("Dataset: Wohnungspreise in Ungarn")));
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testTFIDFRankingList() {
        System.out.println(RelevanceRanking.TFIDFRankingList("Dataset: Wohnungspreise in Ungarn"));
    }
}
