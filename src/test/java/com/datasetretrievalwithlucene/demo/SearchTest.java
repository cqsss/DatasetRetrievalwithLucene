package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.document.Document;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;

@SpringBootTest
public class SearchTest {
    @Test
    public void queryIndexTest() {
        Analyzer analyzer = new EnglishAnalyzer();
        QueryParser queryParser = new QueryParser("content", analyzer);
        Similarity tfidfSimilarity = new ClassicSimilarity();
        try {
            Query query = queryParser.parse("city");
            Directory directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(tfidfSimilarity);
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
                System.out.println("dataset_local_id: " + document.get("local_id") + ", score: " + si.score);
                Explanation e = indexSearcher.explain(query, si.doc);
                System.out.println("Explanation： \n"+e);
                System.out.println("********************************************************************");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
