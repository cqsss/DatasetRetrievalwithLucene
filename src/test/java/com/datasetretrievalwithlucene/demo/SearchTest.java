package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class SearchTest {
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    @Test
    public void testQueryIndex() {
        Analyzer analyzer = new EnglishAnalyzer();
        QueryParser queryParser = new QueryParser("content", analyzer);
        Similarity similarity= new ClassicSimilarity();
        try {
            Query query = queryParser.parse("dog cat");
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(similarity);
            /**
             * maxDoc(): number of documents.
             * docCount(): number of documents that contain this field.
             * sumDocFreq(): number of postings-list entries.
             * sumTotalTermFreq(): number of tokens.
             */
            System.out.println(indexSearcher.collectionStatistics("content"));

            Expression expr = JavascriptCompiler.compile("sqrt(_score) + ln(popularity)");

            SimpleBindings bindings = new SimpleBindings();
            bindings.add("_score", DoubleValuesSource.SCORES);
            bindings.add("popularity", DoubleValuesSource.fromIntField("popularity"));
            Sort sort = new Sort(expr.getSortField(bindings, true));
            TopDocs docsSearch = indexSearcher.search(query, 10);

            System.out.println("--- total ---: " + docsSearch.totalHits);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            for (ScoreDoc si : scoreDocs) {
                Integer docID = si.doc;
                Document document = indexReader.document(docID);
                System.out.println("dataset_id: " + document.get("dataset_id") + ", score: " + si.score);
                Explanation e = indexSearcher.explain(query, si.doc);

                System.out.println("Explanation： \n"+e);
                System.out.println("********************************************************************");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getFreq(List<String> tokens) throws IOException {
        directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
        indexReader = DirectoryReader.open(directory);
        for (String token : tokens) {
            System.out.println(indexReader.totalTermFreq(new Term("content", new BytesRef(token))));
        }
        System.out.println(indexReader.getSumTotalTermFreq("content"));
    }
    @Test
    public void testFreq() throws IOException {
        getFreq(Statistics.getTokens("cats and dogs"));
    }
}
