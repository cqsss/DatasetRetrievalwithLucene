package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class QueryProcessTest {
    private final List<Pair<String, String>> queryList = new ArrayList<>();
    private List<String> queries;
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    public void readOriginQueries(String fileName) {
        try {
            String str;
            String[] tmps;
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                tmps = str.split("\t");
                String tmp = tmps[1].replaceAll("\\p{P}"," ");
                List<String> tmpList = Statistics.getTokens(tmp);
                if (tmpList.size() > 8) continue;
                if (!tmp.matches("^[A-Za-z0-9 ]+$")) continue;
                queryList.add(new Pair<>(tmps[0], tmps[1]));
            }
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
    public void testQueryProcess(){
        readOriginQueries(GlobalVariances.GoogleQueriesPath);
        for (Pair<String, String> i : queryList) {
            System.out.println(i.getKey() + "\t" + i.getValue());
        }
    }
    @Test
    public void testQueryHits() {
        int[] total = new int[100];
        readQueries(GlobalVariances.testQueriesPath);
        //readQueries(GlobalVariances.title_notesTestQueriesPath);
        for (String qi : queries) {
            for (int i=0; i<=20; i++) {
                double k = i*0.1;
                String[] fields = GlobalVariances.queryFields;
                Analyzer analyzer = new EnglishAnalyzer();
                QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
                try {
                    Query query = queryParser.parse(qi);
                    /**
                     * sample query:
                     * (content:dataset title:dataset notes:dataset) (content:prijzen title:prijzen notes:prijzen) (content:de title:de notes:de) (content:supermarkt title:supermarkt notes:supermarkt) (content:java title:java notes:java)
                     */
                    //System.out.println(query);
                    int queryLength = query.toString().split(" ").length / fields.length;
                    directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
                    indexReader = DirectoryReader.open(directory);
                    indexSearcher = new IndexSearcher(indexReader);
                    TopDocs docsSearch = indexSearcher.search(query, 500);
                    ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
                    int cnt = 0;
                    for (ScoreDoc si : scoreDocs) {
                        double averageScore = si.score / (double) queryLength / (double) fields.length;
                        if (averageScore >= k) cnt++;
                        //System.out.println(e);
                    }
                    System.out.printf("%d\t", cnt);
                    if (cnt > 20)
                        total[i] ++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.print("\n");
        }
        for (int i=0; i<=20; i++)
            System.out.printf("%d\t", total[i]);
    }
}
